/*
 * Copyright 2017 Dallas Fraser
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package index;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import utilities.Constants;

/**
 * This class converts a File's MathML to Tangent Tuples
 * 
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class FileStatistics {
    private List<Float> formulas;
    private float words;
    /**
     * A class to determine the filestatistics of a file
     * @param is the input stream reader of the file
     * @throws IOException
     */
    public FileStatistics(InputStreamReader is) throws IOException{
        int character;
        String token = "";
        boolean math = false;
        float formulaSize = 0f;
        this.words = 0;
        this.formulas = new ArrayList<Float>();
        while ((character = is.read()) != -1){
            if (Character.isWhitespace(character)){
                if (token.startsWith("NUMBER SIGN") &&
                    token.endsWith("NUMBER SIGN") &&
                    !token.equals("NUMBER SIGN") &&
                    !(token.equals(Constants.FORMULA_START_TAG) || token.equals(Constants.FORMULA_END_TAG))){
                    // just count the number of math tuples
                    formulaSize += 1f;
                    math = true;
                    token = "";
                }else{
                    if (math == true){
                        // add the math formula size
                        formulaSize = formulaSize  -2f; // do not include start and end tag
                        this.formulas.add(new Float(formulaSize));
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
        if (math == true){
            this.formulas.add(new Float(formulaSize));
            formulaSize = 0f;
        }
    }

    /**
     * Returns the number of words for the file
     * @return float The total number of words
     */
    public float getWordCount(){
        return this.words;
    }

    /**
     * Get the number of tuples in all the formulas
     * @return float the total number of tuples
     */
    public float getFormulaCount(){
        Float total = new Float(0);
        for (Float formula : this.formulas){
            total += formula;
        }
        return total.floatValue(); 
    }

    /**
     * Returns the average formula size of the file
     * @return float the average formula size
     */
    public float averageFormulaSize(){
        Float total = new Float(0);
        for (Float formula : this.formulas){
            total += formula;
        }
        Float average = total / new Float(this.formulas.size());
        return average.floatValue(); 
    }

    /**
     * Returns the max formula size of the file
     * @return float the max formula size
     */
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
