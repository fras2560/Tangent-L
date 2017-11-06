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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.util.BytesRef;

import index.ConvertConfig;
import utilities.Functions;
/**
 * A class for calculating the score using a few different methods determined by the {@link ConvertConfig}
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class MathScoreQueryProvider extends CustomScoreProvider{
    private String _field;
    private LeafReaderContext context;
    private List<TermCountPair> terms;
    private ConvertConfig config;

    /**
     * Class constructor
     * @param field the field to search
     * @param context the context one is searching
     * @param terms the terms of the query
     * @param config the config file to use when querying
     */
    public MathScoreQueryProvider(String field,
                                 LeafReaderContext context,
                                 List<TermCountPair> terms,
                                 ConvertConfig config) {
        super(context);
        this._field = field;
        this.context = context;
        this.terms = terms;
        this.config = config;
    }

    /**
     * Returns the custom score
     */
    public float customScore(int doc, float subQueryScore, float valSrcScores [])  throws IOException {
        // subQueryScore is term frequency of the term
        float newScore = 1f;
        if (this.config.getQueryType().equals(ConvertConfig.DICE_QUERY)){
            newScore = this.diceCustomScore(doc, subQueryScore, valSrcScores);
        }else if(this.config.getQueryType().equals(ConvertConfig.BM25TP_QUERY)){
            newScore = this.bm25tpCustomScore(doc, subQueryScore, valSrcScores);
        }else if(this.config.getQueryType().equals(ConvertConfig.BM25_DISTANCE_QUERY)){
            newScore = this.bm25DistanceCustomScore(doc, subQueryScore, valSrcScores);
        }else if(this.config.getQueryType().equals(ConvertConfig.TERM_QUERY)){
            newScore = subQueryScore;
        }else{
            // default scoring to use
            newScore = subQueryScore;
        }
        return newScore; // New Score
    }

    /**
     * Returns the score using BM25TP as outlined in
     * @see <a href="https://link.springer.com/content/pdf/10.1007/3-540-36618-0.pdf#page=224">paper </a>
     * @param doc
     * @param subQueryScore
     * @param valSrcScores
     * @return
     * @throws IOException
     */
    public float bm25DistanceCustomScore(int doc, float subQueryScore, float valSrcScores []) throws IOException{
        float newScore = 1f;
        return newScore;
    }

    /**
     * Returns the score using BM25 and min-distance as outlined in
     * @see <a href="https://dl.acm.org/citation.cfm?id=1277794">Tao and Zhai </a>
     * @param doc the doc id number
     * @param subQueryScore the sub query scores
     * @param valSrcScores
     * @return float the custom score
     * @throws IOException
     */
    public float bm25tpCustomScore(int doc, float subQueryScore, float valSrcScores []) throws IOException{
        float newScore = 1f;
        return newScore;
    }

    /**
     * Returns a mapping of positions and count for the term given
     * @param reader the leaf reader of the document
     * @param doc the doc id number
     * @param term the term to lookup
     * @return Map<Float, Float> the mapping for positions and counts
     * @throws IOException
     */
    public Map<Float, Float> lookupTermPosition(LeafReader reader, int doc, BytesRef term) throws IOException{
        PostingsEnum posting = reader.postings(new Term(this._field, term), PostingsEnum.POSITIONS);
        Map<Float, Float>positions = new HashMap<Float, Float>();
        if (posting != null){
            // move to the document currently looking at
            posting.advance(doc);
            // get a dictionary of the each potential formula to match to and total number of matches
            int count = 0;
            while (count < posting.freq()){
                Float position = new Float(posting.nextPosition());
                Float freq = positions.get(position);
                if(freq == null){
                    positions.put(position, new Float(1));
                }else{
                    positions.put(position, freq + new Float(1));
                }
                count += 1;
            }
        }
        return positions;
    }

    /**
     * Returns a map of each formula's position to the the formula size
     * @param reader the leaf reader of the document
     * @param doc the doc id number
     * @param formulaSizes 
     * @return Map<Float, Float> the mapping for formula positions to the size of the formula
     * @throws IOException
     */
    public Map<Float, Float> calculateFormulaSizes(LeafReader reader, int doc, Map<Float, Float> formulaSizes) throws IOException{
     // now determine the size of each formula
        Terms termVector = reader.getTermVector(doc, _field);
        TermsEnum termsEnum = null;
        termsEnum = termVector.iterator();
        Map<String, Boolean> termsCalculated = new HashMap<String, Boolean>();
        // loop through all terms in the vector
        while((termsEnum.next()) != null) {
            // check if terms positions have already been considered
            if (termsCalculated.get(termsEnum.term().utf8ToString()) == null &&
                !Functions.containsWildcard(termsEnum.term().utf8ToString())){
                // no then remember it has been used
                termsCalculated.put(termsEnum.term().utf8ToString(), new Boolean(true));
                Map<Float, Float> pos = this.lookupTermPosition(reader, doc, termsEnum.term());
                for (Map.Entry<Float, Float> entry : pos.entrySet()) {
                    // see if the part of a formula the matches some tokens
                    Float freq = formulaSizes.get(entry.getKey());
                    if (freq != null){
                        formulaSizes.put(entry.getKey(), freq + entry.getValue());
                    }
                }
            }
        }
        return formulaSizes;
    }

    /**
     * Returns the score using the Dice Coefficient
     * @param doc the doc id number 
     * @param subQueryScore the sub query scores
     * @param valSrcScores
     * @return float the custom dice score
     * @throws IOException
     */
    public float diceCustomScore(int doc, float subQueryScore, float valSrcScores [])  throws IOException {
        // this is very slow at the moment+
        // subQueryScore is term frequency of the term
        LeafReader reader = this.context.reader();
        float score = 0f;
        float querySize = 0f;
        Map<Float, Float> queryLookup = new HashMap<Float, Float>();
        Map<Float, Float> formulaSizes = new HashMap<Float, Float>();
        if (reader != null){
            // find a list of formulas and their positions
            Float bestFormula = new Float(0);
            for (TermCountPair term : this.terms){
                querySize += term.getCount();
                PostingsEnum posting = reader.postings(new Term(this._field, term.getTerm()), PostingsEnum.POSITIONS);
                if (posting != null){
                    // move to the document currently looking at
                    posting.advance(doc);
                    // get a dictionary of the each potential formula to match to and total number of matches
                    int count = 0;
                    while (count < posting.freq()){
                        Float position = new Float(posting.nextPosition());
                        Float positionCount = queryLookup.get(position);
                        if (positionCount != null){
                            // already in lookup, update and add one
                            positionCount = positionCount + new Float(1);
                            queryLookup.put(position, positionCount);
                        }else{
                            // add to lookup and formula size
                            queryLookup.put(position, new Float(1));
                            formulaSizes.put(position, new Float(0));
                        }
                        count += 1;
                    }
                    // calculate the formula sizes
                    formulaSizes = this.calculateFormulaSizes(reader, doc, formulaSizes);
                    // now calculate the best formula that was matched
//                    System.out.println("Formula matches");
//                    Functions.printMap(queryLookup);
//                    System.out.println("Formula sizes");
//                    Functions.printMap(formulaSizes);
//                    System.out.println("Query Size:" + querySize);
                    for (Map.Entry<Float, Float> entry : queryLookup.entrySet()) {
                        // calculate the intersection
                        Float temp =  (new Float(2) * entry.getValue()) / (querySize + formulaSizes.get(entry.getKey()));
                        if (temp > bestFormula){
                            bestFormula = temp;
                        }
                    }
                }
            }
            score = bestFormula.floatValue();
        }else{
            System.err.println("Unable to find LeafReader for Dice Query");
        }
        return score; // New Score
    }
}
