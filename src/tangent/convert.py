import argparse
import logging
import sys
import os
from math_extractor import MathExtractor
from mathdocument import MathDocument
TERMINAL_NODE = "terminal_node"
COMPOUND_NODE = "compound_node"
EDGE_PAIR_NODE = "edge_pair"
EOL_NODE = "eol_node"
SYMBOL_PAIR_NODE = "symbol_pair"
EDGES = ['n', 'a', 'b', 'c', 'o', 'u', 'd', 'w', 'e']
WILDCARD_MOCK = "?x"
WILDCARD = "*"
WINDOWS = "nt"
if os.name == WINDOWS:
    ENCODING = "utf-8"
else:
    ENCODING = "utf-8"


def convert_math_expression(mathml,
                            window_size=1,
                            symbol_pairs=True,
                            eol=False,
                            compound_symbols=False,
                            terminal_symbols=False,
                            edge_pairs=False,
                            unbounded=False,
                            shortened=True,
                            location=False,
                            synonyms=False):
    """Returns the math tuples for a given math expression

    Parameters:
        mathml: the math expression (string)
        (window_size): The size of the path between nodes for symbols pairs
        (symbol_pairs): True will include symbol pairs
        (eol): True will included eol nodes
        (compound_symbols): True to include compound symbols nodes
        (terminal_symbols): True to include terminal symbols nodes
        (edge_pairs): True to include edge pairs nodes
        (unbounded): True - unbounded window size
        (shortened): True - shorten symbol pair paths
        (location): True to include location for symbol pairs along with path
        (synonyms): True to expand nodes to include wildcard expansion
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
                                symbol_pairs=symbol_pairs,
                                compound_symbols=compound_symbols,
                                terminal_symbols=terminal_symbols,
                                edge_pairs=edge_pairs,
                                unbounded=unbounded,
                                shortened=shortened,
                                include_location=location)
#     for node in pairs:
#         print("Node", node, format_node(node), type(node))
    if not synonyms:
        node_list = [format_node(node)
                     for node in pairs
                     if check_node(node)]
    else:
        # loop through all kept nodes and their expanded nodes
        node_list = [format_node(expanded_node)
                     for node in pairs
                     if check_node(node)
                     for expanded_node in expand_node_with_wildcards(node)
                     ]
    return " ".join(node_list)


def check_node(node):
    """Returns False if the node is not needed
    """
    node_type = determine_node(node)
    check = True
    if node_type == EOL_NODE or node_type == TERMINAL_NODE:
        # only need to look at first part
        check = not check_wildcard(node[0])
    elif node_type == SYMBOL_PAIR_NODE:
        # does it make sense to keep pairs of symbols with no path
        # if one of those symbols is a wildcard
        if len(node) == 2:
            # if one is a wildcard the dont want to keep it
            check = not(check_wildcard(node[0]) or check_wildcard(node[1]))
        else:
            check = not(check_wildcard(node[0]) and check_wildcard(node[1]))
    elif node_type == EDGE_PAIR_NODE or node_type == COMPOUND_NODE:
        # keep them regardless at this point
        pass
    return check


def check_wildcard(term):
    """Returns True if term is a wildcard term"""
    wildcard = True
    letters = "zxcvbnmasdfghjklqwertyuiop"
    letter = 0
    while letter < len(letters) and term.lower() != "?" + letters[letter]:
        letter += 1
    if letter == len(letters):
        # got to the end so not a wild card
        wildcard = False
    return wildcard


def format_node(node):
    """Returns the formatted node
    """
    new_node = []
    for part in node:
        new_node.append(part)
        if "*" in part:
            new_node[-1] = "/*"
        if "?" in part:
            new_node[-1] = WILDCARD
    node = tuple(new_node)
    node = str(node).lower()
    return ("#" + (str(node)
                   .replace(" ", "")
                   .replace("&comma;", "comma")
                   .replace("&lsqb;", "lsqb")
                   .replace("&rsqb;", "rsqb")
                   .replace("&quest;", "quest")
                   .replace("'>'", "'gt'")
                   .replace("'<'", "'lt'")
                   ) + "#")


def determine_node(node):
    """Returns the type of node"""
    node_type = SYMBOL_PAIR_NODE
    if node[1] == "!0":
        if len(node) == 2:
            node_type = TERMINAL_NODE
        else:
            node_type = EOL_NODE
    elif ("[" in node[1] and "]" in node[1]) or isinstance(node[1], list):
        node_type = COMPOUND_NODE
    elif node[0] in EDGES:
        node_type = EDGE_PAIR_NODE
    return node_type


def expand_node_with_wildcards(node):
    """Returns a list of nodes that includes the expanded nodes"""
    results = [node]
    node_type = determine_node(node)
    if node_type == SYMBOL_PAIR_NODE:
        # if just two symbols (no path) then no point in expanding
        if len(node) > 2:
            # expands to two nodes
            # one with first tag as wc and second tag as wc
            temp = list(node)
            remember = temp[0]
            if (not check_wildcard(remember) and not check_wildcard(temp[1])):
                temp[0] = WILDCARD_MOCK
                results.append(tuple(temp))
            # now do the second node
            if (not check_wildcard(remember) and not check_wildcard(temp[1])):
                temp[0] = remember
                temp[1] = WILDCARD_MOCK
                results.append(tuple(temp))
    elif node_type == COMPOUND_NODE:
        # add an expansion of the compound node
        # the node tag is replaced with a wildcard
        if (not check_wildcard(node[0])):
            temp = list(node)
            temp[0] = WILDCARD_MOCK
            results.append(tuple(temp))
    elif node_type == EDGE_PAIR_NODE:
        # replace tag with a wildcard
        if (not check_wildcard(node[-1])):
            temp = list(node)
            temp[-1] = WILDCARD_MOCK
            results.append(tuple(temp))
    elif node_type == TERMINAL_NODE or EOL_NODE:
        # no expansion for them
        pass
    return results


def format_paragraph(paragraph):
    """Returns a formatted paragraph"""
    return paragraph


def parse_file(filename,
               file_id,
               output_file,
               window_size=1,
               symbol_pairs=True,
               eol=False,
               compound_symbols=False,
               terminal_symbols=False,
               edge_pairs=False,
               unbounded=False,
               shortened=True,
               location=False,
               synonyms=False):
    """Parses a file and ouputs to a file with math tuples

    Parameters:
        filename: the name of the file to parse
        file_id: the file id
        output_file: the name of the file to output to
    """

    (ext, content) = MathDocument.read_doc_file(filename)
    with open(output_file, "w+", encoding=ENCODING) as out:
        while len(content) != 0:
            (start, end) = MathExtractor.next_math_token(content)
            if start == -1:
                # can just print the rest
                print(content, end="", file=out)
                content = ""
            else:
                paragraph = format_paragraph(content[0:start])
                ex = convert_math_expression(content[start:end],
                                             window_size=1,
                                             symbol_pairs=symbol_pairs,
                                             eol=eol,
                                             compound_symbols=compound_symbols,
                                             terminal_symbols=terminal_symbols,
                                             edge_pairs=edge_pairs,
                                             unbounded=unbounded,
                                             shortened=shortened,
                                             location=location,
                                             synonyms=synonyms)
                print(paragraph, file=out)
                print(ex, file=out)
                # now move the content further along
                content = content[end:]


if __name__ == "__main__":
    logging.basicConfig(filename="convert.log",
                        level=logging.INFO,
                        format='%(asctime)s %(message)s')
    logger = logging.getLogger(__name__)
    descp = "Convert - MathML file to file with Tangent Tuples"
    parser = argparse.ArgumentParser(description=descp)
    parser.add_argument('-infile',
                        '--infile',
                        help='The file to read from',
                        required=True)
    parser.add_argument('-outfile',
                        '--outfile',
                        help='The file to output to',
                        required=True)
    parser.add_argument("-symbol_pairs",
                        dest="symbol_pairs",
                        action="store_false",
                        help="Do not use symbol pairs",
                        default=True)
    parser.add_argument('-eol',
                        dest="eol",
                        action="store_true",
                        help="Use EOL tuples",
                        default=False)
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
    parser.add_argument('-synonyms',
                        dest="synonyms",
                        action="store_true",
                        help="Whether to expand nodes to include synonyms",
                        default=False)
    prompt = "Whether to include location or not in symbol pairs"
    parser.add_argument('-location',
                        dest="location",
                        action="store_true",
                        help=prompt,
                        default=False)
    parser.add_argument('-window_size',
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
               symbol_pairs=args.symbol_pairs,
               eol=args.eol,
               compound_symbols=args.compound_symbols,
               terminal_symbols=args.terminal_symbols,
               edge_pairs=args.edge_pairs,
               unbounded=args.unbounded,
               shortened=args.shortened,
               location=args.location,
               synonyms=args.synonyms
               )
    logger.info("Done")
