/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package query;


import java.io.IOException;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;

/** Expert: A <code>Scorer</code> for documents matching a <code>Term</code>.
 */
final class MathTermScorer extends Scorer {
  private final PostingsEnum postingsEnum;
  private final Similarity.SimScorer docScorer;
  private final int termCount;
  /**
   * Construct a <code>TermScorer</code>.
   *
   * @param weight
   *          The weight of the <code>Term</code> in the query.
   * @param td
   *          An iterator over the documents matching the <code>Term</code>.
   * @param docScorer
   *          The <code>Similarity.SimScorer</code> implementation
   *          to be used for score computations.
   */
  MathTermScorer(Weight weight, PostingsEnum td, Similarity.SimScorer docScorer, int termCount) {
    super(weight);
    this.docScorer = docScorer;
    this.postingsEnum = td;
    this.termCount = termCount;
  }

  @Override
  public int docID() {
    return postingsEnum.docID();
  }

  @Override
  public int freq() throws IOException {
    return postingsEnum.freq();
  }

  @Override
  public DocIdSetIterator iterator() {
    return postingsEnum;
  }

  @Override
  public float score() throws IOException {
    assert docID() != DocIdSetIterator.NO_MORE_DOCS;
    int maxFreq = Math.min(postingsEnum.freq(), this.termCount );
    return docScorer.score(postingsEnum.docID(), maxFreq);
  }

  /** Returns a string representation of this <code>TermScorer</code>. */
  @Override
  public String toString() { return "scorer(" + weight + ")[" + super.toString() + "]"; }
}
