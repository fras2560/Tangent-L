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

package search;

import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.search.similarities.Similarity;
import query.MathSimilarity;
import utilities.Constants;

/**
 * A wrapper for math similarity.
 *
 * @author Dallas
 */
public class MathSimilarityWrapper extends PerFieldSimilarityWrapper {
  Similarity similarity;

  public MathSimilarityWrapper() {
    this(MathSimilarity.getSimilarity());
  }

  @SuppressWarnings("deprecation")
  public MathSimilarityWrapper(Similarity similarity) {
    // default similarity
    this.similarity = similarity;
  }

  @Override
  public Similarity get(String field) {
    Similarity result;
    if (field.equals(Constants.MATHFIELD)) {
      result = MathSimilarity.getSimilarity(MathSimilarity.MATH);
    } else if (field.equals(Constants.TEXTFIELD)) {
      result = MathSimilarity.getSimilarity(MathSimilarity.TEXT);
    } else {
      result = this.similarity;
    }
    return result;
  }
}
