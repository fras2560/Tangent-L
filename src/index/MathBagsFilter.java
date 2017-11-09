/*
 * Copyright 2017 Dallas Fraser
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package index;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import utilities.Constants;

/**
 * Filters all math tuples for the same formula to be in the same position
 * 
 * @author Dallas Fraser
 * @since 2017-11-06
 *
 */
public class MathBagsFilter extends TokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private boolean openTag;
    /**
     * Class Constructor
     * @param in the stream of tokens
     */
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