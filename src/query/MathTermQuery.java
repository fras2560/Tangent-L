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
import java.util.Objects;
import java.util.Set;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;

/**
 * A Query that matches documents containing a term. This may be combined with other terms with a
 * {@link BooleanQuery}.
 */
public class MathTermQuery extends Query {

  private final Term term;
  private final TermContext perReaderTermState;
  private final int termCount;

  final class MathTermWeight extends Weight {
    private final Similarity similarity;
    private final Similarity.SimWeight stats;
    private final TermContext termStates;
    private final boolean needsScores;
    private final int termCount;

    public MathTermWeight(
        IndexSearcher searcher, boolean needsScores, TermContext termStates, int termCount)
        throws IOException {
      super(MathTermQuery.this);
      this.termCount = termCount;
      if (needsScores && termStates == null) {
        throw new IllegalStateException("termStates are required when scores are needed");
      }
      this.needsScores = needsScores;
      this.termStates = termStates;
      this.similarity = searcher.getSimilarity(needsScores);

      final CollectionStatistics collectionStats;
      final TermStatistics termStats;
      if (needsScores) {
        collectionStats = searcher.collectionStatistics(MathTermQuery.this.term.field());
        termStats = searcher.termStatistics(MathTermQuery.this.term, termStates);
      } else {
        // we do not need the actual stats, use fake stats with docFreq=maxDoc and ttf=-1
        final int maxDoc = searcher.getIndexReader().maxDoc();
        collectionStats =
            new CollectionStatistics(MathTermQuery.this.term.field(), maxDoc, -1, -1, -1);
        termStats = new TermStatistics(MathTermQuery.this.term.bytes(), maxDoc, -1);
      }

      this.stats = this.similarity.computeWeight(collectionStats, termStats);
    }

    @Override
    public void extractTerms(Set<Term> terms) {
      terms.add(MathTermQuery.this.getTerm());
    }

    @Override
    public String toString() {
      return "weight(" + MathTermQuery.this + ")";
    }

    @Override
    public float getValueForNormalization() {
      return this.stats.getValueForNormalization();
    }

    @Override
    public void normalize(float queryNorm, float boost) {
      this.stats.normalize(queryNorm, boost);
    }

    @Override
    public Scorer scorer(LeafReaderContext context) throws IOException {
      assert this.termStates == null
              || this.termStates.wasBuiltFor(ReaderUtil.getTopLevelContext(context))
          : "The top-reader used to create Weight"
              + "is not the same as the current reader's top-reader ("
              + ReaderUtil.getTopLevelContext(context);
      ;
      final TermsEnum termsEnum = this.getTermsEnum(context);
      if (termsEnum == null) {
        return null;
      }
      final PostingsEnum docs =
          termsEnum.postings(null, this.needsScores ? PostingsEnum.FREQS : PostingsEnum.NONE);
      assert docs != null;
      return new MathTermQueryScorer(
          this, docs, this.similarity.simScorer(this.stats, context), this.termCount);
    }

    /**
     * Returns a {@link TermsEnum} positioned at this weights Term or null if the term does not
     * exist in the given context.
     */
    private TermsEnum getTermsEnum(LeafReaderContext context) throws IOException {
      if (this.termStates != null) {
        // TermQuery either used as a Query or the term states have been provided at construction
        // time
        assert this.termStates.wasBuiltFor(ReaderUtil.getTopLevelContext(context))
            : "The top-reader used to create Weight"
                + "is not the same as the current reader's top-reader ("
                + ReaderUtil.getTopLevelContext(context);
        final TermState state = this.termStates.get(context.ord);
        if (state == null) { // term is not present in that reader
          assert this.termNotInReader(context.reader(), MathTermQuery.this.term)
              : "no termstate found but term exists in reader term=" + MathTermQuery.this.term;
          return null;
        }
        final TermsEnum termsEnum =
            context.reader().terms(MathTermQuery.this.term.field()).iterator();
        termsEnum.seekExact(MathTermQuery.this.term.bytes(), state);
        return termsEnum;
      } else {
        // TermQuery used as a filter, so the term states have not been built up front
        final Terms terms = context.reader().terms(MathTermQuery.this.term.field());
        if (terms == null) {
          return null;
        }
        final TermsEnum termsEnum = terms.iterator();
        if (termsEnum.seekExact(MathTermQuery.this.term.bytes())) {
          return termsEnum;
        } else {
          return null;
        }
      }
    }

    /**
     * Returns whether the given term is in the reader.
     *
     * @param reader - the reader
     * @param term - the term
     * @return true if reader contains the term, false otherwise
     * @throws IOException - issue with reading/writing from a file
     */
    private boolean termNotInReader(LeafReader reader, Term term) throws IOException {
      // only called from assert
      // System.out.println("TQ.termNotInReader reader=" + reader + " term=" +
      // field + ":" + bytes.utf8ToString());
      return reader.docFreq(term) == 0;
    }

    @Override
    public Explanation explain(LeafReaderContext context, int doc) throws IOException {
      final Scorer scorer = this.scorer(context);
      if (scorer != null) {
        final int newDoc = scorer.iterator().advance(doc);
        if (newDoc == doc) {
          final float freq = scorer.freq();
          final SimScorer docScorer = this.similarity.simScorer(this.stats, context);
          final Explanation freqExplanation = Explanation.match(freq, "termFreq=" + freq);
          final Explanation scoreExplanation = docScorer.explain(doc, freqExplanation);
          return Explanation.match(
              scoreExplanation.getValue(),
              "weight("
                  + this.getQuery()
                  + " in "
                  + doc
                  + ") ["
                  + this.similarity.getClass().getSimpleName()
                  + "], result of:",
              scoreExplanation);
        }
      }
      return Explanation.noMatch("no matching term");
    }
  }

  /** Constructs a query for the term <code>t</code>. */
  public MathTermQuery(Term t, int termCount) {
    this.term = Objects.requireNonNull(t);
    this.perReaderTermState = null;
    this.termCount = termCount;
  }

  /**
   * Expert: constructs a TermQuery that will use the provided docFreq instead of looking up the
   * docFreq against the searcher.
   */
  public MathTermQuery(Term t, TermContext states, int termCount) {
    assert states != null;
    this.term = Objects.requireNonNull(t);
    this.perReaderTermState = Objects.requireNonNull(states);
    this.termCount = termCount;
  }

  /** Returns the term of this query. */
  public Term getTerm() {
    return this.term;
  }

  @Override
  public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
    final IndexReaderContext context = searcher.getTopReaderContext();
    final TermContext termState;
    if (this.perReaderTermState == null || this.perReaderTermState.wasBuiltFor(context) == false) {
      if (needsScores) {
        // make TermQuery single-pass if we don't have a PRTS or if the context
        // differs!
        termState = TermContext.build(context, this.term);
      } else {
        // do not compute the term state, this will help save seeks in the terms
        // dict on segments that have a cache entry for this query
        termState = null;
      }
    } else {
      // PRTS was pre-build for this IS
      termState = this.perReaderTermState;
    }

    return new MathTermWeight(searcher, needsScores, termState, this.termCount);
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String field) {
    final StringBuilder buffer = new StringBuilder();
    if (!this.term.field().equals(field)) {
      buffer.append(this.term.field());
      buffer.append(":");
    }
    buffer.append(this.term.text());
    return buffer.toString();
  }

  /** Returns true iff <code>o</code> is equal to this. */
  @Override
  public boolean equals(Object other) {
    return this.sameClassAs(other) && this.term.equals(((MathTermQuery) other).term);
  }

  @Override
  public int hashCode() {
    return this.classHash() ^ this.term.hashCode();
  }
}
