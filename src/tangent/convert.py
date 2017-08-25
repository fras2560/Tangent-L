from math_extractor import MathExtractor
from mathdocument import MathDocument
import argparse
import logging
import sys


def convert_math_expression(mathml,
                            window_size=1,
                            eol=False,
                            compound_symbols=False,
                            terminal_symbols=False,
                            edge_pairs=False,
                            unbounded=False,
                            shortened=True,
                            location=False):
    """Returns the math tuples for a given math expression

    Parameters:
        mathml: the math expression (string)
    Returns:
        : a string of the math tuples
    """
    tokens = MathExtractor.math_tokens(mathml)
    pmml = MathExtractor.isolate_pmml(tokens[0])
    tree_root = MathExtractor.convert_to_mathsymbol(pmml)
    height = tree_root.get_height()
    eol_check = False
    if height <= 2:
        eol_check = eol
    pairs = tree_root.get_pairs("",
                                window_size,
                                eol=eol_check,
                                compound_symbols=compound_symbols,
                                terminal_symbols=terminal_symbols,
                                edge_pairs=edge_pairs,
                                unbounded=unbounded,
                                shortened=shortened,
                                location=location)
#     for node in pairs:
#         print("Node", node, format_node(node), type(node))
    node_list = [format_node(node)
                 for node in pairs]
    return " ".join(node_list)


def format_node(node):
    node = str(node).lower()
    node = node.replace("*", "\*")
    for letter in "zxcvbnmasdfghjklqwertyuiop":
        node = node.replace("?" + letter, "*")
    return ("#" + (str(node)
                   .replace(" ", "")
                   .replace("&comma;", "comma")
                   .replace("&lsqb;", "lsqb")
                   .replace("&rsqb;", "rsqb")
                   ) + "#")


def format_paragraph(paragraph):
    return paragraph


def parse_file(filename,
               file_id,
               output_file,
               window_size=1,
               eol=False,
               compound_symbols=False,
               terminal_symbols=False,
               edge_pairs=False,
               unbounded=False,
               shortened=True,
               location=False):
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
                paragraph = format_paragraph(content[0:start])
                expression = convert_math_expression(content[start:end],
                                                     window_size=1,
                                                     eol=False,
                                                     compound_symbols=False,
                                                     terminal_symbols=False,
                                                     edge_pairs=False,
                                                     unbounded=False,
                                                     shortened=True,
                                                     location=False)
                print(paragraph)
                print(paragraph, file=out)
                print(expression)
                print(expression, file=out)
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
    parser.add_argument('-infile',
                        '--infile',
                        help='The file to read from',
                        required=True)
    parser.add_argument('-outfile',
                        '--outfile',
                        help='The file to output to', required=True)
    parser.add_argument('-compound_symbols',
                        dest="compound_symbols",
                        action="store_true",
                        help="Use compound symbols",
                        default=False)
    parser.add_argument('-terminal_symbols',
                        dest="terminal_symbols",
                        action="store_true",
                        help="Use terminal symbols",
                        default=False)
    parser.add_argument('-edge_pairs',
                        dest="edge_pairs",
                        action="store_true",
                        help="Use edge pairs",
                        default=False)
    parser.add_argument('-unbounded',
                        dest="unbounded",
                        action="store_true",
                        help="Whether symbol pairs should be unbounded",
                        default=False)
    parser.add_argument('-shortened',
                        dest="shortened",
                        action="store_false",
                        help="Whether symbol pairs should be unbounded",
                        default=True)
    prompt = "Whether to include location or not in symbol pairs"
    parser.add_argument('-location',
                        dest="location",
                        action="store_true",
                        help=prompt,
                        default=False)
    parser.add_argument('window_size',
                        dest="window_size",
                        default=1,
                        type=int,
                        help='The size of the window',
                        nargs='?')
    args = parser.parse_args()
    infile = None
    outfile = None
    if args.infile is not None:
        infile = args.infile
    if args.outfile is not None:
        outfile = args.outfile
    logger.info(infile)
    logger.info(outfile)
    parse_file(infile,
               0,
               outfile,
               window_size=args.window_size,
               eol=args.eol,
               compound_symbols=args.compound_symbols,
               terminal_symbols=args.terminal_symbols,
               edge_pairs=args.edge_pairs,
               unbounded=args.unbounded,
               shortened=args.shortened,
               location=args.location
               )
    logger.info("Done")
