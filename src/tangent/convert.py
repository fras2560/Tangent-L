import argparse
import logging
import sys
import os
try:
    from math_extractor import MathExtractor
    from mathdocument import MathDocument
    from htmlStriper import strip_tags
except ImportError:
    from tangent.math_extractor import MathExtractor
    from tangent.mathdocument import MathDocument
    from tangent.htmlStriper import strip_tags
PAYLOAD_DELIMITER = "|__|"
PAYLOAD_SEPARATOR = ":"
START_TAG = "#(start)#"
END_TAG = "#(end)#"
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
                            synonyms=False,
                            no_payload=False,
                            expand_location=False):
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
    if tree_root is not None:
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
                                    shortened=shortened)
        if not synonyms:
            node_list = [node
                         for node in pairs
                         if check_node(node)]
        else:
            # loop through all kept nodes and their expanded nodes
            node_list = [expanded_node
                         for node in pairs
                         if check_node(node)
                         for expanded_node in expand_node_with_wildcards(node)
                         ]
        # create a list of nodes and their payloads
        formula_size = len(node_list)
        # do we want to expand with location
        if expand_location:
            nodes_payloads = expand_nodes_with_location(node_list,
                                                        formula_size)
        else:
            # remove the location if not wanted and create payloads
            nodes_payloads = [pop_location(node, formula_size, location)
                              for node in node_list]
        if no_payload:
            node_list = [format_node(node[0]) for node in nodes_payloads]
        else:
            # now format the nodes
            node_list = [format_node(node[0], payload=node[1])
                         for node in nodes_payloads]
        # add start and end strings
        node_list = [START_TAG] + node_list + [END_TAG]
        return " ".join(node_list)
    else:
        return ""


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
        if len(node) == 3:
            # if one is a wildcard the dont want to keep it
            check = not(check_wildcard(node[0]) or check_wildcard(node[1]))
        else:
            # then both need to be a wildcard
            check = not(check_wildcard(node[0]) and check_wildcard(node[1]))
    elif node_type == EDGE_PAIR_NODE or node_type == COMPOUND_NODE:
        # keep them regardless at this point
        pass
    return check


def expand_nodes_with_location(nodes, formula_size):
    """Returns a list of nodes where each tuple is expand to two tuples
        one with its location and one without its location

    Parameters:
        nodes: the list of nodes
        formula_size: the size of the formula
    Returns:
        result: the list of nodes after expansion
    """
    result = []
    for node in nodes:
        location = node[-1]
        # add the first node
        result.append((node, location + PAYLOAD_SEPARATOR + str(formula_size)))
        # add the second node
        temp_node = list(node)
        temp_node.pop()
        temp_node = tuple(temp_node)
        result.append((temp_node,
                      location + PAYLOAD_SEPARATOR + str(formula_size)))
    return result


def pop_location(node, formula_size, include_location):
    """Returns the node along with the payload

    Parameters:
        node: the math tuple
        formula_size: the total size of the math formula
        include_location: whether to include the location in the tuple
    Returns:
        : a tuple with (node, payload)
    """
    location = node[-1]
    if not include_location:
        # need to remove the location from the tuple
        node = list(node)
        node.pop()
        node = tuple(node)
    return (node, location + PAYLOAD_SEPARATOR + str(formula_size))


def check_wildcard(term):
    """Returns True if term is a wildcard term
    """
    wildcard = True
    letters = "zxcvbnmasdfghjklqwertyuiop"
    letter = 0
    while letter < len(letters) and term.lower() != "?" + letters[letter]:
        letter += 1
    if letter == len(letters):
        # got to the end so not a wild card
        wildcard = False
    return wildcard


def format_node(node, payload=None):
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
    if payload is not None:
        node = node + PAYLOAD_DELIMITER + payload
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
    """Returns the type of node
    """
    node_type = SYMBOL_PAIR_NODE
    if node[1] == "!0":
        if len(node) == 2:
            node_type = TERMINAL_NODE
        elif node[2] == "n":
            node_type = EOL_NODE
        else:
            node_type = TERMINAL_NODE
    elif ("[" in node[1] and "]" in node[1]) or isinstance(node[1], list):
        node_type = COMPOUND_NODE
    elif node[0] in EDGES:
        node_type = EDGE_PAIR_NODE
    return node_type


def expand_node_with_wildcards(node):
    """Returns a list of nodes that includes the expanded nodes
    """
    results = [node]
    node_type = determine_node(node)
    if node_type == SYMBOL_PAIR_NODE:
        # if just two symbols (no path) then no point in expanding
        if len(node) > 3:
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
        if (not check_wildcard(node[-2])):
            temp = list(node)
            temp[-2] = WILDCARD_MOCK
            results.append(tuple(temp))
    elif node_type == TERMINAL_NODE or EOL_NODE:
        # no expansion for them
        pass
    return results


def format_paragraph(paragraph, query):
    """Returns a formatted paragraph

    Parameters:
        paragraph: the string paragraph to convert (str)
        query: a boolea that says whether it a query file or not (boolean)
    Returns:
        striped: the formated paragraph
    """
    striped = paragraph
    if not query:
        striped = strip_tags(paragraph)
    return striped


def convert_file_to_words(filename,
                          window_size=1,
                          symbol_pairs=True,
                          eol=False,
                          compound_symbols=False,
                          terminal_symbols=False,
                          edge_pairs=False,
                          unbounded=False,
                          shortened=True,
                          location=False,
                          synonyms=False,
                          query=False,
                          no_payload=True,
                          expand_location=False):
    """Parses a file and returns a of words
    Parameters:
        filename: the name of the file to parse
        file_id: the file id
        output_file: the name of the file to output to
    Returns:
        result: the list of words and math tuples
    """
    (ext, content) = MathDocument.read_doc_file(filename)
    tokens = ""
    while len(content) != 0:
        (start, end) = MathExtractor.next_math_token(content)
        if start == -1:
            # can just print the rest
            tokens += " " + format_paragraph(content, query)
            content = ""
        else:
            paragraph = format_paragraph(content[0:start], query)
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
                                         synonyms=synonyms,
                                         no_payload=no_payload,
                                         expand_location=expand_location)
            tokens += " " + paragraph + " " + ex
            # now move the content further along
            content = content[end:]
    return tokens


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
               synonyms=False,
               query=True,
               expand_location=False):
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
                print(format_paragraph(content, query), end="", file=out)
                content = ""
            else:
                paragraph = format_paragraph(content[0:start], query)
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
                                             synonyms=synonyms,
                                             expand_location=expand_location)
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
    parser.add_argument("-query",
                        dest="query",
                        action="store_true",
                        help="True if a query file",
                        default=False)
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
    parser.add_argument('-expand_location',
                        dest="expand_location",
                        action="store_true",
                        help="Expand tuples to include location",
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
               synonyms=args.synonyms,
               query=args.query,
               expand_location=args.expand_location
               )
    print(args)
    logger.info("Done")
