package query;

import java.io.IOException;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.AttributeSource;

public class MQuery extends Query {

    @Override
    public boolean equals(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String toString(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
        return new MathWeight(this);
    }

}
