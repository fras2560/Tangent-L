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

import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
/**
 * Determine the type of similarity to use for math search
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class MathSimilarity {
    public static int CLASSIC = 0;
    public static int BM25 = 1;
    public static int BOOLEAN = 2;
    public static int MATH = 3;
    public static int TEXT = 4;
    public static int ADJUSTED_BM25 = 5;
    public static int BM25_PLUS = 6;
    /**
     * Class constructor
     * @param type the similarity type to use
     * @return
     */
    public static Similarity getSimilarity(int type){
        Similarity sim = new BooleanSimilarity();
        if(type == MathSimilarity.CLASSIC)
            sim =  new ClassicSimilarity();
        else if(type == MathSimilarity.BM25){
            sim =  new BM25Similarity();
        }else if(type == MathSimilarity.BOOLEAN){
            sim = new BooleanSimilarity();
        }else if(type == MathSimilarity.MATH){
            BM25Similarity temp = new BM25Similarity();
            sim = (Similarity) temp;
        }else if(type == MathSimilarity.TEXT){
            sim = new BM25Similarity();
        }else if(type == MathSimilarity.ADJUSTED_BM25){
            sim = new AdjustedBM25Similarity();
        }else if(type == MathSimilarity.BM25_PLUS){
            sim = new BM25PlusSimilarity();
        }
        return sim;
    }

    /**
     * Get the similarity to use
     * @return Similarity the similarity to use
     */
    public static Similarity getSimilarity(){
        return getSimilarity(MathSimilarity.BM25);
    }

}