'''
Name: Dallas Fraser
Email: d6fraser@uwaterloo.ca
Date: 2017-07-27
Project: Tangent GT
Purpose: Tests html stripper
'''
import unittest
try:
    from htmlStriper import strip_tags
except ImportError:
    from tangent.htmlStriper import strip_tags


class TestStriper(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass

    def testStripTags(self):
        result = strip_tags("<html>Hello<html>")
        self.assertEqual(result.strip(), "Hello")
        result = strip_tags("<ol><li>Hello</li><li>baby</li></ol>")
        self.assertEqual(result.strip(), "Hello baby")

    def testMathDoc(self):
        test = """
                <div class="bibblock">
Willick, J.A., Courteau, S., Faber, S.M., Burstein, D., Dekel, A., &amp; Strauss, M.A. 1997, ApJS, 109, 333
                            </div>"""
        result = strip_tags(test)
        expect = """Willick, J.A., Courteau, S., Faber, S.M., Burstein, D., Dekel, A., & Strauss, M.A. 1997, ApJS, 109, 333"""
        self.assertEqual(result.strip(), expect)

    def testTextDoc(self):
        test = """dallas lies lies fraser"""
        self.assertEqual(strip_tags(test), test)

    def testPartHtml(self):
        test = """<p class="ltx_p" id="S2.p1.1">We first explain formulation of full conformal quantum field theory on the"""
        expect = """We first explain formulation of full conformal quantum field theory on the"""
        self.assertEqual(strip_tags(test), expect)

if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
