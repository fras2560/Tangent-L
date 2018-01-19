'''
Created on Jan 17, 2018

@author: d6fraser
'''
import unittest
import argparse
import os
import shutil
try:
    from math_extractor import MathExtractor
    from mathdocument import MathDocument
except ImportError:
    from tangent.math_extractor import MathExtractor
    from tangent.mathdocument import MathDocument


def compare_doc_id(document_id, doc_id):
    """Returns True if the document id is the same as parsed from mathml
    """
    return doc_id in document_id


def parse_formula_id(document_id, mathml):
    """Returns the formula id for the mathml equation
    """
    index = mathml.index('id="math.')
    formula_id = "?"
    if(index != -1):
        # find document id
        start = index + len('id="math.')
        end = start
        while "." not in mathml[start:end+1]:
            end += 1
        doc_id = mathml[start:end]
        if compare_doc_id(document_id, doc_id):
            # check if document id and one find are equivalent
            # now find formula id
            start = end + 1
            end = start
            while mathml[end].isdigit():
                end += 1
            formula_id = mathml[start:end]
        else:
            print("FAILED PARSING FORMULA ID: " + document_id)
            print(mathml)
    else:
        print("FAILED PARSING FORMULA ID: " + document_id)
        print(mathml)
    return formula_id


def save_formulas(file, directory):
    """Saves the formulas from the file in their seperate files in the directory
    """
    (__, content) = MathDocument.read_doc_file(file)
    while len(content) != 0:
        (start, end) = MathExtractor.next_math_token(content)
        if(start != -1):
            file_name = os.path.splitext(os.path.basename(file))[0]
            ext = os.path.splitext(os.path.basename(file))[1]
            formula_id = parse_formula_id(file_name, content[start:end])
            formula_path = os.path.join(directory,
                                        file_name + "-" + formula_id + ext)
            with open(formula_path, "w+") as f:
                print(content[start:end], file=f)
            # move to the next equation
            content = content[end:]
        else:
            content = ""
    return


def parse_directory(directory, move_to):
    for subdir, __, files in os.walk(directory):
        for file in files:
            print(file)
            save_formulas(os.path.join(subdir, file), move_to)


class Test(unittest.TestCase):
    def setUp(self):
        self.test_folder = os.path.join(os.getcwd(), "testFiles", "temp")
        try:
            shutil.rmtree(self.test_folder)
        except:
            pass
        os.makedirs(self.test_folder)

    def tearDown(self):
        shutil.rmtree(self.test_folder)

    def testSaveFormulas(self):
        save_formulas(os.path.join(os.getcwd(),
                                   "testFiles",
                                   "02459.html"), self.test_folder)
        for __, __, files in os.walk(self.test_folder):
            self.assertEqual(15, len(files))

    def testParseFormulaId(self):
        mathml = '''<math  id="math.2459.12." '''
        self.assertEqual(parse_formula_id("02459", mathml), "12")
        mathml = '''<math xmlns="http://www.w3.org/1998/Math/MathML"
                    id="math.2459.0"'''
        self.assertEqual(parse_formula_id("02459", mathml), "0")

    def testCompareDocumentId(self):
        self.assertEqual(compare_doc_id("02459", "2459"), True)
        self.assertEqual(compare_doc_id("02457", "2459"), False)
        self.assertEqual(compare_doc_id("02459.html", "2459"), True)


if __name__ == "__main__":
    descp = "Parse out formulas to their own files"
    parser = argparse.ArgumentParser(description=descp)
    parser.add_argument('-in_directory',
                        '--in_directory',
                        help='The folder with the documents',
                        required=True)
    parser.add_argument('-out_directory',
                        '--out_directory',
                        help='The folder to output the formula files',
                        required=True)
    args = parser.parse_args()
    parse_directory(args.in_directory, args.out_directory)
