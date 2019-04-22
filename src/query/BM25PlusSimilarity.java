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
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

/**
 * BM25 Similarity. Introduced in Stephen E. Robertson, Steve Walker, Susan Jones, Micheline
 * Hancock-Beaulieu, and Mike Gatford. Okapi at TREC-3. In Proceedings of the Third <b>T</b>ext
 * <b>RE</b>trieval <b>C</b>onference (TREC 1994). Gaithersburg, USA, November 1994.
 */
public class BM25PlusSimilarity extends Similarity {
  private final float k1;
  private final float bvalue;
  private final float delta;

  /**
   * BM25 with the supplied parameter values.
   *
   * @param k1 Controls non-linear term frequency normalization (saturation).
   * @param b Controls to what degree document length normalizes tf values.
   * @throws IllegalArgumentException if {@code k1} is infinite or negative, or if {@code b} is not
   *     within the range {@code [0..1]}
   */
  public BM25PlusSimilarity(float k1, float b) {
    if (Float.isFinite(k1) == false || k1 < 0) {
      throw new IllegalArgumentException(
          "illegal k1 value: " + k1 + ", must be a non-negative finite value");
    }
    if (Float.isNaN(b) || b < 0 || b > 1) {
      throw new IllegalArgumentException("illegal b value: " + b + ", must be between 0 and 1");
    }
    this.k1 = k1;
    this.bvalue = b;
    this.delta = 1f;
  }

  /**
   * BM25 with these default values.
   *
   * <ul>
   *   <li>{@code k1 = 1.2}
   *   <li>{@code b = 0.75}
   * </ul>
   */
  public BM25PlusSimilarity() {
    this(1.2f, 0.75f);
  }

  /** Implemented as <code>log(1 + (docCount - docFreq + 0.5)/(docFreq + 0.5))</code>. */
  protected float idf(long docFreq, long docCount) {
    // return (float) Math.log(1 + (docCount - docFreq + 0.5D)/(docFreq + 0.5D));
    return (float) Math.log((docCount + 1.0d) / (docFreq));
  }

  /** Implemented as <code>1 / (distance + 1)</code>. */
  protected float sloppyFreq(int distance) {
    return 1.0f / (distance + 1);
  }

  /** The default implementation returns <code>1</code>. */
  protected float scorePayload(int doc, int start, int end, BytesRef payload) {
    return 1;
  }

  /**
   * The default implementation computes the average as <code>sumTotalTermFreq / docCount</code>, or
   * returns <code>1</code> if the index does not store sumTotalTermFreq: any field that omits
   * frequency information).
   */
  protected float avgFieldLength(CollectionStatistics collectionStats) {
    final long sumTotalTermFreq = collectionStats.sumTotalTermFreq();
    if (sumTotalTermFreq <= 0) {
      return 1f; // field does not exist, or stat is unsupported
    } else {
      final long docCount =
          collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
      return (float) (sumTotalTermFreq / (double) docCount);
    }
  }

  /**
   * The default implementation encodes <code>boost / sqrt(length)</code> with {@link
   * SmallFloat#floatToByte315(float)}. This is compatible with Lucene's default implementation. If
   * you change this, then you should change {@link #decodeNormValue(byte)} to match.
   */
  protected byte encodeNormValue(float boost, int fieldLength) {
    return SmallFloat.floatToByte315(boost / (float) Math.sqrt(fieldLength));
  }

  /**
   * The default implementation returns <code>1 / f<sup>2</sup></code> where <code>f</code> is
   * {@link SmallFloat#byte315ToFloat(byte)}.
   */
  protected float decodeNormValue(byte b) {
    return NORM_TABLE[b & 0xFF];
  }

  /**
   * True if overlap tokens (tokens with a position of increment of zero) are discounted from the
   * document's length.
   */
  protected boolean discountOverlaps = true;

  /**
   * Sets whether overlap tokens (Tokens with 0 position increment) are ignored when computing norm.
   * By default this is true, meaning overlap tokens do not count when computing norms.
   */
  public void setDiscountOverlaps(boolean v) {
    this.discountOverlaps = v;
  }

  /**
   * Returns true if overlap tokens are discounted from the document's length.
   *
   * @see #setDiscountOverlaps
   */
  public boolean getDiscountOverlaps() {
    return this.discountOverlaps;
  }

  /** Cache of decoded bytes. */
  private static final float[] NORM_TABLE = new float[256];

  static {
    for (int i = 1; i < 256; i++) {
      final float f = SmallFloat.byte315ToFloat((byte) i);
      NORM_TABLE[i] = 1.0f / (f * f);
    }
    NORM_TABLE[0] = 1.0f / NORM_TABLE[255]; // otherwise inf
  }

  @Override
  public final long computeNorm(FieldInvertState state) {
    final int numTerms =
        this.discountOverlaps ? state.getLength() - state.getNumOverlap() : state.getLength();
    return this.encodeNormValue(state.getBoost(), numTerms);
  }

  /**
   * Computes a score factor for a simple term and returns an explanation for that score factor.
   *
   * <p>The default implementation uses:
   *
   * <pre class="prettyprint">
   * idf(docFreq, docCount);
   * </pre>
   *
   * <p>Note that {@link CollectionStatistics#docCount()} is used instead of {@link
   * org.apache.lucene.index.IndexReader#numDocs() IndexReader#numDocs()} because also {@link
   * TermStatistics#docFreq()} is used, and when the latter is inaccurate, so is {@link
   * CollectionStatistics#docCount()}, and in the same direction. In addition, {@link
   * CollectionStatistics#docCount()} does not skew when fields are sparse.
   *
   * @param collectionStats collection-level statistics
   * @param termStats term-level statistics for the term
   * @return an Explain object that includes both an idf score factor and an explanation for the
   *     term.
   */
  public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
    final long df = termStats.docFreq();
    final long docCount =
        collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
    final float idf = this.idf(df, docCount);
    return Explanation.match(
        idf,
        "idf, computed as log(1 + (docCount - docFreq + 0.5) / (docFreq + 0.5)) from:",
        Explanation.match(df, "docFreq"),
        Explanation.match(docCount, "docCount"));
  }

  /**
   * Computes a score factor for a phrase.
   *
   * <p>The default implementation sums the idf factor for each term in the phrase.
   *
   * @param collectionStats collection-level statistics
   * @param termStats term-level statistics for the terms in the phrase
   * @return an Explain object that includes both an idf score factor for the phrase and an
   *     explanation for each term.
   */
  public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics[] termStats) {
    double idf = 0d; // sum into a double before casting into a float
    final List<Explanation> details = new ArrayList<>();
    for (final TermStatistics stat : termStats) {
      final Explanation idfExplain = this.idfExplain(collectionStats, stat);
      details.add(idfExplain);
      idf += idfExplain.getValue();
    }
    return Explanation.match((float) idf, "idf(), sum of:", details);
  }

  @Override
  public final SimWeight computeWeight(
      CollectionStatistics collectionStats, TermStatistics... termStats) {
    final Explanation idf =
        termStats.length == 1
            ? this.idfExplain(collectionStats, termStats[0])
            : this.idfExplain(collectionStats, termStats);

    final float avgdl = this.avgFieldLength(collectionStats);

    // compute freq-independent part of bm25 equation across all norm values
    final float[] cache = new float[256];
    for (int i = 0; i < cache.length; i++) {
      cache[i] =
          this.k1 * ((1 - this.bvalue) + this.bvalue * this.decodeNormValue((byte) i) / avgdl);
    }
    return new BM25Stats(collectionStats.field(), idf, avgdl, cache);
  }

  @Override
  public final SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
    final BM25Stats bm25stats = (BM25Stats) stats;
    return new BM25DocScorer(bm25stats, context.reader().getNormValues(bm25stats.field));
  }

  private class BM25DocScorer extends SimScorer {
    private final BM25Stats stats;
    private final float weightValue; // boost * idf * (k1 + 1)
    private final NumericDocValues norms;
    private final float[] cache;

    BM25DocScorer(BM25Stats stats, NumericDocValues norms) throws IOException {
      this.stats = stats;
      this.weightValue = stats.weight;
      this.cache = stats.cache;
      this.norms = norms;
    }

    @Override
    public float score(int doc, float freq) {
      // if there are no norms, we act as if b=0
      final float norm =
          this.norms == null
              ? BM25PlusSimilarity.this.k1
              : this.cache[(byte) this.norms.get(doc) & 0xFF];
      return this.weightValue
          * ((freq * (BM25PlusSimilarity.this.k1 + 1)) / (freq + norm)
              + BM25PlusSimilarity.this.delta);
    }

    @Override
    public Explanation explain(int doc, Explanation freq) {
      return BM25PlusSimilarity.this.explainScore(doc, freq, this.stats, this.norms);
    }

    @Override
    public float computeSlopFactor(int distance) {
      return BM25PlusSimilarity.this.sloppyFreq(distance);
    }

    @Override
    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
      return BM25PlusSimilarity.this.scorePayload(doc, start, end, payload);
    }
  }

  /** Collection statistics for the BM25 model. */
  private static class BM25Stats extends SimWeight {

    /** BM25's idf. */
    private final Explanation idf;

    /** The average document length. */
    private final float avgdl;

    /** query boost. */
    private float boost;

    /** weight (idf * boost). */
    private float weight;

    /** field name, for pulling norms. */
    private final String field;

    /** pre-computed norm[256] with k1 * ((1 - b) + b * dl / avgdl). */
    private final float[] cache;

    BM25Stats(String field, Explanation idf, float avgdl, float[] cache) {
      this.field = field;
      this.idf = idf;
      this.avgdl = avgdl;
      this.cache = cache;
      this.normalize(1f, 1f);
    }

    @Override
    public float getValueForNormalization() {
      // we return a TF-IDF like normalization to be nice, but we don't actually normalize
      // ourselves.
      return this.weight * this.weight;
    }

    @Override
    public void normalize(float queryNorm, float boost) {
      // we don't normalize with queryNorm at all, we just capture the top-level boost
      this.boost = boost;
      this.weight = this.idf.getValue() * boost;
    }
  }

  private Explanation explainTfNorm(
      int doc, Explanation freq, BM25Stats stats, NumericDocValues norms) {
    final List<Explanation> subs = new ArrayList<>();
    subs.add(freq);
    subs.add(Explanation.match(this.k1, "parameter k1"));
    if (norms == null) {
      subs.add(Explanation.match(0, "parameter b (norms omitted for field)"));
      return Explanation.match(
          (freq.getValue() * (this.k1 + 1)) / (freq.getValue() + this.k1),
          "tfNorm, computed as (freq * (k1 + 1)) / (freq + k1) from:",
          subs);
    } else {
      final float doclen = this.decodeNormValue((byte) norms.get(doc));
      subs.add(Explanation.match(this.bvalue, "parameter b"));
      subs.add(Explanation.match(stats.avgdl, "avgFieldLength"));
      subs.add(Explanation.match(doclen, "fieldLength"));
      return Explanation.match(
          (freq.getValue() * (this.k1 + 1))
              / (freq.getValue()
                  + this.k1 * (1 - this.bvalue + this.bvalue * doclen / stats.avgdl)),
          "tfNorm, computed as (freq * (k1 + 1))"
              + "/ (freq + k1 * (1 - b + b * fieldLength / avgFieldLength)) from:",
          subs);
    }
  }

  private Explanation explainScore(
      int doc, Explanation freq, BM25Stats stats, NumericDocValues norms) {
    final Explanation boostExpl = Explanation.match(stats.boost, "boost");
    final List<Explanation> subs = new ArrayList<>();
    if (boostExpl.getValue() != 1.0f) {
      subs.add(boostExpl);
    }
    subs.add(stats.idf);
    final Explanation tfNormExpl = this.explainTfNorm(doc, freq, stats, norms);
    subs.add(tfNormExpl);
    return Explanation.match(
        boostExpl.getValue() * stats.idf.getValue() * tfNormExpl.getValue(),
        "score(doc=" + doc + ",freq=" + freq + "), product of:",
        subs);
  }

  @Override
  public String toString() {
    return "BM25(k1=" + this.k1 + ",b=" + this.bvalue + ")";
  }

  /**
   * Returns the <code>k1</code> parameter.
   *
   * @see org.apache.lucene.search.similarities.BM25Similarity
   */
  public final float getK1() {
    return this.k1;
  }

  /**
   * Returns the <code>b</code> parameter.
   *
   * @see org.apache.lucene.search.similarities.BM25Similarity
   */
  public final float getB() {
    return this.bvalue;
  }
}
