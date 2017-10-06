package query;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

public class MathWeight extends Weight {

    protected MathWeight(Query query) {
        super(query);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Explanation explain(LeafReaderContext arg0, int arg1) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void extractTerms(Set<Term> arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public float getValueForNormalization() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void normalize(float arg0, float arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Scorer scorer(LeafReaderContext arg0) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
