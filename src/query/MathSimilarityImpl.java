package query;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.util.BytesRef;

public class MathSimilarityImpl extends SimilarityBase{

    @Override
    protected float score(BasicStats arg0, float arg1, float arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }

    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
        System.out.println("Bytes:" + payload);
      return 1f;
    }
}
