package index;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import utilities.Constants;

public class MathBagsFilter extends TokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private boolean openTag;

    public MathBagsFilter(TokenStream in){
        super(in);
        this.openTag = false;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!this.input.incrementToken()) {
            return false;
        }
        // get the current token
        final char[] token = Arrays.copyOfRange(this.termAtt.buffer(), 0, this.termAtt.length());
        String stoken = String.valueOf(token);
        if (stoken.equals(Constants.FORMULA_START_TAG)){
            this.openTag = true;
        }else if (stoken.equals(Constants.FORMULA_END_TAG)){
            this.openTag = false;
        }else if (this.openTag){
            // just put them in all the same bag
            posIncrAtt.setPositionIncrement(0);
        }
        return true;
    }
}