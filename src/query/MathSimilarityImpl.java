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

package query;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.util.BytesRef;

/**
 * Implementation of math similarity.
 *
 * <p>Does not seem to be complete
 *
 * @author Dallas
 */
public class MathSimilarityImpl extends SimilarityBase {

  @Override
  protected float score(BasicStats arg0, float arg1, float arg2) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Computes the payload factor.
   *
   * @param doc - the document number
   * @param start - the start of the payload
   * @param end - the end of the payload
   * @param payload - the bytes of the payload
   * @return payload factor score
   */
  public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
    // TODO Auto-generated method stub
    System.out.println("Bytes:" + payload);
    return 1f;
  }
}
