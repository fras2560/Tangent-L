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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.util.BytesRef;
import utilities.Constants;
import utilities.Functions;

/**
 * A class for calculating the score using a few different methods determined by the {@link
 * ConvertConfig}.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class MathScoreQueryProvider extends CustomScoreProvider {
  private final String privateField;
  private final LeafReaderContext context;
  private final List<TermCountPair> terms;
  private final ConvertConfig config;
  private final float avgDl;
  private final float numDocs;

  // all are discussed in the papers
  private static final float ALPHA = (float) 0.3;
  private static final float K_1 = (float) 1.2;
  private static final float K_3 = 1000;
  private static final float K = 2;
  private static final float B = (float) 0.9;

  /**
   * Class constructor.
   *
   * @param field the field to search
   * @param context the context one is searching
   * @param terms the terms of the query
   * @param config the config file to use when querying
   * @param avgDl - average document length
   * @param numDoc - the number of documents
   */
  public MathScoreQueryProvider(
      String field,
      LeafReaderContext context,
      List<TermCountPair> terms,
      ConvertConfig config,
      float avgDl,
      float numDoc) {
    super(context);
    this.privateField = field;
    this.context = context;
    this.terms = terms;
    this.config = config;
    this.avgDl = avgDl;
    this.numDocs = numDoc;
  }

  /**
   * Returns the Custom Score.
   *
   * @param doc the doc number id
   * @param subQueryScore the score the subquery gave it
   * @param valSrcScores an array of scores from src
   * @return float a the update custom score which is calculated based upon config
   */
  @Override
  public float customScore(int doc, float subQueryScore, float[] valSrcScores) throws IOException {
    // subQueryScore is term frequency of the term
    float newScore = 1f;
    if (this.config.getQueryType().equals(ConvertConfig.DICE_QUERY)) {
      newScore = this.diceCustomScore(doc, subQueryScore, valSrcScores);
    } else if (this.config.getQueryType().equals(ConvertConfig.BM25TP_QUERY)) {
      newScore = this.bm25tpCustomScore(doc, subQueryScore, valSrcScores);
    } else if (this.config.getQueryType().equals(ConvertConfig.BM25_DISTANCE_QUERY)) {
      newScore = this.bm25DistanceCustomScore(doc, subQueryScore, valSrcScores);
    } else if (this.config.getQueryType().equals(ConvertConfig.TERM_QUERY)) {
      newScore = subQueryScore;
    } else {
      // default scoring to use
      newScore = subQueryScore;
    }
    return newScore; // New Score
  }

  /**
   * Returns the score using BM25TP as outlined in.
   *
   * @see <a href="https://link.springer.com/content/pdf/10.1007/3-540-36618-0.pdf#page=224">paper
   *     </a>
   * @param doc - an document number
   * @param subQueryScore - a score for the subquery
   * @param valSrcScores an array of scores from src
   * @return the BM25 distance score
   * @throws IOException - issue with reading/writing to a file
   */
  public float bm25DistanceCustomScore(int doc, float subQueryScore, float[] valSrcScores)
      throws IOException {
    float newScore = subQueryScore;
    // comment one out if you want to switch the distance measurement used
    final float measurement = this.minSpan(doc);
    // float measurement = this.minDistancePair(doc);
    newScore = (float) (newScore + Math.log(MathScoreQueryProvider.ALPHA + Math.exp(-measurement)));
    return newScore;
  }

  /**
   * Returns the maximum value of the list of lists.
   *
   * @param positions a list of positions lists
   * @return int the maximum value
   */
  public int maxValue(List<List<Integer>> positions) {
    int value = 0;
    for (final List<Integer> values : positions) {
      final int temp = Collections.max(values).intValue();
      if (temp > value) {
        value = temp;
      }
    }
    return value;
  }

  /**
   * Return the maximum of the min value of each position listing.
   *
   * @param positions a list of position lists
   * @return int the maximum of the min values
   */
  public int maxMinValue(List<List<Integer>> positions) {
    int value = 0;
    for (final List<Integer> values : positions) {
      final int temp = Collections.min(values).intValue();
      if (temp > value) {
        value = temp;
      }
    }
    return value;
  }

  /**
   * Return the minimum of the min value of each position listing.
   *
   * @param positions a list of position lists
   * @return int the minimum of the min values
   */
  public int minMinValue(List<List<Integer>> positions) {
    int value = Integer.MAX_VALUE;
    for (final List<Integer> values : positions) {
      final int temp = Collections.min(values).intValue();
      if (temp < value) {
        value = temp;
      }
    }
    return value;
  }

  /**
   * Removes any positions that are lower than the start.
   *
   * @param positions a list of positions lists
   * @param start the start value or left border
   */
  public void removeLowerPositions(List<List<Integer>> positions, int start) {
    for (final List<Integer> values : positions) {
      while (values.get(0).floatValue() < start) {
        values.remove(0);
      }
    }
  }

  /**
   * Calculates the min span from a list of position lists.
   *
   * @param positions a list of position lists
   * @return float the min span measurement
   */
  public int minSpanCalc(List<List<Integer>> positions) {
    int start = 0;
    final int end = this.maxValue(positions);
    int spanStart;
    int spanEnd;
    final List<Integer> spans = new ArrayList<Integer>();
    if (start == end) {
      spans.add(new Integer(0));
    }
    while (start < end) {
      spanStart = this.minMinValue(positions);
      spanEnd = this.maxMinValue(positions);
      spans.add(new Integer(Math.abs(spanEnd - spanStart)));
      start = spanStart + 1;
      try {
        this.removeLowerPositions(positions, start);
      } catch (final IndexOutOfBoundsException e) {
        // we are done the one position lists is out of values
        start = end;
      }
    }
    return Collections.min(spans).intValue();
  }

  /**
   * Returns the minimum span for all matched terms.
   *
   * @param doc the doc id number
   * @return float the minimum span
   * @throws IOException - issue with reading/writing to a file
   */
  public float minSpan(int doc) throws IOException {
    float distance = 0f;
    final LeafReader reader = this.context.reader();
    final List<List<Integer>> positions = new ArrayList<List<Integer>>();
    if (reader != null) {
      // get a sorted list of positions
      for (final TermCountPair term : this.terms) {
        final List<Integer> pos = new ArrayList<Integer>();
        final PostingsEnum posting =
            reader.postings(new Term(this.privateField, term.getTerm()), PostingsEnum.POSITIONS);
        if (posting != null) {
          // move to the document currently looking at
          posting.advance(doc);
          int count = 0;
          final int freq = posting.freq();
          // make sure to add them all
          while (count < freq) {
            pos.add(new Integer(posting.nextPosition()));
            count += 1;
          }
        }
        if (pos.size() > 0) {
          positions.add(pos);
        }
      }
    }
    if (positions.size() < 2) {
      distance = this.getDocLength(doc);
    } else {
      distance = this.minSpanCalc(positions);
    }
    return distance;
  }

  /**
   * Returns the minimum distance between pairs of terms.
   *
   * @param doc the doc id number
   * @return float the minimum distance
   * @throws IOException - issue with reading/writing to a file
   */
  public float minDistancePair(int doc) throws IOException {
    float distance = 0f;
    final SortedSet<Integer> pos = new TreeSet<Integer>();
    final LeafReader reader = this.context.reader();
    if (reader != null && this.terms.size() > 1) {
      // get a sorted list of positions
      for (final TermCountPair term : this.terms) {
        final PostingsEnum posting =
            reader.postings(new Term(this.privateField, term.getTerm()), PostingsEnum.POSITIONS);
        if (posting != null) {
          // move to the document currently looking at
          posting.advance(doc);
          int count = 0;
          final int freq = posting.freq();
          // make sure to add them all
          while (count < freq) {
            pos.add(new Integer(posting.nextPosition()));
            count += 1;
          }
        }
      }
      // now find the closest pairs
      Integer dist = Math.abs(pos.first() - pos.last());
      final Iterator<Integer> it = pos.iterator();
      Integer prev = pos.last();
      Integer current;
      while (it.hasNext()) {
        current = it.next();
        if (Math.abs(current - prev) < dist) {
          dist = Math.abs(current - prev);
        }
        prev = current;
      }
      distance = dist.intValue();
    } else if (this.terms.size() > 1) {
      distance = this.getDocLength(doc);
    }
    return distance;
  }

  /**
   * Calculate the term pair instance weight.
   *
   * @param i - the i value
   * @param j - the j value
   * @return float the term pair distance
   */
  public float calculateTpi(float i, float j) {
    return (float) (1f / Math.pow(Math.abs(i - j) + 1, 2));
  }

  /**
   * Calculates the weight for a given term pair i and j.
   *
   * @param positionsI The positions that Term i appear in
   * @param positionsJ The positions that Term j appear in
   * @param valueK Okapi document length consideration
   * @return float term weight
   */
  public float termWeight(List<Integer> positionsI, List<Integer> positionsJ, float valueK) {
    float sumTpi = 0;
    float weight;
    for (int i = 0; i < positionsI.size(); i++) {
      for (int j = 0; j < positionsJ.size(); j++) {
        sumTpi += this.calculateTpi(positionsI.get(i), positionsJ.get(j));
      }
    }
    weight = (MathScoreQueryProvider.K_1 + 1) * (sumTpi / (valueK + sumTpi));
    return weight;
  }

  /**
   * Returns the score using BM25 and min-distance as outlined in.
   *
   * @see <a href="https://dl.acm.org/citation.cfm?id=1277794">Tao and Zhai </a>
   * @param doc the doc id number
   * @param subQueryScore the sub query scores
   * @param valSrcScores an array of scores from src
   * @return float the custom score
   * @throws IOException - issue with reading/writing to a file
   */
  public float bm25tpCustomScore(int doc, float subQueryScore, float[] valSrcScores)
      throws IOException {
    final float newScore = subQueryScore;
    final Map<String, List<Integer>> pos = this.termsPositions(doc);
    final float docLength = this.getDocLength(doc);
    final float K =
        MathScoreQueryProvider.K
            * ((1 - MathScoreQueryProvider.B)
                + MathScoreQueryProvider.B * (docLength / this.avgDl));
    int qtfi;
    int qtfj;
    List<Integer> positionsI;
    List<Integer> positionsJ;
    float qwi;
    float qwj;
    float score = 0;
    float termWeight;
    for (int i = 0; i < this.terms.size(); i++) {
      positionsI = pos.get(this.terms.get(i).getTerm());
      if (positionsI != null) {
        qtfi = (int) this.terms.get(i).getCount();
        qwi =
            (float)
                ((qtfi / (MathScoreQueryProvider.K_3 + qtfi))
                    * Math.log((this.numDocs - positionsI.size()) / positionsI.size()));
        for (int j = i + 1; j < this.terms.size(); j++) {
          positionsJ = pos.get(this.terms.get(j).getTerm());
          if (positionsJ != null) {
            qtfj = (int) this.terms.get(j).getCount();
            qwj =
                (float)
                    ((qtfj / (MathScoreQueryProvider.K_3 + qtfj))
                        * Math.log((this.numDocs - positionsJ.size()) / positionsJ.size()));
            termWeight = this.termWeight(positionsI, positionsJ, K);
            score += termWeight * Math.min(qwi, qwj);
          }
        }
      }
    }
    return newScore + score;
  }

  /**
   * Returns the doc length.
   *
   * @param doc the doc number if
   * @return long the doc id
   * @throws IOException - issue with reading/writing to a file
   */
  public long getDocLength(int doc) throws IOException {
    long docLength = 1;
    final LeafReader reader = this.context.reader();
    if (reader != null) {
      docLength = Long.parseLong(reader.document(doc).get(Constants.DOCUMENT_LENGTH));
      // docLength = reader.getNumericDocValues(Constants.DOCUMENT_LENGTH).get(doc);
    }
    return docLength;
  }

  /**
   * Returns a map lookup for a string and its list of positions.
   *
   * @param doc the document to create the map for
   * @return Map
   * @throws IOException - issue with reading/writing to a file
   */
  public Map<String, List<Integer>> termsPositions(int doc) throws IOException {
    final LeafReader reader = this.context.reader();
    final Map<String, List<Integer>> positions = new HashMap<String, List<Integer>>();
    if (reader != null) {
      // get a list of map and their pairs
      for (final TermCountPair term : this.terms) {
        final PostingsEnum posting =
            reader.postings(new Term(this.privateField, term.getTerm()), PostingsEnum.POSITIONS);
        if (posting != null) {
          // move to the document currently looking at
          posting.advance(doc);
          int count = 0;
          final int freq = posting.freq();
          final List<Integer> pos = new ArrayList<Integer>();
          while (count < freq) {
            pos.add(new Integer(posting.nextPosition()));
            count += 1;
          }
          positions.put(term.getTerm(), pos);
        }
      }
    }
    return positions;
  }

  /**
   * Returns a mapping of positions and count for the term given.
   *
   * @param reader the leaf reader of the document
   * @param doc the doc id number
   * @param term the term to lookup
   * @return Map the mapping for positions and counts
   * @throws IOException - issue with reading/writing from a file
   */
  public Map<Float, Float> lookupTermPosition(LeafReader reader, int doc, BytesRef term)
      throws IOException {
    final PostingsEnum posting =
        reader.postings(new Term(this.privateField, term), PostingsEnum.POSITIONS);
    final Map<Float, Float> positions = new HashMap<Float, Float>();
    if (posting != null) {
      // move to the document currently looking at
      posting.advance(doc);
      // get a dictionary of the each potential formula to match to and total number of matches
      int count = 0;
      while (count < posting.freq()) {
        final Float position = new Float(posting.nextPosition());
        final Float freq = positions.get(position);
        if (freq == null) {
          positions.put(position, new Float(1));
        } else {
          positions.put(position, freq + new Float(1));
        }
        count += 1;
      }
    }
    return positions;
  }

  /**
   * Returns a map of each formula's position to the the formula size.
   *
   * @param reader the leaf reader of the document
   * @param doc the doc id number
   * @param formulaSizes - a map from formulate position to its size
   * @return Map the mapping for formula positions to the size of the formula
   * @throws IOException - issue with reading/writing from a file
   */
  public Map<Float, Float> calculateFormulaSizes(
      LeafReader reader, int doc, Map<Float, Float> formulaSizes) throws IOException {
    // now determine the size of each formula
    final Terms termVector = reader.getTermVector(doc, this.privateField);
    TermsEnum termsEnum = null;
    termsEnum = termVector.iterator();
    final Map<String, Boolean> termsCalculated = new HashMap<String, Boolean>();
    // loop through all terms in the vector
    while ((termsEnum.next()) != null) {
      // check if terms positions have already been considered
      if (termsCalculated.get(termsEnum.term().utf8ToString()) == null
          && !Functions.containsWildcard(termsEnum.term().utf8ToString())) {
        // no then remember it has been used
        termsCalculated.put(termsEnum.term().utf8ToString(), new Boolean(true));
        final Map<Float, Float> pos = this.lookupTermPosition(reader, doc, termsEnum.term());
        for (final Map.Entry<Float, Float> entry : pos.entrySet()) {
          // see if the part of a formula the matches some tokens
          final Float freq = formulaSizes.get(entry.getKey());
          if (freq != null) {
            formulaSizes.put(entry.getKey(), freq + entry.getValue());
          }
        }
      }
    }
    return formulaSizes;
  }

  /**
   * Returns the score using the Dice Coefficient.
   *
   * @param doc the doc id number
   * @param subQueryScore the sub query scores
   * @param valSrcScores a list of scores for each value
   * @return float the custom dice score
   * @throws IOException - issue with reading/writing from a file
   */
  public float diceCustomScore(int doc, float subQueryScore, float[] valSrcScores)
      throws IOException {
    // this is very slow at the moment+
    // subQueryScore is term frequency of the term
    final LeafReader reader = this.context.reader();
    float score = 0f;
    float querySize = 0f;
    final Map<Float, Float> queryLookup = new HashMap<Float, Float>();
    Map<Float, Float> formulaSizes = new HashMap<Float, Float>();
    if (reader != null) {
      // find a list of formulas and their positions
      Float bestFormula = new Float(0);
      for (final TermCountPair term : this.terms) {
        querySize += term.getCount();
        final PostingsEnum posting =
            reader.postings(new Term(this.privateField, term.getTerm()), PostingsEnum.POSITIONS);
        if (posting != null) {
          // move to the document currently looking at
          posting.advance(doc);
          // get a dictionary of the each potential formula to match to and total number of matches
          int count = 0;
          while (count < posting.freq()) {
            final Float position = new Float(posting.nextPosition());
            Float positionCount = queryLookup.get(position);
            if (positionCount != null) {
              // already in lookup, update and add one
              positionCount = positionCount + new Float(1);
              queryLookup.put(position, positionCount);
            } else {
              // add to lookup and formula size
              queryLookup.put(position, new Float(1));
              formulaSizes.put(position, new Float(0));
            }
            count += 1;
          }
          // calculate the formula sizes
          formulaSizes = this.calculateFormulaSizes(reader, doc, formulaSizes);
          // now calculate the best formula that was matched
          for (final Map.Entry<Float, Float> entry : queryLookup.entrySet()) {
            // calculate the intersection
            final Float temp =
                (new Float(2) * entry.getValue()) / (querySize + formulaSizes.get(entry.getKey()));
            if (temp > bestFormula) {
              bestFormula = temp;
            }
          }
        }
      }
      score = bestFormula.floatValue();
    } else {
      System.err.println("Unable to find LeafReader for Dice Query");
    }
    return score; // New Score
  }
}
