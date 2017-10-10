'''
Name: Dallas Fraser
Email: d6fraser@uwaterloo.ca
Date: 2017-07-27
Project: Tangent GT
Purpose: Used to parse math documents and gensim to iterate through them
'''
from gensim import corpora
from six import iteritems
from tangent.htmlStriper import strip_tags
from nltk.stem.porter import PorterStemmer
from nltk.corpus import stopwords
from tangent.math_extractor import MathExtractor
from tangent.mathdocument import MathDocument
import os

STOP_WORDS = set(stopwords.words('english'))


def convert_math_expression(mathml):
    """Returns the math tuples for a given math expression

    Parameters:
        mathml: the math expression (string)
    Returns:
        : a string of the math tuples
    """
    try:
        tokens = MathExtractor.math_tokens(mathml)
        pmml = MathExtractor.isolate_pmml(tokens[0])
        tree_root = MathExtractor.convert_to_mathsymbol(pmml)
        height = tree_root.get_height()
        eol = False
        if height <= 2:
            eol = True
        pairs = tree_root.get_pairs("", 1, eol=eol, unbounded=True)
        node_list = [format_node(node)
                     for node in pairs]
        return " ".join(node_list)
    except AttributeError:
        return ""


def format_node(node):
    """Returns a formatted node

    Parameters:
        node: the the math node (string)
    Returns:
        : a formatted node
    """
    node = str(node).lower()
    node = node.replace("*", "\*")
    for letter in "zxcvbnmasdfghjklqwertyuiop":
        node = node.replace("?" + letter, "*")
    return ((str(node)
            .replace(" ", "")
            .replace("&comma;", "comma")
            .replace("&lsqb;", "lsqb")
            .replace("&rsqb;", "rsqb")
             ))


def keep_word(word):
    """Returns true if the word should be kepts

    Parameters:
        word: the word to be checked (string)
    Returns:
        result: true if the word is worth keep (boolean)
    """
    result = False
    if len(word) > 1 and word not in STOP_WORDS and word.isalpha():
        result = True
    return result


def format_paragraph(paragraph, stemmer):
    """Returns a formatted paragraph

    Parameters:
        paragraph: the text paragraph to format (string)
        stemmer: the stemmer to use
    Returns:
        : a list of words (list)
    """
    result = strip_tags(paragraph)
    words = result.split(" ")
    return [stemmer.stem(word.lower().strip()) for word in words
            if keep_word(word.strip())]


class MathCorpus(object):
    def __init__(self, directory, filepath=None):
        """A gensim corpus for math documents uses tangent to encode MathMl

        Parameters:
            directory: the corpus directory (os.path)
            filepath: the filepath to the dictionary (os.path)
        """
        self.directory = directory
        if filepath is None:
            self.directory = directory
            fps = []
            for p, __, files in os.walk(self.directory):
                for file in files:
                    fps.append(os.path.join(p, file))
            self.length = len(fps)
            dictionary = corpora.Dictionary(self.parse_file(file)
                                            for file in fps)
            stop_ids = [dictionary.token2id[stopword]
                        for stopword in STOP_WORDS
                        if stopword in dictionary.token2id]
            once_ids = [tokenid
                        for tokenid, docfreq in iteritems(dictionary.dfs)
                        if docfreq == 1]
            # remove stop words and words that appear only once
            dictionary.filter_tokens(stop_ids + once_ids)
            # remove gaps in id sequence after words that were removed
            dictionary.compactify()
            self.dictionary = dictionary
        else:
            self.load_dictionary(filepath)
        print("Dictionary", self.dictionary)
        print(self.dictionary.token2id)

    def parse_file(self, filepath):
        """Returns the parsed contents of the file

        Parameters:
            filepath: the path to the file to be parsed (os.path)
        Returns:
            result: the parsed contents of the file (list)
        """
        if filepath.endswith(".xhtml") or filepath.endswith(".html"):
            result = ParseDocument(filepath).get_words().split(" ")
        else:
            result = []
        return result

    def load_dictionary(self, filepath):
        """Load a previous dictionary

        Parameters:
            filepath: the path to the dictionary (os.path)
        """
        self.dictionary = corpora.Dictionary.load(filepath)

    def save_dictionary(self, filepath):
        """Save the current dictionary

        Parameters:
            filepath: the path to where the dictionary will be saved (os.path)
        """
        self.dictionary.save(filepath)

    def __iter__(self):
        """ Yields one parsed document at a time"""
        for subdir, __, files in os.walk(self.directory):
            for file in files:
                filepath = os.path.join(subdir, file)
                if filepath.endswith(".xhtml") or filepath.endswith(".html"):
                    words = ParseDocument(filepath).get_words()
                    yield self.dictionary.doc2bow(words.split(" "))
                else:
                    # just skip for now
                    pass

    def __len__(self):
        """Returns the number of document in the corpus """
        return self.length


class ParseDocument(object):
    def __init__(self, filepath):
        """
        Parameters:
            filepath: the filepath to the document
        """
        self.filepath = filepath
        self.lines = []
        self.formulas = []
        self.text = []
        stemmer = PorterStemmer()
        (__, content) = MathDocument.read_doc_file(self.filepath)
        while len(content) != 0:
            (start, end) = MathExtractor.next_math_token(content)
            if start == -1:
                # can just print the rest
                words = format_paragraph(content, stemmer)
                self.lines.append(" ".join(words))
                self.text += words
                content = ""
            else:
                words = format_paragraph(content[0:start], stemmer)
                self.lines.append(" ".join(words))
                self.text += words
                maths = convert_math_expression(content[start:end])
                self.lines.append(maths)
                self.formulas.append(maths)
                # now move the content further along
                content = content[end:]

    def get_words(self):
        """Returns a string of the words parsed"""
        return " ".join(self.lines)

    def get_math(self):
        """Returns a list of math formulas """
        return self.formulas

    def get_text(self):
        """Returns a list of text"""
        return self.text

if __name__ == "__main__":
    pass
