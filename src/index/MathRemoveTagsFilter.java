package index;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import utilities.Constants;

public class MathRemoveTagsFilter extends FilteringTokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public MathRemoveTagsFilter(TokenStream in){
        super(in);
    }

    @Override
    protected boolean accept() throws IOException {
        // TODO Auto-generated method stub
     // get the current token
        final char[] token = Arrays.copyOfRange(this.termAtt.buffer(), 0, this.termAtt.length());
        String stoken = String.valueOf(token);
        boolean keep = true;
        if (stoken.equals(Constants.FORMULA_START_TAG) || stoken.equals(Constants.FORMULA_END_TAG)){
            keep = false;
        }
        System.out.println("Token:" + stoken + "  Keep:" + keep);
        return keep;
    }
}
