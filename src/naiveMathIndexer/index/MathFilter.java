package naiveMathIndexer.index;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class MathFilter extends TokenFilter {
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private boolean mathTag = true;
	public MathFilter(TokenStream in){
		super(in);
		this.mathTag = true;
	}

	@Override
	public final boolean incrementToken() throws IOException {
	    if (!input.incrementToken()) {
	        return false;
	      }
	    final char[] buffer = termAtt.buffer();
	    final int bufferLength = termAtt.length();
	    final char[] sub = new char[bufferLength];
	    System.arraycopy(buffer, 0, sub, 0, bufferLength);
		return mathTag;
	}
}
