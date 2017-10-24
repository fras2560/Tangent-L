package query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.util.BytesRef;

import utilities.Functions;

public class DiceScoreProvider extends CustomScoreProvider{
    private String _field;
    private LeafReaderContext context;
    private List<TermCountPair> terms;
    public DiceScoreProvider(String field,
                             LeafReaderContext context,
                             List<TermCountPair> terms) {
        super(context);
        this._field = field;
        this.context = context;
        this.terms = terms;
    }

    public Map<Float, Float> lookupTermPosition(LeafReader reader, int doc, BytesRef term) throws IOException{
        PostingsEnum posting = reader.postings(new Term(this._field, term), PostingsEnum.POSITIONS);
        Map<Float, Float>positions = new HashMap<Float, Float>();
        if (posting != null){
            // move to the document currently looking at
            posting.advance(doc);
            // get a dictionary of the each potential formula to match to and total number of matches
            int count = 0;
            while (count < posting.freq()){
                
                Float position = new Float(posting.nextPosition());
                Float freq = positions.get(position);
                if(freq == null){
                    positions.put(position, new Float(1));
                }else{
                    positions.put(position, freq + new Float(1));
                }
                count += 1;
            }
        }
        return positions;
    }

    public Map<Float, Float> calculateFormulaSizes(LeafReader reader, int doc, Map<Float, Float> formulaSizes) throws IOException{
     // now determine the size of each formula
        Terms termVector = reader.getTermVector(doc, _field);
        TermsEnum termsEnum = null;
        termsEnum = termVector.iterator();
        Map<String, Boolean> termsCalculated = new HashMap<String, Boolean>();
        // loop through all terms in the vector
        while((termsEnum.next()) != null) {
            // check if terms positions have already been considered
            if (termsCalculated.get(termsEnum.term().utf8ToString()) == null &&
                !Functions.containsWildcard(termsEnum.term().utf8ToString())){
                // no then remember it has been used
                termsCalculated.put(termsEnum.term().utf8ToString(), new Boolean(true));
                Map<Float, Float> pos = this.lookupTermPosition(reader, doc, termsEnum.term());
                for (Map.Entry<Float, Float> entry : pos.entrySet()) {
                    // see if the part of a formula the matches some tokens
                    Float freq = formulaSizes.get(entry.getKey());
                    if (freq != null){
                        formulaSizes.put(entry.getKey(), freq + entry.getValue());
                    }
                }
            }
        }
        return formulaSizes;
    }
    public float customScore(int doc, float subQueryScore, float valSrcScores [])  throws IOException {
        // subQueryScore is term frequency of the term
        LeafReader reader = this.context.reader();
        float score = 0f;
        float querySize = 0f;
        Map<Float, Float> queryLookup = new HashMap<Float, Float>();
        Map<Float, Float> formulaSizes = new HashMap<Float, Float>();
        if (reader != null){
            // find a list of formulas and their positions
            Float bestFormula = new Float(0);
            for (TermCountPair term : this.terms){
                System.out.println(term.getTerm());
                querySize += term.getCount();
                PostingsEnum posting = reader.postings(new Term(this._field, term.getTerm()), PostingsEnum.POSITIONS);
                if (posting != null){
                    System.out.println("Posting");
                    // move to the document currently looking at
                    posting.advance(doc);
                    // get a dictionary of the each potential formula to match to and total number of matches
                    int count = 0;
                    while (count < posting.freq()){
                        Float position = new Float(posting.nextPosition());
                        Float positionCount = queryLookup.get(position);
                        if (positionCount != null){
                            // already in lookup, update and add one
                            positionCount = positionCount + new Float(1);
                            queryLookup.put(position, positionCount);
                        }else{
                            // add to lookup and formula size
                            queryLookup.put(position, new Float(1));
                            formulaSizes.put(position, new Float(0));
                        }
                        count += 1;
                    }
                    // calculate the formula sizes
                    formulaSizes = this.calculateFormulaSizes(reader, doc, formulaSizes);
                    // now calculate the best formula that was matched
//                    System.out.println("Formula matches");
//                    Functions.printMap(queryLookup);
//                    System.out.println("Formula sizes");
//                    Functions.printMap(formulaSizes);
//                    System.out.println("Query Size:" + querySize);
                    for (Map.Entry<Float, Float> entry : queryLookup.entrySet()) {
                        // calculate the intersection
                        Float temp =  (new Float(2) * entry.getValue()) / (querySize + formulaSizes.get(entry.getKey()));
                        if (temp > bestFormula){
                            bestFormula = temp;
                        }
                    }
                }
            }
            score = bestFormula.floatValue();
        }else{
            System.err.println("Unable to find LeafReader for Dice Query");
        }
        return score; // New Score
    }

}
