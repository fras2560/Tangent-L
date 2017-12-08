package search;

import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.search.similarities.Similarity;

import query.MathSimilarity;
import utilities.Constants;

public class MathSimilarityWrapper extends PerFieldSimilarityWrapper {
    Similarity similarity;
    public MathSimilarityWrapper() {
        this(MathSimilarity.getSimilarity());
    }

    @SuppressWarnings("deprecation")
    public MathSimilarityWrapper(Similarity similarity) {
        // TODO Auto-generated constructor stub
        this.similarity = similarity;
    }

    @Override
    public Similarity get(String field) {
        Similarity result;
        if (field.equals(Constants.MATHFIELD)){
            result = MathSimilarity.getSimilarity(MathSimilarity.MATH);
        }else if(field.equals(Constants.TEXTFIELD)){
            result = MathSimilarity.getSimilarity(MathSimilarity.TEXT);
        }else{
            result = this.similarity;
        }
        return result;
    }
}
