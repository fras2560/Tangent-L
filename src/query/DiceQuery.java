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
    public DiceQuery(Query subQuery, ArrayList<TermCountPair> terms) {
        super(subQuery);
        this.sizeOfQuery = 0;
        for (TermCountPair tcp : terms){
            System.out.println(tcp);
            this.sizeOfQuery += tcp.getCount();
        }
        System.out.println("Size of query"  + this.sizeOfQuery);
    }
    
    protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException{
        float numOfTerms = 0f;
        
        this.denominator = this.sizeOfQuery + numOfTerms;
        return new DiceScoreProvider(Constants.FIELD, context, this.denominator);
    }
}
