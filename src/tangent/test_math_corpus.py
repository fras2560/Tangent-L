'''
Name: Dallas Fraser
Email: d6fraser@uwaterloo.ca
Date: 2017-07-27
Project: Tangent GT
Purpose: Tests the math corpus file
'''
from tangent.math_corpus import MathCorpus, ParseDocument,\
                                format_paragraph, keep_word,\
                                convert_math_expression
from nltk.stem.porter import PorterStemmer
import unittest
import os


class TestMathCorpus(unittest.TestCase):
    def setUp(self):
        cwd = os.path.dirname(os.getcwd())
        self.fp = os.path.join(cwd, "testing", "test", "toyDocuments")
        self.corpus = os.path.join(cwd, "testing", "test", "tutorialDocuments")

    def tearDown(self):
        pass

    def test(self):
        mc = MathCorpus(self.fp)
        expected = [[(0, 1), (1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1)],
                    [(0, 1), (1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1)]]
        print(mc.dictionary.token2id)
        for index, vector in enumerate(mc):
            print(vector)
            self.assertEqual(expected[index], vector)

    def testTutorial(self):
        mc = MathCorpus(self.corpus)
        expect = ['human', 'time', 'minor', 'comput', 'survey', 'user',
                  'system', 'interfac', 'respons', 'graph', 'tree', 'ep']
        for key in expect:
            self.assertEqual(key in mc.dictionary.token2id.keys(), True)


class TestMathDocument(unittest.TestCase):
    def setUp(self):
        cwd = os.path.dirname(os.getcwd())
        self.fp = os.path.join(cwd, "testing", "test", "toyDocuments")

    def tearDown(self):
        pass

    def test1(self):
        md = ParseDocument(os.path.join(self.fp, "1.xhtml"))
        expect = ["mathemat",
                  "rigor",
                  "approach",
                  "quantum",
                  "field",
                  "theori",
                  "oper",
                  "algebra",
                  "in",
                  "case",
                  "('n!1','+','n')",
                  "('n!1','n!1')",
                  "('+','n!1','n')"]
        self.assertEqual(md.get_words().strip(), " ".join(expect))

    def test2(self):
        md = ParseDocument(os.path.join(self.fp, "2.xhtml"))
        expect = ["we",
                  "first",
                  "explain",
                  "formul",
                  "full",
                  "conform",
                  "theori",
                  "('n!1','+','n')",
                  "('n!1','n!1')",
                  "('+','n!1','n')",
                  "minkowski",
                  "algebra",
                  "quantum",
                  "field"]
        self.assertEqual(md.get_words().strip(), " ".join(expect))


class TestFunctions(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass

    def testFormatParagraph(self):
        stemmer = PorterStemmer()
        result = format_paragraph("<h1> Hello</h1> <p>How are you</p>",
                                  stemmer)
        self.assertEqual(result, ['hello', 'how'])

    def testConverMathExpression(self):
        test = """
                <math display="inline" id="1_(number):0">
                  <semantics>
                    <mi mathvariant="normal">
                      I
                    </mi>
                    <annotation-xml encoding="MathML-Content">
                      <ci>
                        normal-I
                      </ci>
                    </annotation-xml>
                    <annotation encoding="application/x-tex">
                      \mathrm{I}
                    </annotation>
                  </semantics></math>
               """
        result = convert_math_expression(test)
        self.assertEqual(result, "('v!i','!0','n')")

    def testKeepWord(self):
        self.assertEqual(keep_word("they"), False)
        self.assertEqual(keep_word("hello"), True)
        self.assertEqual(keep_word("pep8"), False)

    def testFormatParagraph2(self):
        test = """
                <p>
                  There are two ways to write the real number 1 as a
                  <a href="recurring_decimal"
                  title="wikilink">recurring decimal</a>:
                  as 1.000..., and as
                  <a class="uri" href="0.999..." title="wikilink">0.999...</a>
                  (<em><a class="uri" href="q.v."
                  title="wikilink">q.v.</a></em>).
                  There is only one way to represent the real number 1
                  as a <a href="Dedekind_cut" title="wikilink">Dedekind cut</a>
                  <math display="block" id="1_(number):1">
                </p>
               """
        stemmer = PorterStemmer()
        result = format_paragraph(test, stemmer)
        expect = ['there', 'two', 'way', 'write', 'real', 'number',
                  'recur', 'decim', 'there', 'one', 'way',
                  'repres', 'real', 'number', 'dedekind', 'cut']
        self.assertEqual(result, expect)
