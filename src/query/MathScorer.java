package query;

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

public class MathScorer extends Scorer{

    protected MathScorer(Weight weight) {
        super(weight);
        // TODO Auto-generated constructor stub
    }

    @Override
    public int docID() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int freq() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DocIdSetIterator iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float score() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

}
