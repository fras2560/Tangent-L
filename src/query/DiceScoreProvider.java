package query;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;

public class DiceScoreProvider extends CustomScoreProvider{
    private String _field;
    private ArrayList<String> terms;
    public DiceScoreProvider(String field, LeafReaderContext context, ArrayList<String> terms) {
        super(context);
        this._field = field;
        this.terms = terms;
        // TODO Auto-generated constructor stub
    }

    public float customScore(int doc, float subQueryScore, float valSrcScores [])  throws IOException {
        IndexReader r = this.context.reader();
        float nQuery, nDocument;
        float nIntersection = 0;
        nQuery = this.terms.size();
        for (String term: this.terms){
            System.out.println(term);
        }
        System.out.println("Query score:" + subQueryScore);
        Terms terms = r.getTermVector(doc, this._field);
        // calculate dice baby
        TermsEnum termsEnum = null;
        termsEnum = terms.iterator();
        int numTerms = 0;
        while((termsEnum.next()) != null){
            numTerms ++;
        }
        return (float)(numTerms); // New Score
    }

}
