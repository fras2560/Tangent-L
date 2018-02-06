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
                                expand_nodes_with_location,\
                                expand_node_with_wildcards,\
                                EDGE_PAIR_NODE, COMPOUND_NODE, \
                                EOL_NODE, TERMINAL_NODE, SYMBOL_PAIR_NODE,\
                                WILDCARD_MOCK, START_TAG, END_TAG,\
                                PAYLOAD_DELIMITER as P_D
except ImportError:
    from convert import convert_math_expression,\
                                check_node,\
                                check_wildcard,\
                                determine_node,\
                                expand_nodes_with_location,\
                                expand_node_with_wildcards,\
                                EDGE_PAIR_NODE, COMPOUND_NODE, \
                                EOL_NODE, TERMINAL_NODE, SYMBOL_PAIR_NODE,\
                                WILDCARD_MOCK, START_TAG, END_TAG,\
                                PAYLOAD_DELIMITER as P_D


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


class TestSpecial(TestBase):
    def setUp(self):
        self.debug = True
        self.mathml = """
<m:math>
  <m:semantics xml:id="m1.1a">
    <m:apply xml:id="m1.1.4" xref="m1.1.4.pmml">
      <m:plus xml:id="m1.1.2" xref="m1.1.2.pmml"/>
      <m:ci xml:id="m1.1.1" xref="m1.1.1.pmml">x</m:ci>
      <mws:qvar xmlns:mws="http://search.mathweb.org/ns" name="y"/>
    </m:apply>
    <m:annotation-xml encoding="MathML-Presentation" xml:id="m1.1b">
      <m:mrow xml:id="m1.1.4.pmml" xref="m1.1.4">
        <m:mi xml:id="m1.1.1.pmml" xref="m1.1.1">x</m:mi>
        <m:mo xml:id="m1.1.2.pmml" xref="m1.1.2">+</m:mo>
        <mws:qvar xmlns:mws="http://search.mathweb.org/ns" name="y"/>
      </m:mrow>
    </m:annotation-xml>
    <m:annotation encoding="application/x-tex" xml:id="m1.1c">
        x+\qvar@construct{y}
    </m:annotation>
  </m:semantics>
</m:math>
                      """

    def testConvert(self):
        results = convert_math_expression(self.mathml,
                                          terminal_symbols=True,
                                          compound_symbols=True,
                                          expand_location=True,
                                          unbounded=True,
                                          synonyms=True)
        expect = [START_TAG,
                  """#('v!x','+','n','-')""" + P_D + """-:4#""",
                  """#('v!x','+','n')""" + P_D + """-:4#""",
                  """#('*','+','n','-')""" + P_D + """-:4#""",
                  """#('*','+','n')""" + P_D + """-:4#""",
                  """#('v!x','*','n','-')""" + P_D + """-:4#""",
                  """#('v!x','*','n')""" + P_D + """-:4#""",
                  """#('+','*','n','n')""" + P_D + """n:4#""",
                  """#('+','*','n')""" + P_D + """n:4#""",
                  END_TAG]
        self.assertEqual(" ".join(expect), results)


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
                  """#('*','!0','n')""" + P_D + """b:1#""",
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
                  """#('n','a','v!k')""" + P_D + """nnnnn:8#""",
                  """#('n','n','/')""" + P_D + """nnnn:8#""",
                  """#('n','n','n!2')""" + P_D + """nnn:8#""",
                  """#('n','a','n!2')""" + P_D + """nnn:8#""",
                  """#('n','n','gt')""" + P_D + """nn:8#""",
                  """#('w','n','n!2')""" + P_D + """nw:8#""",
                  """#('w','e','n!2')""" + P_D + """nw:8#""",
                  """#('n','n','m!()1x2')""" + P_D + """n:8#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)


class TestExpandLocation(TestBase):
    def setUp(self):
        self.debug = True
        self.file = os.path.join(os.getcwd(), "testFiles", "test_wildcard.xml")
        self.mathml = self.loadFile(self.file)

    def tearDown(self):
        pass

    def testExpandLocation(self):
        file = os.path.join(os.getcwd(), "testFiles", "test_edge_pair.xml")
        mathml = self.loadFile(file)
        results = convert_math_expression(mathml,
                                          expand_location=True)
        expect = [START_TAG,
                  """#('v!w','m!()1x2','n','-')""" + P_D + """-:10#""",
                  """#('v!w','m!()1x2','n')""" + P_D + """-:10#""",
                  """#('m!()1x2','gt','n','n')""" + P_D + """n:10#""",
                  """#('m!()1x2','gt','n')""" + P_D + """n:10#""",
                  """#('gt','n!2','n','nn')""" + P_D + """nn:10#""",
                  """#('gt','n!2','n')""" + P_D + """nn:10#""",
                  """#('n!2','/','n','nnn')""" + P_D + """nnn:10#""",
                  """#('n!2','/','n')""" + P_D + """nnn:10#""",
                  """#('/','v!k','n','nnnn')""" + P_D + """nnnn:10#""",
                  """#('/','v!k','n')""" + P_D + """nnnn:10#""",
                  """#('v!k','v!ε','a','nnnnn')""" + P_D + """nnnnn:10#""",
                  """#('v!k','v!ε','a')""" + P_D + """nnnnn:10#""",
                  """#('n!2','v!k','a','nnn')""" + P_D + """nnn:10#""",
                  """#('n!2','v!k','a')""" + P_D + """nnn:10#""",
                  """#('m!()1x2','n!2','w','n')""" + P_D + """n:10#""",
                  """#('m!()1x2','n!2','w')""" + P_D + """n:10#""",
                  """#('n!2','comma','n','nw')""" + P_D + """nw:10#""",
                  """#('n!2','comma','n')""" + P_D + """nw:10#""",
                  """#('n!2','v!k','e','nw')""" + P_D + """nw:10#""",
                  """#('n!2','v!k','e')""" + P_D + """nw:10#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testExpandLocation2(self):
        nodes_list = [('m!()1x1', '?w', 'n', "-")]
        result = expand_nodes_with_location(nodes_list, 6)
        self.assertEquals(len(result), 2)
        expect = [(('m!()1x1', '?w', 'n', '-'), '-:6'),
                  (('m!()1x1', '?w', 'n'), '-:6')]
        self.assertEquals(result, expect)


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
                  """#('v!t','*','b')""" + P_D + """-:2#""",
                  """#('*','!0','n')""" + P_D + """b:2#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testConvertAll(self):
        results = convert_math_expression(self.mathml,
                                          terminal_symbols=True,
                                          compound_symbols=True,
                                          edge_pairs=True)
        expect = [START_TAG,
                  """#('v!t','*','b')""" + P_D + """-:2#""",
                  """#('*','!0')""" + P_D + """b:2#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)


class TestDetermineNode(TestBase):
    def setUp(self):
        self.debug = True

    def tearDown(self):
        pass

    def testDetermineNode(self):
        self.assertEqual(determine_node(('m!()1x1', '?w', 'n', "-")),
                         SYMBOL_PAIR_NODE)
        self.assertEqual(determine_node(('m!()1x1', '?w', "-")),
                         SYMBOL_PAIR_NODE)
        self.assertEqual(determine_node(('n!0', '!0', "-")),
                         TERMINAL_NODE)
        self.assertEqual(determine_node(('n!0', '!0', "n", "-")),
                         EOL_NODE)
        self.assertEqual(determine_node(('v!α', "['n', 'b']", "-")),
                         COMPOUND_NODE)
        self.assertEqual(determine_node(('n', 'b', 'v!y', "-")),
                         EDGE_PAIR_NODE)
        self.assertEqual(determine_node(('n', 'n', 'v!y', "-")),
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
        expect = [('m!()1x1', 'n!1', 'n', "-"),
                  (WILDCARD_MOCK, 'n!1', 'n', "-"),
                  ('m!()1x1', WILDCARD_MOCK, 'n', "-"),
                  ]
        result = expand_node_with_wildcards(('m!()1x1', 'n!1', 'n', "-"))
        self.log(result)
        self.assertEquals(result, expect)
        # terminal symbol
        result = expand_node_with_wildcards(('n!0', '!0', "-"))
        self.log(result)
        expect = [('n!0', '!0', "-")]
        self.assertEquals(result, expect)
        # eol symbol
        result = expand_node_with_wildcards(('n!0', '!0', "n", "-"))
        expect = [('n!0', '!0', "n", "-")]
        self.log(result)
        self.assertEquals(result, expect)
        # compound symbol
        expect = [('v!α', "['n', 'b']", "-"),
                  (WILDCARD_MOCK, "['n', 'b']", "-")]
        result = expand_node_with_wildcards(('v!α', "['n', 'b']", "-"))
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
                  """#('*',"['n','b']")""" + P_D + """-:9#""",
                  """#('*','=','n')""" + P_D + """-:9#""",
                  """#('=','n!1','n')""" + P_D + """n:9#""",
                  """#('*','n!1','n')""" + P_D + """n:9#""",
                  """#('=','*','n')""" + P_D + """n:9#""",
                  """#('n!1','!0')""" + P_D + """nn:9#""",
                  """#('n','n','=')""" + P_D + """n:9#""",
                  """#('n','n','*')""" + P_D + """n:9#""",
                  """#('b','n','*')""" + P_D + """b:9#""",
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
                  """#('v!α',"['n','b']")""" + P_D + """-:36#""",
                  """#('*',"['n','b']")""" + P_D + """-:36#""",
                  """#('v!α','m!()1x1','n')""" + P_D + """-:36#""",
                  """#('*','m!()1x1','n')""" + P_D + """-:36#""",
                  """#('v!α','*','n')""" + P_D + """-:36#""",
                  """#('m!()1x1',"['n','w']")""" + P_D + """n:36#""",
                  """#('*',"['n','w']")""" + P_D + """n:36#""",
                  """#('m!()1x1','=','n')""" + P_D + """n:36#""",
                  """#('*','=','n')""" + P_D + """n:36#""",
                  """#('m!()1x1','*','n')""" + P_D + """n:36#""",
                  """#('=','v!y','n')""" + P_D + """nn:36#""",
                  """#('*','v!y','n')""" + P_D + """nn:36#""",
                  """#('=','*','n')""" + P_D + """nn:36#""",
                  """#('v!y','n!0','b')""" + P_D + """nnn:36#""",
                  """#('*','n!0','b')""" + P_D + """nnn:36#""",
                  """#('v!y','*','b')""" + P_D + """nnn:36#""",
                  """#('n!0','!0')""" + P_D + """nnnb:36#""",
                  """#('n','b','v!y')""" + P_D + """nnn:36#""",
                  """#('n','b','*')""" + P_D + """nnn:36#""",
                  """#('n','n','=')""" + P_D + """nn:36#""",
                  """#('n','n','*')""" + P_D + """nn:36#""",
                  """#('m!()1x1','v!x','w')""" + P_D + """n:36#""",
                  """#('*','v!x','w')""" + P_D + """n:36#""",
                  """#('m!()1x1','*','w')""" + P_D + """n:36#""",
                  """#('v!x','n!0','b')""" + P_D + """nw:36#""",
                  """#('*','n!0','b')""" + P_D + """nw:36#""",
                  """#('v!x','*','b')""" + P_D + """nw:36#""",
                  """#('n!0','!0')""" + P_D + """nwb:36#""",
                  """#('w','b','v!x')""" + P_D + """nw:36#""",
                  """#('w','b','*')""" + P_D + """nw:36#""",
                  """#('n','n','m!()1x1')""" + P_D + """n:36#""",
                  """#('n','n','*')""" + P_D + """n:36#""",
                  """#('v!α','n!0','b')""" + P_D + """-:36#""",
                  """#('*','n!0','b')""" + P_D + """-:36#""",
                  """#('v!α','*','b')""" + P_D + """-:36#""",
                  """#('n!0','!0')""" + P_D + """b:36#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)


class TestWildcardReductionAndCheck(TestBase):
    def setUp(self):
        self.debug = True

    def tearDown(self):
        pass

    def testCheckNode(self):
        valid_nodes = [('?w', 'm!()1x1', 'n', '-'),
                       ('m!()1x1', '?w', 'n', '-'),
                       ('=', 'v!y', 'n', '-'),
                       ('v!y', 'n!0', 'b', '-'),
                       ('m!()1x1', 'v!x', 'w', '-'),
                       ('v!x', 'n!0', 'b', '-'),
                       ('v!Î±', 'n!0', 'b', '-'),
                       ('v!Î±', "['n','b']", '-'),
                       ('m!()1x1', "['n','w']", '-'),
                       ('n!0', '!0', '-'),
                       ('n', 'b', '?w', '-'),
                       ('n', 'n', '=', '-'),
                       ('w', 'b', 'v!x', '-'),
                       ('n', 'n', 'm!()1x1', '-'),
                       ('n', 'w', 'm!()1x1', '-')]
        invalid_nodes = [('?v', '!0', '-'),
                         ('?w', '?w', 'b', '-'),
                         ("?w", "?w", "b", "nn", '-'),
                         ("?w", "!0", "n", '-')]
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
                  """#('v!𝖿𝗏','!0')""" + P_D + """-:1#""",
                  END_TAG]
        self.assertEqual(results, " ".join(expect))

    def testF2(self):
        self.mathml = self.loadFile(self.f2)
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        self.log(results)
        expect = [START_TAG,
                  """#('v!𝒔','!0')""" + P_D + """-:1#""",
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
                  """#('*','=','n')""" + P_D + """-:2#""",
                  """#('=','n!1','n')""" + P_D + """n:2#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testWindowSize(self):
        results = convert_math_expression(self.mathml, window_size=2)
        expect = [START_TAG,
                  """#('*','=','n')""" + P_D + """-:3#""",
                  """#('*','n!1','nn')""" + P_D + """-:3#""",
                  """#('=','n!1','n')""" + P_D + """n:3#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEOL(self):
        # height too big
        results = convert_math_expression(self.mathml, eol=True)
        expect = [START_TAG,
                  """#('*','=','n')""" + P_D + """-:2#""",
                  """#('=','n!1','n')""" + P_D + """n:2#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testCompoundSymbols(self):
        results = convert_math_expression(self.mathml, compound_symbols=True)
        expect = [START_TAG,
                  """#('*',"['n','b']")""" + P_D + """-:3#""",
                  """#('*','=','n')""" + P_D + """-:3#""",
                  """#('=','n!1','n')""" + P_D + """n:3#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testTerminalSymbols(self):
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        expect = [START_TAG,
                  """#('*','=','n')""" + P_D + """-:3#""",
                  """#('=','n!1','n')""" + P_D + """n:3#""",
                  """#('n!1','!0')""" + P_D + """nn:3#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEdgePairs(self):
        results = convert_math_expression(self.mathml, edge_pairs=True)
        expect = [START_TAG,
                  """#('*','=','n')""" + P_D + """-:4#""",
                  """#('=','n!1','n')""" + P_D + """n:4#""",
                  """#('n','n','=')""" + P_D + """n:4#""",
                  """#('b','n','*')""" + P_D + """b:4#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testUnbounded(self):
        results = convert_math_expression(self.mathml, unbounded=True)
        expect = [START_TAG,
                  """#('*','=','n')""" + P_D + """-:2#""",
                  """#('=','n!1','n')""" + P_D + """n:2#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testLocation(self):
        results = convert_math_expression(self.mathml, location=True)
        expect = [START_TAG,
                  """#('*','=','n','-')""" + P_D + """-:2#""",
                  """#('=','n!1','n','n')""" + P_D + """n:2#""",
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
                  """#('v!α','m!()1x1','n')""" + P_D + """-:7#""",
                  """#('m!()1x1','=','n')""" + P_D + """n:7#""",
                  """#('=','v!y','n')""" + P_D + """nn:7#""",
                  """#('v!y','n!0','b')""" + P_D + """nnn:7#""",
                  """#('m!()1x1','v!x','w')""" + P_D + """n:7#""",
                  """#('v!x','n!0','b')""" + P_D + """nw:7#""",
                  """#('v!α','n!0','b')""" + P_D + """-:7#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testWindowSize(self):
        results = convert_math_expression(self.mathml, window_size=2)
        expect = [START_TAG,
                  """#('v!α','m!()1x1','n')""" + P_D + """-:12#""",
                  """#('v!α','v!x','nw')""" + P_D + """-:12#""",
                  """#('v!α','=','nn')""" + P_D + """-:12#""",
                  """#('m!()1x1','=','n')""" + P_D + """n:12#""",
                  """#('m!()1x1','v!y','nn')""" + P_D + """n:12#""",
                  """#('=','v!y','n')""" + P_D + """nn:12#""",
                  """#('=','n!0','nb')""" + P_D + """nn:12#""",
                  """#('v!y','n!0','b')""" + P_D + """nnn:12#""",
                  """#('m!()1x1','v!x','w')""" + P_D + """n:12#""",
                  """#('m!()1x1','n!0','wb')""" + P_D + """n:12#""",
                  """#('v!x','n!0','b')""" + P_D + """nw:12#""",
                  """#('v!α','n!0','b')""" + P_D + """-:12#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEOL(self):
        # height too big
        results = convert_math_expression(self.mathml, eol=True)
        expect = [START_TAG,
                  """#('v!α','m!()1x1','n')""" + P_D + """-:7#""",
                  """#('m!()1x1','=','n')""" + P_D + """n:7#""",
                  """#('=','v!y','n')""" + P_D + """nn:7#""",
                  """#('v!y','n!0','b')""" + P_D + """nnn:7#""",
                  """#('m!()1x1','v!x','w')""" + P_D + """n:7#""",
                  """#('v!x','n!0','b')""" + P_D + """nw:7#""",
                  """#('v!α','n!0','b')""" + P_D + """-:7#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testCompoundSymbols(self):
        results = convert_math_expression(self.mathml, compound_symbols=True)
        expect = [START_TAG,
                  """#('v!α',"['n','b']")""" + P_D + """-:9#""",
                  """#('v!α','m!()1x1','n')""" + P_D + """-:9#""",
                  """#('m!()1x1',"['n','w']")""" + P_D + """n:9#""",
                  """#('m!()1x1','=','n')""" + P_D + """n:9#""",
                  """#('=','v!y','n')""" + P_D + """nn:9#""",
                  """#('v!y','n!0','b')""" + P_D + """nnn:9#""",
                  """#('m!()1x1','v!x','w')""" + P_D + """n:9#""",
                  """#('v!x','n!0','b')""" + P_D + """nw:9#""",
                  """#('v!α','n!0','b')""" + P_D + """-:9#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testTerminalSymbols(self):
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        expect = [START_TAG,
                  """#('v!α','m!()1x1','n')""" + P_D + """-:10#""",
                  """#('m!()1x1','=','n')""" + P_D + """n:10#""",
                  """#('=','v!y','n')""" + P_D + """nn:10#""",
                  """#('v!y','n!0','b')""" + P_D + """nnn:10#""",
                  """#('n!0','!0')""" + P_D + """nnnb:10#""",
                  """#('m!()1x1','v!x','w')""" + P_D + """n:10#""",
                  """#('v!x','n!0','b')""" + P_D + """nw:10#""",
                  """#('n!0','!0')""" + P_D + """nwb:10#""",
                  """#('v!α','n!0','b')""" + P_D + """-:10#""",
                  """#('n!0','!0')""" + P_D + """b:10#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEdgePairs(self):
        results = convert_math_expression(self.mathml, edge_pairs=True)
        expect = [START_TAG,
                  """#('v!α','m!()1x1','n')""" + P_D + """-:11#""",
                  """#('m!()1x1','=','n')""" + P_D + """n:11#""",
                  """#('=','v!y','n')""" + P_D + """nn:11#""",
                  """#('v!y','n!0','b')""" + P_D + """nnn:11#""",
                  """#('n','b','v!y')""" + P_D + """nnn:11#""",
                  """#('n','n','=')""" + P_D + """nn:11#""",
                  """#('m!()1x1','v!x','w')""" + P_D + """n:11#""",
                  """#('v!x','n!0','b')""" + P_D + """nw:11#""",
                  """#('w','b','v!x')""" + P_D + """nw:11#""",
                  """#('n','n','m!()1x1')""" + P_D + """n:11#""",
                  """#('v!α','n!0','b')""" + P_D + """-:11#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testUnbounded(self):
        results = convert_math_expression(self.mathml, unbounded=True)
        expect = [START_TAG,
                  """#('v!α','m!()1x1','n')""" + P_D + """-:16#""",
                  """#('v!α','v!x')""" + P_D + """-:16#""",
                  """#('v!α','n!0')""" + P_D + """-:16#""",
                  """#('v!α','=')""" + P_D + """-:16#""",
                  """#('v!α','v!y')""" + P_D + """-:16#""",
                  """#('v!α','n!0')""" + P_D + """-:16#""",
                  """#('m!()1x1','=','n')""" + P_D + """n:16#""",
                  """#('m!()1x1','v!y')""" + P_D + """n:16#""",
                  """#('m!()1x1','n!0')""" + P_D + """n:16#""",
                  """#('=','v!y','n')""" + P_D + """nn:16#""",
                  """#('=','n!0')""" + P_D + """nn:16#""",
                  """#('v!y','n!0','b')""" + P_D + """nnn:16#""",
                  """#('m!()1x1','v!x','w')""" + P_D + """n:16#""",
                  """#('m!()1x1','n!0')""" + P_D + """n:16#""",
                  """#('v!x','n!0','b')""" + P_D + """nw:16#""",
                  """#('v!α','n!0','b')""" + P_D + """-:16#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testLocation(self):
        results = convert_math_expression(self.mathml, location=True)
        expect = [START_TAG,
                  """#('v!α','m!()1x1','n','-')""" + P_D + """-:7#""",
                  """#('m!()1x1','=','n','n')""" + P_D + """n:7#""",
                  """#('=','v!y','n','nn')""" + P_D + """nn:7#""",
                  """#('v!y','n!0','b','nnn')""" + P_D + """nnn:7#""",
                  """#('m!()1x1','v!x','w','n')""" + P_D + """n:7#""",
                  """#('v!x','n!0','b','nw')""" + P_D + """nw:7#""",
                  """#('v!α','n!0','b','-')""" + P_D + """-:7#""",
                  END_TAG]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
