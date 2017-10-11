package query;

import java.io.IOException;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;

import utilities.Constants;

public class DiceScoreProvider extends CustomScoreProvider{
    private String _field;
    private LeafReaderContext context;
    private float sizeOfQuery;
    private TermCountPair term;
    public DiceScoreProvider(String field, LeafReaderContext context, float sizeOfQuery, TermCountPair term) {
        super(context);
        this._field = field;
        this.sizeOfQuery = sizeOfQuery;
        this.context = context;
        this.term = term;
    }

    public float customScore(int doc, float subQueryScore, float valSrcScores [])  throws IOException {
        // subQueryScore is term frequency of the term
        float intersection = subQueryScore < this.term.getCount() ? subQueryScore : this.term.getCount();
        float numberOfTerms = 0f;
        LeafReader reader = this.context.reader();
        System.out.println("Reader: " + reader);
        if (reader != null){
            System.out.println(reader.getNumericDocValues(Constants.FORMULA_COUNT).get(doc));
            System.out.println(reader.getNumericDocValues(Constants.WORD_COUNT).get(doc));
            numberOfTerms = reader.getNumericDocValues(Constants.FORMULA_COUNT).get(doc);
        }
        float denominator = this.sizeOfQuery + numberOfTerms;
        System.out.println("Doc:" + doc +
                           " Subqueryscore: " + subQueryScore +
                           " valSrcScores: " +  valSrcScores.length +
                           " numberOfTerms: " + numberOfTerms +
                           " querySize: " + this.sizeOfQuery + 
                           " denominator: " + denominator +
                           " intersection: " + intersection);
        return (2 * (float) intersection) / denominator; // New Score
    }

}
