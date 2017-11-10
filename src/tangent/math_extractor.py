import sys
import re
import string
import io
import xml
import os
import codecs
import platform
from bs4 import BeautifulSoup
from xml.parsers import expat
try:
    from mathsymbol import MathSymbol
    from symboltree import SymbolTree
    from latex_mml import LatexToMathML
    from exceptions import UnknownTagException
    from utility import uprint
except ImportError:
    from tangent.mathsymbol import MathSymbol
    from tangent.symboltree import SymbolTree
    from tangent.latex_mml import LatexToMathML
    from tangent.exceptions import UnknownTagException
    from tangent.utility import uprint
__author__ = 'Nidhin, FWTompa'

## TODO: produce cleaned_file_content for text indexing on a separate pass (called separately in Version 0.2)
## simplify math extraction by creating simple list of math expressions and then grouping them by SLT, rather than by LaTeX


class MathExtractor:
    def __init__(self):
        pass

    namespace = r"(?:[^> :]*:)?"
    attributes = r"(?: [^>]*)?"
    math_expr = "<"+namespace+"math"+attributes+r">.*?</"+namespace+"math>"
    dollars = r"(?<!\\)\$+"
    latex_expr = dollars+".{1,200}?"+dollars # converted to math_expr in cleaned text
    # latex could also be surrounded by \(..\) or \[..\], but these are ignored for now (FWT)
    text_token = r"[^<\s]+"
 
    math_pattern = re.compile(math_expr, re.DOTALL)  # TODO: allow for LaTeX as well
    # split_pattern = re.compile(math_expr+"|"+latex_expr+"|"+text_token, re.DOTALL)

    inner_math = re.compile(".*(<"+math_expr+")", re.DOTALL)  # rightmost <*:math 
    open_tag = re.compile("<(?!/)(?!mws:qvar)"+namespace, re.DOTALL) # up to and including namespace
    close_tag = re.compile("</(?!mws:qvar)"+namespace, re.DOTALL)    # but keep qvar namespace

##    @classmethod
##    def get_string_tokenized(cls, content):
##        return cls.split_pattern.findall(content)

    @classmethod
    def next_math_token(cls, content):
        token = cls.math_pattern.search(content)
        result = (-1, -1)
        if token is not None:
            result = token.span()
        return result

    @classmethod
    def math_tokens(cls, content):
        """
        extract Math expressions from XML (incl. HTML) file
        
        param content: XML document
        type  content: string

        return: embedded math expressions
        rtype:  list(string) where each string is a MathML expr
        """

        tokens = cls.math_pattern.findall(content)
        math = []

        for token in tokens:
            # print("Token = "+token,flush=True)

            if token.endswith("math>"): # MathML token
##                # Does not handle the case where one math expression is nested inside another
##                #       (likely with different namespaces)
##                # N.B. Removing this check speeds up processing significantly (FWT)
##                token = cls.inner_math.sub(r"\0",token)  # find innermost <*:math 
                token = cls.close_tag.sub("</",token) # drop namespaces (FWT)
                token = cls.open_tag.sub("<",token)

                math.append(token)
                
            else:  # LaTeX math expression
                tex = token.strip("$") # TODO: handle other latex delimiters
                math.append(LatexToMathML.convert_to_mathml(tex))           

        return math


    @classmethod
    def isolate_pmml(cls,tree):
        """
        extract the Presentation MathML from a MathML expr
        
        param tree: MathML expression
        type  tree: string
        return: Presentation MathML
        rtype:  string
        """
        parsed_xml=BeautifulSoup(tree)


        math_root=parsed_xml.find("math") # namespaces have been removed (FWT)
##        altext=math_root.get("alttext")
        application_tex= math_root.find("annotation",{"encoding":"application/x-tex"})
        #print("M: %s, A: %s, AA: %s" % (parsed_xml,altext,application_tex))
        
        if application_tex:
##            application_tex_text=application_tex.text
            application_tex.decompose()

##        latex=altext if altext else application_tex_text

        pmml_markup=math_root.find("annotation-xml",{"encoding":"MathML-Presentation"})
        if pmml_markup:
            pmml_markup.name = "math"
        else:
            pmml_markup=math_root
            cmml_markup=math_root.find("annotation-xml",{"encoding":"MathML-Content"})
            if cmml_markup:
                cmml_markup.decompose() # delete any Content MML
        pmml_markup['xmlns']="http://www.w3.org/1998/Math/MathML" # set the default namespace
        return str(pmml_markup)

    @classmethod
    def convert_to_mathsymbol(cls, elem):
        """
        Parse expression from MathML


        :param elem: mathml
        :type  elem: string

        :rtype MathSymbol or None
        :return root of symbol tree

        """
        if (len(elem) == 0):
            return None
        elem_content = io.StringIO(elem) # treat the string as if a file
        parser = xml.etree.ElementTree.XMLParser(encoding="utf-8")
        root = xml.etree.ElementTree.parse(elem_content,
                                           parser=parser).getroot()
        mmathml = xml.etree.ElementTree.tostring(root, encoding="unicode")
        uprint("parse_from_mathml tree: " + mmathml)
        return MathSymbol.parse_from_mathml(root)

    @classmethod
    def convert_and_link_mathml(cls, elem, document=None, position=None):
        """
        Parse expression from MathML keeping the links to the original MathML for visualization purposes


        :param elem: mathml
        :type  elem: string

        :rtype SymbolTree or None
        :return Symbol tree instance

        """
        if (len(elem) == 0):
            return None

        elem_content = io.StringIO(elem) # treat the string as if a file
        root = xml.etree.ElementTree.parse(elem_content).getroot()
##        print("parse_from_mathml tree: " + xml.etree.ElementTree.tostring(root,encoding="unicode"))
        symbol_root = MathSymbol.parse_from_mathml(root)

        return SymbolTree(symbol_root, document, position, root)


    @classmethod
    def parse_from_tex(cls, tex, file_id=-1, position=[0]):
        """
        Parse expression from Tex string using latexmlmath to convert to presentation markup language


        :param tex: tex string
        :type tex string
        :param file_id: file identifier
        :type  file_id: int

        :rtype SymbolTree
        :return equivalent SymbolTree

        """

        #print("Parsing tex doc %s" % file_id,flush=True)
        mathml=LatexToMathML.convert_to_mathml(tex)
        pmml = cls.isolate_pmml(mathml)
##        print('LaTeX converted to MathML: \n' )
        return SymbolTree(cls.convert_to_mathsymbol(pmml),file_id,position)


    @classmethod
    def parse_from_xml(cls, content, content_id, missing_tags=None, problem_files=None):
        """
        Parse expressions from XML file

        :param content: XML content to be parsed
        :type  content: string
        :param content_id: fileid for indexing or querynum for querying
        :type  content_id: int
        :param missing_tags: dictionary to collect tag errors
        :type  missing_tags: dictionary(tag->set(content_id))
        :param problem_files: dictionary to collect parsing errors
        :type  problem_files: dictionary(str->set(content_id))

        :rtype list(SymbolTree)
        :return list of Symbol trees found in content identified by content_id

        """
        idx = -1
        try:
            trees = cls.math_tokens(content)
            groupBySLT = {}
            for idx, tree in enumerate(trees):
                #print("Parsing doc %s, expr %i" % (content_id,idx),flush=True)
                pmml = cls.isolate_pmml(tree)
                symbol_tree = cls.convert_to_mathsymbol(pmml)
                if symbol_tree:
                    s = symbol_tree.tostring()
                    if s not in groupBySLT:
                        groupBySLT[s] = SymbolTree(symbol_tree,content_id,[idx])
                    else:
                        groupBySLT[s].position.append(idx)
            return(list(groupBySLT.values()))
        
        except UnknownTagException as e:
            print("Unknown tag in file or query "+str(content_id)+": "+e.tag, file=sys.stderr)
            missing_tags[e.tag] = missing_tags.get(e.tag, set())
            missing_tags[e.tag].add([content_id,idx])
        except Exception as err:
            reason = str(err)
            print("Parse error in file or query "+str(content_id)+": "+reason+": "+str(tree), file=sys.stderr)
            raise Exception(reason) # pass on the exception to identify the document or query

##    @classmethod
##    def convert_to_presentation_mml(cls, tex):
##        url = 'http://halifax.cs.rit.edu:8324/'
##        data = pickle.dumps(tex)
##        r = req.get(url, data=data)
##        # load the numpy array
##        try:
##            output = pickle.loads(r.text)
##            output = output.decode('utf-8')
##            return output
##        except Exception as e:
##            z = 9
