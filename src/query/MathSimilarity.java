package query;

import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;

public class MathSimilarity {
    private static int CLASSIC = 0;
    private static int BM25 = 1;
    private static int BOOLEAN = 2;
    public static Similarity getSimilarity(int type){
        Similarity sim = new BooleanSimilarity();
        if(type == MathSimilarity.CLASSIC)
            sim =  new ClassicSimilarity();
        else if(type == MathSimilarity.BM25){
            sim =  new BM25Similarity();
        }else if(type == MathSimilarity.BOOLEAN){
            sim = new BooleanSimilarity();
        }
        return sim;
    }

    public static Similarity getSimilarity(){
        return getSimilarity(MathSimilarity.BM25);
    }

}
