package query;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

import utilities.Constants;

public class DiceQuery extends CustomScoreQuery{
    ArrayList<TermCountPair> termCounts;
    float denominator;
    float sizeOfQuery;
    TermCountPair term;
    public DiceQuery(Query subQuery, ArrayList<TermCountPair> terms, TermCountPair term) {
        super(subQuery);
        this.sizeOfQuery = 0;
        // remember the query
        this.term = term;
        for (TermCountPair tcp : terms){
            this.sizeOfQuery += tcp.getCount();
        }
    }
    
    protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException{
        return new DiceScoreProvider(Constants.FIELD, context, this.sizeOfQuery, this.term);
    }
}
