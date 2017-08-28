'''
Name: Dallas Fraser
Email: d6fraser@uwaterloo.ca
Date: 2017-08-25
Project: Tangent
Purpose: To test the convert of mathml to Tangent Tuples
'''
import unittest
import os
from tangent.convert import convert_math_expression


class TestBase(unittest.TestCase):
    def loadFile(self, math_file):
        with open(math_file) as f:
            lines = []
            for line in f:
                lines.append(line)
        return " ".join(lines)

    def log(self, out):
        if self.debug:
            print(out)


class TestArxivQuery(TestBase):
    def setUp(self):
        self.debug = True
        self.file = os.path.join(os.getcwd(), "testFiles", "test_1.xml")
        self.mathml = self.loadFile(self.file)

    def tearDown(self):
        pass

    def testBase(self):
        results = convert_math_expression(self.mathml)
        expect = ["#('*','=','n')#",
                  "#('=','n!1','n')#",
                  "#('*','*','b')#",
                  "#('*','*','n')#"]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testWindowSize(self):
        results = convert_math_expression(self.mathml, window_size=2)
        expect = ["#('*','=','n')#",
                  "#('*','n!1','nn')#",
                  "#('=','n!1','n')#",
                  "#('*','*','b')#",
                  "#('*','*','bn')#",
                  "#('*','*','n')#"]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEOL(self):
        # height too big
        results = convert_math_expression(self.mathml, eol=True)
        expect = ["#('*','=','n')#",
                  "#('=','n!1','n')#",
                  "#('*','*','b')#",
                  "#('*','*','n')#"]
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testCompoundSymbols(self):
        results = convert_math_expression(self.mathml, compound_symbols=True)
        expect = ['''#('*',"['n','b']")#''',
                  '''#('*','=','n')#''',
                  '''#('=',"['n']")#''',
                  '''#('=','n!1','n')#''',
                  '''#('*','*','b')#''',
                  '''#('*',"['n']")#''',
                  '''#('*','*','n')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testTerminalSymbols(self):
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        expect = ['''#('*','=','n')#''',
                  '''#('=','n!1','n')#''',
                  '''#('n!1','!0')#''',
                  '''#('*','*','b')#''',
                  '''#('*','*','n')#''',
                  '''#('*','!0')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEdgePairs(self):
        results = convert_math_expression(self.mathml, edge_pairs=True)
        expect = ['''#('*','=','n')#''',
                  '''#('=','n!1','n')#''',
                  '''#('n','n','=')#''',
                  '''#('*','*','b')#''',
                  '''#('*','*','n')#''',
                  '''#('b','n','*')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testUnbounded(self):
        results = convert_math_expression(self.mathml, unbounded=True)
        expect = ['''#('*','=','n')#''',
                  '''#('*','n!1')#''',
                  '''#('=','n!1','n')#''',
                  '''#('*','*','b')#''',
                  '''#('*','*')#''',
                  '''#('*','*','n')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testLocation(self):
        results = convert_math_expression(self.mathml, location=True)
        expect = ['''#('*','=','n','-')#''',
                  '''#('=','n!1','n','n')#''',
                  '''#('*','*','b','-')#''',
                  '''#('*','*','n','b')#''']
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
        expect = ['''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('v!α','n!0','b')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testWindowSize(self):
        results = convert_math_expression(self.mathml, window_size=2)
        expect = ['''#('v!α','m!()1x1','n')#''',
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
                  '''#('v!α','n!0','b')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEOL(self):
        # height too big
        results = convert_math_expression(self.mathml, eol=True)
        expect = ['''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('v!α','n!0','b')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testCompoundSymbols(self):
        results = convert_math_expression(self.mathml, compound_symbols=True)
        expect = ['''#('v!α',"['n','b']")#''',
                  '''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1',"['n','w']")#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=',"['n']")#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y',"['b']")#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x',"['b']")#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('v!α','n!0','b')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testTerminalSymbols(self):
        results = convert_math_expression(self.mathml, terminal_symbols=True)
        expect = ['''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('n!0','!0')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('n!0','!0')#''',
                  '''#('v!α','n!0','b')#''',
                  '''#('n!0','!0')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testEdgePairs(self):
        results = convert_math_expression(self.mathml, edge_pairs=True)
        expect = ['''#('v!α','m!()1x1','n')#''',
                  '''#('m!()1x1','=','n')#''',
                  '''#('=','v!y','n')#''',
                  '''#('v!y','n!0','b')#''',
                  '''#('n','b','v!y')#''',
                  '''#('n','n','=')#''',
                  '''#('m!()1x1','v!x','w')#''',
                  '''#('v!x','n!0','b')#''',
                  '''#('w','b','v!x')#''',
                  '''#('n','n','m!()1x1')#''',
                  '''#('n','w','m!()1x1')#''',
                  '''#('v!α','n!0','b')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testUnbounded(self):
        results = convert_math_expression(self.mathml, unbounded=True)
        expect = ['''#('v!α','m!()1x1','n')#''',
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
                  '''#('v!α','n!0','b')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

    def testLocation(self):
        results = convert_math_expression(self.mathml, location=True)
        expect = ['''#('v!α','m!()1x1','n','-')#''',
                  '''#('m!()1x1','=','n','n')#''',
                  '''#('=','v!y','n','nn')#''',
                  '''#('v!y','n!0','b','nnn')#''',
                  '''#('m!()1x1','v!x','w','n')#''',
                  '''#('v!x','n!0','b','nw')#''',
                  '''#('v!α','n!0','b','-')#''']
        self.log(results)
        self.assertEqual(" ".join(expect), results)

if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()