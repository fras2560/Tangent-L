package query;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class DiceSimilarity extends TFIDFSimilarity{
    public DiceSimilarity(){
        super();
    }

    @Override
    public float coord(int overlap, int maxOverlap) {
      return 1f / maxOverlap;
    }

    @Override
    public float idf(long docFreq, long numDocs) {
      return 1f;
    }

    @Override
    public float queryNorm(float sumOfSquaredWeights) {
      return 1f;
    }

    @Override
    public float tf(float freq) {
      return freq == 0f ? 0f : 1f;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float decodeNormValue(long arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long encodeNormValue(float arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float lengthNorm(FieldInvertState arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float scorePayload(int arg0, int arg1, int arg2, BytesRef arg3) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float sloppyFreq(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
