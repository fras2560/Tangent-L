package query;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

public class DiceSimilarity extends TFIDFSimilarity{
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

    /**
     * Encodes a normalization factor for storage in an index.
     * <p>
     * The encoding uses a three-bit mantissa, a five-bit exponent, and the
     * zero-exponent point at 15, thus representing values from around 7x10^9 to
     * 2x10^-9 with about one significant decimal digit of accuracy. Zero is also
     * represented. Negative numbers are rounded up to zero. Values too large to
     * represent are rounded down to the largest representable value. Positive
     * values too small to represent are rounded up to the smallest positive
     * representable value.
     * 
     * @see org.apache.lucene.document.Field#setBoost(float)
     * @see org.apache.lucene.util.SmallFloat
     */
    @Override
    public final long encodeNormValue(float f) {
      return SmallFloat.floatToByte315(f);
    }

    /**
     * Decodes the norm value, assuming it is a single byte.
     * 
     * @see #encodeNormValue(float)
     */
    @Override
    public final float decodeNormValue(long norm) {
      return NORM_TABLE[(int) (norm & 0xFF)];  // & 0xFF maps negative bytes to positive above 127
    }

    @Override
    public float lengthNorm(FieldInvertState state) {
        return 1f;
    }

    @Override
    public float scorePayload(int arg0, int arg1, int arg2, BytesRef arg3) {
        System.out.println("Payload" + arg3);
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float sloppyFreq(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
