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

import java.io.BufferedWriter;
import java.io.IOException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import query.MathQuery;

/**
 * A class to hold the search results for a query.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class SearchResult {
  public TopDocs results;
  public MathQuery mathQuery;
  public Query query;
  public int size;

  /**
   * Class Constructor.
   *
   * @param results the documents returned by the query
   * @param mathQuery the Math Query
   * @param size the number of documents returned by the query
   * @param query the Lucene Query
   */
  public SearchResult(TopDocs results, MathQuery mathQuery, int size, Query query) {
    this.results = results;
    this.mathQuery = mathQuery;
    this.size = size;
    this.query = query;
  }

  /**
   * Returns the maximum number of documents that could be returned by the search.
   *
   * @return int the size of the results
   */
  public int getSize() {
    return this.size;
  }

  /**
   * Returns the results.
   *
   * @return TopDocs the top documents of the results
   */
  public TopDocs getResults() {
    return this.results;
  }

  /** Sets the results. */
  public void setResults(TopDocs docs) {
    this.results = docs;
  }

  /**
   * Returns the Math Query.
   *
   * @return MathQuery the math query of the result
   */
  public MathQuery getMathQuery() {
    return this.mathQuery;
  }

  /**
   * Returns the Query.
   *
   * @return Query the query that was used when searching
   */
  public Query getQuery() {
    return this.query;
  }

  /**
   * Returns the number of documents returned by the search.
   *
   * @return the number of hits.
   */
  public int hitsNumber() {
    int hits = 0;
    if (this.results != null) {
      hits = this.results.totalHits;
    }
    return hits;
  }

  /*
   * Returns a String representation of the object
   * @return a String representation
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String result = this.mathQuery.getQueryName();
    if (this.results != null) {
      final ScoreDoc[] hits = this.results.scoreDocs;
      System.out.println(hits);
      for (final ScoreDoc hit : hits) {
        System.out.println("Hit: " + hit);
        result = result + " " + hit.doc + ":" + hit.score;
      }
    }
    return result;
  }

  /**
   * Explain the results.
   *
   * @param searcher the searcher used
   * @throws IOException - issue while dealing with a file
   */
  public void explainResults(IndexSearcher searcher) throws IOException {
    if (this.results != null) {
      final ScoreDoc[] hits = this.results.scoreDocs;
      for (final ScoreDoc hit : hits) {
        System.out.println(
            searcher.doc(hit.doc).get("path")
                + " "
                + hit.toString()
                + ":"
                + searcher.explain(this.query, hit.doc));
      }
    } else {
      System.out.println("Query had no terms or results");
    }
  }

  /**
   * Explains the results and outputs the explanation to a file.
   *
   * @param searcher the searcher used
   * @param output the file to output to
   * @throws IOException - issue while dealing with a file
   */
  public void explainResults(IndexSearcher searcher, BufferedWriter output) throws IOException {
    if (this.results != null) {
      final ScoreDoc[] hits = this.results.scoreDocs;
      for (final ScoreDoc hit : hits) {
        output.write(
            searcher.doc(hit.doc).get("path")
                + " "
                + hit.toString()
                + ":"
                + searcher.explain(this.query, hit.doc));
        output.newLine();
      }
    } else {
      output.write("Query had no terms or results");
    }
  }
}
