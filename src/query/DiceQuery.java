package query;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

import utilities.Constants;

public class DiceQuery extends CustomScoreQuery{
    ArrayList<String> terms;
    public DiceQuery(Query subQuery, ArrayList<String> terms) {
        super(subQuery);
        this.terms = terms;
        // TODO Auto-generated constructor stub
    }
    
    protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException{
        
        return new DiceScoreProvider(Constants.FIELD, context, this.terms);
    }
}
