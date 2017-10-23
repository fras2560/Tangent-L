package mq;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;
import query.TermCountPair;
import utilities.Constants;

public class Mathquery extends CustomScoreQuery{
    ArrayList<TermCountPair> termCounts;
    float denominator;
    float sizeOfQuery;
    TermCountPair term;
    String field;
    public Mathquery(Query subQuery, ArrayList<TermCountPair> terms, String field) {
        super(subQuery);
        this.sizeOfQuery = 0;
        // remember the query
        for (TermCountPair tcp : terms){
            this.sizeOfQuery += tcp.getCount();
        }
        this.termCounts = terms;
        this.field = field;
    }
    
    protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException{
        return new MathqueryProvider(Constants.FIELD,
                                     context,
                                     this.termCounts);
    }
}
