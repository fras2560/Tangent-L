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

import index.ConvertConfig;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Query;
import utilities.Constants;

/**
 * A class for the scoring math query based upon the {@link ConvertConfig}.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class MathScoreQuery extends CustomScoreQuery {
  private List<TermCountPair> termCounts;
  private String field;
  private ConvertConfig config;
  private float avgDl;
  private float numDocs;

  /**
   * Class constructor.
   *
   * @param subQuery the subQuery of the math terms (usually a BooleanQuery)
   * @param terms the terms for the query along with the number of time they appear in the query
   * @param config the config file
   * @param stats statistics about the index collection
   */
  public MathScoreQuery(
      Query subQuery, List<TermCountPair> terms, ConvertConfig config, CollectionStatistics stats) {
    super(subQuery);
    this.init(subQuery, terms, Constants.FIELD, config, stats);
  }

  /**
   * Class constructor.
   *
   * @param subQuery the subQuery of the math terms (usually a BooleanQuery)
   * @param terms the terms for the query along with the number of times they appear in the query
   * @param field the field to query
   * @param config the config file
   * @param stats statistics about the index collection
   */
  public MathScoreQuery(
      Query subQuery,
      List<TermCountPair> terms,
      String field,
      ConvertConfig config,
      CollectionStatistics stats) {
    super(subQuery);
    this.init(subQuery, terms, field, config, stats);
  }

  /**
   * Initializes the class.
   *
   * @param subQuery the subQuery of the math terms (usually a BooleanQuery)
   * @param terms the terms for the query along with the number of times they appear in the query
   * @param field the field to query
   * @param config the config file
   * @param stats statistics about the index collection
   */
  public void init(
      Query subQuery,
      List<TermCountPair> terms,
      String field,
      ConvertConfig config,
      CollectionStatistics stats) {
    this.termCounts = terms;
    this.field = field;
    this.config = config;
    final long sumTotalTermFreq = stats.sumTotalTermFreq();
    final long docCount = stats.docCount() == -1 ? stats.maxDoc() : stats.docCount();
    this.avgDl = 1f;
    if (sumTotalTermFreq > 0) {
      this.avgDl = (float) (sumTotalTermFreq / (double) docCount);
    }
    this.numDocs = docCount;
  }

  /**
   * Returns the Score Provider for the Query.
   *
   * @param context the context the score provider uses
   * @return CustomScoreProvider the score provider to use
   */
  @Override
  protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context)
      throws IOException {
    return new MathScoreQueryProvider(
        this.field, context, this.termCounts, this.config, this.avgDl, this.numDocs);
  }
}
