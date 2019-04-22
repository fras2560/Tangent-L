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
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;
import utilities.Constants;

/**
 * Filter adds a payload to appropriate terms.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class PayloadFilter extends TokenFilter {
  private final PayloadAttribute payloadAtt;
  private final CharTermAttribute termAtt = this.addAttribute(CharTermAttribute.class);

  /**
   * Class Constructor.
   *
   * @param in the token stream
   */
  public PayloadFilter(TokenStream in) {
    super(in);
    this.payloadAtt = this.addAttribute(PayloadAttribute.class);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (!this.input.incrementToken()) {
      return false;
    }
    // get the current token
    final char[] token = Arrays.copyOfRange(this.termAtt.buffer(), 0, this.termAtt.length());
    final String stoken = String.valueOf(token);
    final String[] parts = stoken.split(Constants.PAYLOAD_DELIMITER);
    if (parts.length > 1 && parts.length == 2) {
      this.termAtt.setLength(parts[0].length());
      // the rest is the payload
      final BytesRef br = new BytesRef(parts[1]);
      this.payloadAtt.setPayload(br);
    } else if (parts.length > 1) {
      // skip
    } else {
      // no payload here
      this.payloadAtt.setPayload(null);
    }
    return true;
  }
}
