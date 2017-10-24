package mq;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import query.TermCountPair;

public class MathqueryProvider extends CustomScoreProvider{
    private String _field;
    private LeafReaderContext context;
    private TermCountPair term;
    private List<TermCountPair> terms;
    public MathqueryProvider(String field,
                             LeafReaderContext context,
                             List<TermCountPair> terms) {
        super(context);
        this._field = field;
        this.context = context;
        this.terms = terms;
    }

    public float customScore(int doc, float subQueryScore, float valSrcScores [])  throws IOException {
        // subQueryScore is term frequency of the term
        LeafReader reader = this.context.reader();
        if (reader != null){
            for (TermCountPair term : this.terms){
                PostingsEnum posting = reader.postings(new Term(this._field, term.getTerm()), PostingsEnum.POSITIONS);
                System.out.println("Term: " + term.getTerm() + " freq:" + posting.freq());
            }
        }
//        System.out.println("Doc:" + doc +
//                           " Subqueryscore: " + subQueryScore +
//                           " valSrcScores: " +  valSrcScores.length +
//                           " numberOfTerms: " + numberOfTerms +
//                           " querySize: " + this.sizeOfQuery + 
//                           " denominator: " + denominator +
//                           " intersection: " + intersection);
        return 1f; // New Score
    }
}
