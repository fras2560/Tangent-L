package query;

import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;

public class MathSimilarity {
    public static Similarity getSimilarity(){
        return new BM25Similarity();
    }
}
