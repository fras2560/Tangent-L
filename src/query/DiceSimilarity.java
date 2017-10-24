package query;

import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.util.SmallFloat;

public class DiceSimilarity extends BooleanSimilarity{
    /** Cache of decoded bytes. */
    private static final float[] NORM_TABLE = new float[256];

    static {
      for (int i = 0; i < 256; i++) {
        NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte)i);
      }
    }

    public DiceSimilarity(){
        super();
    }

    @Override
    public float coord(int overlap, int maxOverlap) {
      return 1f;
    }

    @Override
    public float queryNorm(float sumOfSquaredWeights) {
      return 1f;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }

}
