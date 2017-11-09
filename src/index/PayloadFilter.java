package index;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

import utilities.Constants;
/**
 * Filter adds a payload to appropriate terms
 * 
 * @author Dallas Fraser
 * @since 2017-11-06
 *
 */
public class PayloadFilter extends TokenFilter {
    private final PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    /**
     * Class Constructor
     * @param in the token stream
     */
    public PayloadFilter(TokenStream in){
        super(in);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!this.input.incrementToken()) {
            return false;
        }
        // get the current token
        final char[] token = Arrays.copyOfRange(this.termAtt.buffer(), 0, this.termAtt.length());
        String stoken = String.valueOf(token);
        String[] parts = stoken.split(Constants.PAYLOAD_DELIMITER);
        System.out.println(stoken + ": " + parts.length);
        if (parts.length > 1 && parts.length == 2){
            termAtt.copyBuffer(parts[0].toCharArray(), 0, parts[0].length());
            System.out.println(parts[0]);
            // the rest is the payload
            payloadAtt.setPayload(new BytesRef(parts[1].getBytes()));
        }else if (parts.length > 1){
            System.out.println("Not sure what to do");
        }

        return true;
    }
}
