__author__ = 'Nidhin, FWTompa'


class MathML:
    """
    List of recognized tags
    """
    namespace = '{http://www.w3.org/1998/Math/MathML}'
    math = namespace + 'math'
    mn = namespace + 'mn'
    mo = namespace + 'mo'
    mi = namespace + 'mi'
    mtext = namespace + 'mtext'
    mrow = namespace + 'mrow'
    msub = namespace + 'msub'
    msup = namespace + 'msup'
    msubsup = namespace + 'msubsup'
    munderover = namespace + 'munderover'
    msqrt = namespace + 'msqrt'
    mroot = namespace + 'mroot'
    mfrac = namespace + 'mfrac'
    menclose = namespace + 'menclose'
    mfenced = namespace + 'mfenced'
    mover = namespace + 'mover'
    munder = namespace + 'munder'
    mpadded = namespace + 'mpadded'
    mphantom = namespace + 'mphantom'
    none = namespace + 'none'
    mstyle = namespace + 'mstyle'
    mspace = namespace + 'mspace'
    mtable = namespace + 'mtable'
    mtr = namespace + 'mtr'
    mtd = namespace + 'mtd'
    semantics = namespace + 'semantics'
    mmultiscripts = namespace + 'mmultiscripts'
    mprescripts = namespace + 'mprescripts'
    mqvar = '{http://search.mathweb.org/ns}qvar'
    mqvar2 = namespace + 'qvar' # for erroneous namespace
    merror = namespace + 'merror'  # To deal with Errors in MathML conversion from tools (KMD)
