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
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Filters words that are not tuples.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class RemoveTextFilter extends FilteringTokenFilter {
  private final CharTermAttribute termAtt = this.addAttribute(CharTermAttribute.class);

  /**
   * Class Constructor.
   *
   * @param in the stream of tokens
   */
  public RemoveTextFilter(TokenStream in) {
    super(in);
  }

  @Override
  protected boolean accept() throws IOException {
    // get the current token
    final char[] token = Arrays.copyOfRange(this.termAtt.buffer(), 0, this.termAtt.length());
    final String stoken = String.valueOf(token);
    boolean keep = false;
    if (stoken.startsWith("(") && stoken.endsWith(")")) {
      keep = true;
    }
    return keep;
  }
}
