from math_extractor import MathExtractor
from mathdocument import MathDocument
import argparse
import logging
import sys

def convert_math_expression(mathml):
    """Returns the math tuples for a given math expression

    Parameters:
        mathml: the math expression (string)
    Returns:
        : a string of the math tuples
    """
    print(mathml)
    tokens = MathExtractor.math_tokens(mathml)
    pmml = MathExtractor.isolate_pmml(tokens[0])
    tree_root = MathExtractor.convert_to_mathsymbol(pmml)
    node_list = [str(node).replace(" ", "")
                 for node in tree_root.get_pairs("", 1)]
    return " ".join(node_list)

def parse_file(filename, file_id, output_file):
    """Parses a file and ouputs to a file with math tuples

    Parameters:
        filename: the name of the file to parse
        file_id: the file id
        output_file: the name of the file to output to
    """

    (ext, content) = MathDocument.read_doc_file(filename)
    with open(output_file, "w+") as out:
        while len(content) != 0:
            (start, end) = MathExtractor.next_math_token(content)
            print(start, end)
            if start == -1:
                # can just print the rest
                print(content)
                print(content, file=out)
                content = ""
            else:
                print(content[0:start])
                print(content[0:start], file=out)
                print(convert_math_expression(content[start:end]))
                print(convert_math_expression(content[start:end]), file=out)
                # now move the content further along
                content = content[end:]


if __name__ == "__main__":
    logging.basicConfig(filename="convert.log",
                        level=logging.INFO,
                        format='%(asctime)s %(message)s')
    logger = logging.getLogger(__name__)
    print(sys.argv)
    descp = "Convert - MathML file to file with Tangent Tuples"
    parser = argparse.ArgumentParser(description=descp)
    parser.add_argument('-infile','--infile', help='The file to read from', required=True)
    parser.add_argument('-outfile','--outfile', help='The file to output to', required=True)
    args = parser.parse_args()
    infile = None
    outfile = None
    if args.infile is not None:
        infile = args.infile
    if args.outfile is not None:
        outfile = args.outfile
    logger.info(infile)
    logger.info(outfile)
    parse_file(infile, 0, outfile)
    logger.info("Done")
