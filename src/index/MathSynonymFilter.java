package index;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import utilities.Constants;
/**
 * Filters all math synonyms to be at the same position
 * 
 * @author Dallas Fraser
 * @since 2017-11-06
 *
 */
public class MathSynonymFilter extends TokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private boolean mathTag = true;
    /**
     * Class Constructor
     * @param in the token stream
     */
    public MathSynonymFilter(TokenStream in){
        super(in);
        this.mathTag = true;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!this.input.incrementToken()) {
            return false;
        }
        // get the current token
        final char[] token = Arrays.copyOfRange(this.termAtt.buffer(), 0, this.termAtt.length());
        String stoken = String.valueOf(token);
        if (stoken.contains(Constants.WILDCARD) && !stoken.contains(Constants.ESCAPED_WILDCARD)){
            posIncrAtt.setPositionIncrement(0);
        }
        return mathTag;
    }
}
