package index;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileStatistics {
    private List<Float> formulas;
    private float words;

    public FileStatistics(InputStreamReader is) throws IOException{
        int character;
        String token = "";
        boolean math = false;
        float formulaSize = 0f;
        this.words = 0;
        this.formulas = new ArrayList<Float>();
        while ((character = is.read()) != -1){
            if (Character.isWhitespace(character)){
                if (token.startsWith("NUMBER SIGN") && token.endsWith("NUMBER SIGN") && !token.equals("NUMBER SIGN")){
                    // just count the number of math tuples
                    formulaSize += 1f;
                    math = true;
                    token = "";
                }else{
                    if (math == true){
                        // add the math formula size
                        this.formulas.add(new Float(formulaSize));
                        System.out.println("Formula size: " + formulaSize);
                        formulaSize = 0f;
                    }
                    // now not a previous math term
                    math = false;
                    // assume a word
                    if (!token.equals("")){
                        this.words += 1f;
                    }
                    token = "";
                }
            }else{
                // build up the token
                token += Character.getName(character);
                token = token.trim();
            }
        }
    }

    public float getWordCount(){
        return this.words;
    }

    public float getFormulaCount(){
        Float total = new Float(0);
        for (Float formula : this.formulas){
            total += formula;
        }
        return total.floatValue(); 
    }

    public float averageFormulaSize(){
        Float total = new Float(0);
        for (Float formula : this.formulas){
            total += formula;
        }
        Float average = total / new Float(this.formulas.size());
        return average.floatValue(); 
    }

    public float maxFormulaSize(){
        Float maxFormula = new Float(0);
        for (Float formula : this.formulas){
            if (formula >= maxFormula){
                maxFormula = formula;
            }
            
        }
        return maxFormula.floatValue();
    }
}
