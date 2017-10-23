'''
Name: Dallas Fraser
Email: d6fraser@uwaterloo.ca
Date: 2017-08-25
Project: Tangent
Purpose: To test the convert of mathml to Tangent Tuples
'''
import unittest
import os
WINDOWS = "nt"
try:
    from tangent.convert import convert_math_expression,\
                                check_node,\
                                check_wildcard,\
                                determine_node,\
                                expand_node_with_wildcards,\
                                EDGE_PAIR_NODE, COMPOUND_NODE, \
                                EOL_NODE, TERMINAL_NODE, SYMBOL_PAIR_NODE,\
                                WILDCARD_MOCK, START_TAG, END_TAG
except ImportError:
    from convert import convert_math_expression,\
                                check_node,\
                                check_wildcard,\
                                determine_node,\
                                expand_node_with_wildcards,\
                                EDGE_PAIR_NODE, COMPOUND_NODE, \
                                EOL_NODE, TERMINAL_NODE, SYMBOL_PAIR_NODE,\
                                WILDCARD_MOCK, START_TAG, END_TAG


class TestBase(unittest.TestCase):
    def loadFile(self, math_file):
        if os.name == WINDOWS:
            with open(math_file, encoding="utf8") as f:
                lines = []
                for line in f:
                    lines.append(line)
        else:
            with open(math_file) as f:
                lines = []
                for line in f:
                    lines.append(line)
        return " ".join(lines)

    def log(self, out):
        if self.debug:
            print(out)


class TestSymbolPairs(TestBase):
    def setUp(self):
        self.debug = True
        self.file = os.path.join(os.getcwd(), "testFiles", "test_wildcard.xml")
        self.mathml = self.loadFile(self.file)

    def tearDown(self):
        pass

    def testConvert(self):
        results = convert_math_expression(self.mathml, symbol_pairs=False)
        expect = [START_TAG,
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testConvertNotFalse(self):
        results = convert_math_expression(self.mathml,
                                          symbol_pairs=False,
                                          eol=True)
        expect = [START_TAG,
                  """#('*','!0','n')#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testConvertEdgePairs(self):
        file = os.path.join(os.getcwd(), "testFiles", "test_edge_pair.xml")
        mathml = self.loadFile(file)
        results = convert_math_expression(mathml,
                                          symbol_pairs=False,
                                          edge_pairs=True)
        expect = [START_TAG,
                  """#('n','a','v!k')#""",
                  """#('n','n','/')#""",
                  """#('n','n','n!2')#""",
                  """#('n','a','n!2')#""",
                  """#('n','n','gt')#""",
                  """#('w','n','n!2')#""",
                  """#('w','e','n!2')#""",
                  """#('n','n','m!()1x2')#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)


class TestDifferentWildcardReplacements(TestBase):
    def setUp(self):
        self.debug = True
        self.file = os.path.join(os.getcwd(), "testFiles", "test_wildcard.xml")
        self.mathml = self.loadFile(self.file)

    def tearDown(self):
        pass

    def testConvertEOL(self):
        results = convert_math_expression(self.mathml, eol=True)
        expect = [START_TAG,
                  """#('v!t','*','b')#""",
                  """#('*','!0','n')#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testConvertAll(self):
        results = convert_math_expression(self.mathml,
                                          terminal_symbols=True,
                                          compound_symbols=True,
                                          edge_pairs=True)
        expect = [START_TAG,
                  """#('v!t','*','b')#""",
                  """#('*','!0')#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)


class TestDetermineNode(TestBase):
    def setUp(self):
        self.debug = True

    def tearDown(self):
        pass

    def testDetermineNode(self):
        self.assertEqual(determine_node(('m!()1x1', '?w', 'n')),
                         SYMBOL_PAIR_NODE)
        self.assertEqual(determine_node(('n!0', '!0')),
                         TERMINAL_NODE)
        self.assertEqual(determine_node(('n!0', '!0', "n")),
                         EOL_NODE)
        self.assertEqual(determine_node(('v!α', "['n', 'b']")),
                         COMPOUND_NODE)
        self.assertEqual(determine_node(('n', 'b', 'v!y')),
                         EDGE_PAIR_NODE)


class TestSynonym(TestBase):
    def setUp(self):
        self.debug = True
        self.file = os.path.join(os.getcwd(), "testFiles", "test_1.xml")
        self.mathml = self.loadFile(self.file)
        self.file2 = os.path.join(os.getcwd(), "testFiles", "test_2.xml")
        self.mathml2 = self.loadFile(self.file2)

    def tearDown(self):
        pass

    def testExpandNodeWithWildcards(self):
        # symbol pair with wildcard
        expect = [('m!()1x1', '?w', 'n')]
        result = expand_node_with_wildcards(('m!()1x1', '?w', 'n'))
        self.log(result)
        self.assertEquals(result, expect)
        # normal symbol pair
        expect = [('m!()1x1', 'n!1', 'n'),
                  (WILDCARD_MOCK, 'n!1', 'n'),
                  ('m!()1x1', WILDCARD_MOCK, 'n'),
                  ]
        result = expand_node_with_wildcards(('m!()1x1', 'n!1', 'n'))
        self.log(result)
        self.assertEquals(result, expect)
        # terminal symbol
        result = expand_node_with_wildcards(('n!0', '!0'))
        self.log(result)
        expect = [('n!0', '!0')]
        self.assertEquals(result, expect)
        # eol symbol
        result = expand_node_with_wildcards(('n!0', '!0', "n"))
        expect = [('n!0', '!0', "n")]
        self.log(result)
        self.assertEquals(result, expect)
        # compound symbol
        expect = [('v!α', "['n', 'b']"), (WILDCARD_MOCK, "['n', 'b']")]
        result = expand_node_with_wildcards(('v!α', "['n', 'b']"))
        self.log(result)
        self.assertEquals(result, expect)

    def testConvertWithSynonyms(self):
        results = convert_math_expression(self.mathml,
                                          compound_symbols=True,
                                          edge_pairs=True,
                                          terminal_symbols=True,
                                          eol=True,
                                          synonyms=True)
        expect = [START_TAG,
                  """#('*',"['n','b']")#""",
                  """#('*','=','n')#""",
                  """#('=','n!1','n')#""",
                  """#('*','n!1','n')#""",
                  """#('=','*','n')#""",
                  """#('n!1','!0')#""",
                  """#('n','n','=')#""",
                  """#('n','n','*')#""",
                  """#('b','n','*')#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)
        # test the other file
        results = convert_math_expression(self.mathml2,
                                          compound_symbols=True,
                                          edge_pairs=True,
                                          terminal_symbols=True,
                                          eol=True,
                                          synonyms=True)
        expect = [START_TAG,
                  """#('v!α',"['n','b']")#""",
                  """#('*',"['n','b']")#""",
                  """#('v!α','m!()1x1','n')#""",
                  """#('*','m!()1x1','n')#""",
                  """#('v!α','*','n')#""",
                  """#('m!()1x1',"['n','w']")#""",
                  """#('*',"['n','w']")#""",
                  """#('m!()1x1','=','n')#""",
                  """#('*','=','n')#""",
                  """#('m!()1x1','*','n')#""",
                  """#('=','v!y','n')#""",
                  """#('*','v!y','n')#""",
                  """#('=','*','n')#""",
                  """#('v!y','n!0','b')#""",
                  """#('*','n!0','b')#""",
                  """#('v!y','*','b')#""",
                  """#('n!0','!0')#""",
                  """#('n','b','v!y')#""",
                  """#('n','b','*')#""",
                  """#('n','n','=')#""",
                  """#('n','n','*')#""",
                  """#('m!()1x1','v!x','w')#""",
                  """#('*','v!x','w')#""",
                  """#('m!()1x1','*','w')#""",
                  """#('v!x','n!0','b')#""",
                  """#('*','n!0','b')#""",
                  """#('v!x','*','b')#""",
                  """#('n!0','!0')#""",
                  """#('w','b','v!x')#""",
                  """#('w','b','*')#""",
                  """#('n','n','m!()1x1')#""",
                  """#('n','n','*')#""",
                  """#('v!α','n!0','b')#""",
                  """#('*','n!0','b')#""",
                  """#('v!α','*','b')#""",
                  """#('n!0','!0')#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)


class TestWildcardReductionAndCheck(TestBase):
    def setUp(self):
        self.debug = True

    def tearDown(self):
        pass

    def testCheckNode(self):
        valid_nodes = [('?w', 'm!()1x1', 'n'),
                       ('m!()1x1', '?w', 'n'),
                       ('=', 'v!y', 'n'),
                       ('v!y', 'n!0', 'b'),
                       ('m!()1x1', 'v!x', 'w'),
                       ('v!x', 'n!0', 'b'),
                       ('v!Î±', 'n!0', 'b'),
                       ('v!Î±', "['n','b']"),
                       ('m!()1x1', "['n','w']"),
                       ('n!0', '!0'),
                       ('n', 'b', '?w'),
                       ('n', 'n', '='),
                       ('w', 'b', 'v!x'),
                       ('n', 'n', 'm!()1x1'),
                       ('n', 'w', 'm!()1x1')]
        invalid_nodes = [('?v', '!0'),
                         ('?w', '?w', 'b'),
                         ("?w", "?w", "b", "nn"),
                         ("?w", "!0", "n")]
        for node in valid_nodes:
            self.assertEqual(check_node(node), True)
        for node in invalid_nodes:
            self.assertEqual(check_node(node), False)

    def testCheckWildcard(self):
        self.assertEqual(check_wildcard("?v"), True)
        self.assertEqual(check_wildcard("n!6"), False)
        self.assertEqual(check_wildcard("v!x"), False)


class TestTerminalQuery(TestBase):
    def setUp(self):
        self.debug = True
        self.f1 = os.path.join(os.getcwd(),
                               "testFiles",
                               "test_terminal_small_1.xml")
        self.f2 = os.path.join(os.getcwd(),
                               "testFiles",
                               "test_terminal_small_2.xml")

    def testF1(self):
        self.mathml = self.loadFile(self.f1)
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        self.log(results)
        expect = [START_TAG,
                  "#('v!𝖿𝗏','!0')#",
                  END_TAG]
        self.assertEqual(results, " ".join(expect))

    def testF2(self):
        self.mathml = self.loadFile(self.f2)
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        self.log(results)
        expect = [START_TAG,
                  "#('v!𝒔','!0')#",
                  END_TAG]
        self.assertEqual(results, " ".join(expect))


class TestArxivQuery(TestBase):
    def setUp(self):
        self.debug = True
        self.file = os.path.join(os.getcwd(), "testFiles", "test_1.xml")
        self.mathml = self.loadFile(self.file)

    def tearDown(self):
        pass

    def testBase(self):
        results = convert_math_expression(self.mathml)
        expect = [START_TAG,
                  "#('*','=','n')#",
                  "#('=','n!1','n')#",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testWindowSize(self):
        results = convert_math_expression(self.mathml, window_size=2)
        expect = [START_TAG,
                  "#('*','=','n')#",
                  "#('*','n!1','nn')#",
                  "#('=','n!1','n')#",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEOL(self):
        # height too big
        results = convert_math_expression(self.mathml, eol=True)
        expect = [START_TAG,
                  "#('*','=','n')#",
                  "#('=','n!1','n')#",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testCompoundSymbols(self):
        results = convert_math_expression(self.mathml, compound_symbols=True)
        expect = [START_TAG,
                  '''#('*',"['n','b']")#''',
                  '''#('*','=','n')#''',
                  '''#('=','n!1','n')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testTerminalSymbols(self):
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        expect = [START_TAG,
                  '''#('*','=','n')#''',
                  '''#('=','n!1','n')#''',
                  '''#('n!1','!0')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEdgePairs(self):
        results = convert_math_expression(self.mathml, edge_pairs=True)
        expect = [START_TAG,
                  '''#('*','=','n')#''',
                  '''#('=','n!1','n')#''',
                  '''#('n','n','=')#''',
                  '''#('b','n','*')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testUnbounded(self):
        results = convert_math_expression(self.mathml, unbounded=True)
        expect = [START_TAG,
                  '''#('*','=','n')#''',
                  '''#('=','n!1','n')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testLocation(self):
        results = convert_math_expression(self.mathml, location=True)
        expect = [START_TAG,
                  '''#('*','=','n','-')#''',
                  '''#('=','n!1','n','n')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)


class TestRandomEquation(TestBase):
    def setUp(self):
        self.debug = True
        self.file = os.path.join(os.getcwd(), "testFiles", "test_2.xml")
        self.mathml = self.loadFile(self.file)

    def tearDown(self):
        pass

    def testBase(self):
        results = convert_math_expression(self.mathml)
        expect = [START_TAG,
                  '''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('v!α','n!0','b')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testWindowSize(self):
        results = convert_math_expression(self.mathml, window_size=2)
        expect = [START_TAG,
                  '''#('v!α','m!()1x1','n')#''',
                  '''#('v!α','v!x','nw')#''',
                  '''#('v!α','=','nn')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('m!()1x1','v!y','nn')#''',
                  '''#('=','v!y','n')#''',
                  '''#('=','n!0','nb')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('m!()1x1','n!0','wb')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('v!α','n!0','b')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEOL(self):
        # height too big
        results = convert_math_expression(self.mathml, eol=True)
        expect = [START_TAG,
                  '''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('v!α','n!0','b')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testCompoundSymbols(self):
        results = convert_math_expression(self.mathml, compound_symbols=True)
        expect = [START_TAG,
                  '''#('v!α',"['n','b']")#''',
                  '''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1',"['n','w']")#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('v!α','n!0','b')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testTerminalSymbols(self):
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        expect = [START_TAG,
                  '''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('n!0','!0')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('n!0','!0')#''',
                  '''#('v!α','n!0','b')#''',
                  '''#('n!0','!0')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEdgePairs(self):
        results = convert_math_expression(self.mathml, edge_pairs=True)
        expect = [START_TAG,
                  '''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('n','b','v!y')#''',
                  '''#('n','n','=')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('w','b','v!x')#''',
                  '''#('n','n','m!()1x1')#''',
                  '''#('v!α','n!0','b')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testUnbounded(self):
        results = convert_math_expression(self.mathml, unbounded=True)
        expect = [START_TAG,
                  '''#('v!α','m!()1x1','n')#''',
                  '''#('v!α','v!x')#''',
                  '''#('v!α','n!0')#''',
                  '''#('v!α','=')#''',
                  '''#('v!α','v!y')#''',
                  '''#('v!α','n!0')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('m!()1x1','v!y')#''',
                  '''#('m!()1x1','n!0')#''',
                  '''#('=','v!y','n')#''',
                  '''#('=','n!0')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('m!()1x1','n!0')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('v!α','n!0','b')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testLocation(self):
        results = convert_math_expression(self.mathml, location=True)
        expect = [START_TAG,
                  '''#('v!α','m!()1x1','n','-')#''',
                  '''#('m!()1x1','=','n','n')#''',
                  '''#('=','v!y','n','nn')#''',
                  '''#('v!y','n!0','b','nnn')#''',
                  '''#('m!()1x1','v!x','w','n')#''',
                  '''#('v!x','n!0','b','nw')#''',
                  '''#('v!α','n!0','b','-')#''',
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
