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
    private ArrayList<String> allTerms;
    private ArrayList<String> terms;
    private String term;
    private float denominator;
    public DiceScoreProvider(String field, LeafReaderContext context, float denominator) {
        super(context);
        this._field = field;
        this.denominator = denominator;
    }

    public float customScore(int doc, float subQueryScore, float valSrcScores [])  throws IOException {
        System.out.println("Doc:" + doc + " Subqueryscore:" + subQueryScore + " valSrcScores:" +  valSrcScores.length + " denominator:" + this.denominator);
        return (2 * (float)subQueryScore) / this.denominator; // New Score
    }

}
