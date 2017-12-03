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
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import utilities.Constants;
import utilities.Payload;
import utilities.Payload.PayloadException;

/**
 * A query that boost a term if it matches the location of the query term
 * @author Dallas Fraser
 * @since 2017-11-14
 */
public class LocationBoostedQuery extends CustomScoreQuery{
    private TermCountPair term;
    private static float DEFAULT_BOOST = 1.5f;
    private float boost;
    private String field;
    /**
     * Class constructor
     * @param subQuery the Term Query
     * @param term the term of the Query
     */
    public LocationBoostedQuery(Query subQuery, TermCountPair term) {
        super(subQuery);
        this.term = term;
        this.boost = LocationBoostedQuery.DEFAULT_BOOST;
        this.field = Constants.FIELD;
    }

    /**
     * Class constructor
     * @param subQuery the Term Query
     * @param term the term of the Query
     * @param field the field of the query
     */
    public LocationBoostedQuery(Query subQuery, TermCountPair term, String field){
        super(subQuery);
        this.term = term;
        this.boost = LocationBoostedQuery.DEFAULT_BOOST;
        this.field = field;
    }

    /**
     * Class Constructor
     * @param subQuery the Term Query
     * @param term the term of the Query
     * @param boost the factor to boost by
     */
    public LocationBoostedQuery(Query subQuery, TermCountPair term, float boost){
        super(subQuery);
        this.term = term;
        this.boost = boost;
        this.field = Constants.FIELD;
    }

    /**
     * Class Constructor
     * @param subQuery the Term Query
     * @param term the term of the Query
     * @param boost the factor to boost by
     * @param field the field of the query
     */
    public LocationBoostedQuery(Query subQuery, TermCountPair term, float boost, String field){
        super(subQuery);
        this.term = term;
        this.boost = boost;
        this.field = field;
    }

    /**
     * Returns the Score Provider for the Query
     * @param context the context the score provider uses
     * @return CustomScoreProvider the score provider to use
     */
    protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException{
        return new LocationBoostedProvider(context, this.term, this.boost, this.field);
    }

    /**
     * A Private Class for the Location Boosted Query that calculates the score
     * @author Dallas Fraser
     * @since 2017-11-14
     */
    private class LocationBoostedProvider extends CustomScoreProvider{
        private LeafReaderContext context;
        private TermCountPair term;
        private float boost;
        private String field;
        /**
         * Class Constructor
         * @param context the leaf reader context
         * @param term the term of the query
         * @param boost the factor to boost when location is matched
         */
        public LocationBoostedProvider(LeafReaderContext context, TermCountPair term, float boost, String field) {
            super(context);
            this.context = context;
            this.term = term;
            this.boost = boost;
            this.field = field;
        }

       /**
        * Determines the factor to boost the score by
        * @param doc the doc id
        * @return float the factor to boost by
        * @throws IOException
        */
       public float determineBoost(int doc) throws IOException{
           float boost = 1f;
           LeafReader reader = this.context.reader();
           System.out.println("Has payloads:" + reader.getFieldInfos().hasPayloads());
           // loop through each location of the term and boost if location matches the payload
           if (reader != null){
               PostingsEnum posting = reader.postings(new Term(this.field, term.getTerm()), PostingsEnum.POSITIONS);
               System.out.println("Term: " + term.getTerm());
               if (posting != null){
                   // move to the document currently looking at
                   posting.advance(doc);
                   int count = 0;
                   while (count < posting.freq()){
                       BytesRef load = posting.getPayload();
                       System.out.println(posting);
                       System.out.println(posting.getClass());
                       System.out.println(posting.attributes());
                       System.out.println("Load: " + load);
                       // if the location matches in the term location than boos the term by the boost factor
                       try {
                            if(load != null && term.containLocation(new Payload(load))){
                                   boost =  boost * this.boost;
                               }
                        } catch (PayloadException e) {
                            // do not care too much, the payload is unrecognized
                            // this is not going to change the  boost factor
                        }
                       posting.nextPosition();
                       count += 1;
                   }
               }
           }
           return boost;
       }

       public float customScore(int doc, float subQueryScore, float valSrcScores [])  throws IOException {
           float boost = this.determineBoost(doc);
           return subQueryScore * boost;
           
       }
    }
}
