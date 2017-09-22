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
import java.util.LinkedList;


/*
 * This class contains a configuration that Tangent can use when
 * when converting MathML into tuples
 * @author Dallas Fraser
 * @see ConvertMathML
 * @since 2017-09-06
 */
public class ConvertConfig {
    /*
     * The configuration parameters that are set for Tangent
     */
    private boolean shortened;
    private boolean eol;
    private boolean compound_symbols;
    private boolean terminal_symbols;
    private boolean edge_pairs;
    private boolean unbounded;
    private boolean location;
    private boolean synonyms;
    private int window_size;
    /*
     * The possible features that Tangent can use
     */
    public static String SHORTENED = "SHORTENED";
    public static String EOL = "EOL";
    public static String COMPOUND = "COMPOUND_SYMBOLS";
    public static String TERMINAL = "TERMINAL_SYMBOLS";
    public static String EDGE = "EDGE_PAIRS";
    public static String UNBOUNDED = "UNBOUNDED";
    public static String LOCATION = "LOCATION";
    public static String SYNONYMS = "SYNONYMS";
    /*
     * Class constructor
     */
    public ConvertConfig(){
        this.window_size = 1;
        this.shortened = true;
        this.eol = false;
        this.compound_symbols = false;
        this.terminal_symbols = false;
        this.edge_pairs = false;
        this.unbounded = false;
        this.location = false;
        this.synonyms = false;
    }

    @Override
    /*
     * Returns True if the two objects are equal False otherwise
     * @param o the object to check if it equal with
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }
        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof ConvertConfig)) {
            return false;
        }
        // typecast o to Complex so that we can compare data members 
        ConvertConfig c = (ConvertConfig) o;
        // Compare the data members and return accordingly
        return this.location == c.location
                && this.unbounded == c.unbounded
                && this.edge_pairs == c.edge_pairs
                && this.terminal_symbols == c.terminal_symbols
                && this.compound_symbols == c.compound_symbols
                && this.eol == c.eol
                && this.shortened == c.shortened
                && this.window_size == c.window_size
                && this.synonyms == c.synonyms;
    }

    /*
     * Flips the current attributes setting.
     * Parameter attribute is one of the static String of ConvertConfig
     * (e.g. <code> ConvertConfig.UNBOUNDED </code>)
     * @param attribute The attribute to flip
     */
    public void flipBit(String attribute){
        if (attribute.equals(ConvertConfig.SHORTENED)){
            this.shortened = !this.shortened;
        }else if(attribute.equals(ConvertConfig.EOL)){
            this.eol = !this.eol;
        }else if(attribute.equals(ConvertConfig.COMPOUND)){
            this.compound_symbols = !this.compound_symbols;
        }else if(attribute.equals(ConvertConfig.TERMINAL)){
            this.terminal_symbols = !this.terminal_symbols;
        }else if(attribute.equals(ConvertConfig.EDGE)){
            this.edge_pairs = !this.edge_pairs;
        }else if(attribute.equals(ConvertConfig.UNBOUNDED)){
            this.unbounded = !this.unbounded;
        }else if(attribute.equals(ConvertConfig.LOCATION)){
            this.location = !this.location;
        }else if (attribute.equals(ConvertConfig.SYNONYMS)){
            this.synonyms = !this.synonyms;
        }
    }

    /*
     * Updates the window size that Tangent will use
     * @param n the size of the new window
     */
    public void setWindowSize(int n){
        if (n > 0){
            this.window_size = n;
        }
    }

    /*
     * Sets the attribute to some new value
     * @param attribute The attribute to change
     * @param settting The boolean value to change it to
     */
    public void setBooleanAttribute(String attribute, boolean setting){
        if (attribute.equals(ConvertConfig.SHORTENED)){
            this.shortened = setting;
        }else if(attribute.equals(ConvertConfig.EOL)){
            this.eol = setting;
        }else if(attribute.equals(ConvertConfig.COMPOUND)){
            this.compound_symbols = setting;
        }else if(attribute.equals(ConvertConfig.TERMINAL)){
            this.terminal_symbols = setting;
        }else if(attribute.equals(ConvertConfig.EDGE)){
            this.edge_pairs = setting;
        }else if(attribute.equals(ConvertConfig.UNBOUNDED)){
            this.unbounded = setting;
        }else if(attribute.equals(ConvertConfig.LOCATION)){
            this.location = setting;
        }else if(attribute.equals(ConvertConfig.SYNONYMS)){
            this.synonyms = setting;
        }
    }

    /*
     * Updates the config to the optimal configuration
     */
    public void optimalConfig(){
        this.compound_symbols = true;
        this.edge_pairs = true;
        this.unbounded = true;
        return;
    }

    /*
     * Returns the an Array of Commands that can be used to pass parameters to Tangent
     * @return a list of commands
     */
    public String[] toCommands(){
        LinkedList <String> commands = new LinkedList <String>();
        if (!this.shortened){
            commands.add("-shortened");
        }
        if (this.location){
            commands.add("-location");
        }
        if (this.eol){
            commands.add("-eol");
        }
        if (this.compound_symbols){
            commands.add("-compound_symbols");
        }
        if (this.terminal_symbols){
            commands.add("-terminal_symbols");
        }
        if (this.edge_pairs){
            commands.add("-edge_pairs");
        }
        if (this.unbounded){
            commands.add("-unbounded");
        }
        if (this.synonyms){
            commands.add("-synonyms");
        }
        if (this.window_size > 1){
            commands.add("-window_size");
            commands.add(Integer.toString(this.window_size));
        }
        String[] result = {};
        if (!commands.isEmpty()){
            result = commands.toArray(new String[0]); 
        }
        return result;
    }

    /*
     * Returns a copy of the ConvertConfig
     * @return a copy of the ConvertConfig
     */
    public ConvertConfig copy(){
        ConvertConfig config =  new ConvertConfig();
        config.setBooleanAttribute(ConvertConfig.COMPOUND, this.compound_symbols);
        config.setBooleanAttribute(ConvertConfig.SHORTENED, this.shortened);
        config.setBooleanAttribute(ConvertConfig.EDGE, this.edge_pairs);
        config.setBooleanAttribute(ConvertConfig.EOL, this.eol);
        config.setBooleanAttribute(ConvertConfig.TERMINAL, this.terminal_symbols);
        config.setBooleanAttribute(ConvertConfig.LOCATION, this.location);
        config.setBooleanAttribute(ConvertConfig.UNBOUNDED, this.unbounded);
        config.setWindowSize(this.window_size);
        return config;
    }

    /*
     * Getter for synonyms
     * @returns boolean True if synonyms are to be used
     */
    public boolean getSynonym(){
        return this.synonyms;
    }

    /*
     * Returns a String representation of the object
     * @return a String representation
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        String result = "";
        if (!this.shortened){
            result = result + " -shortened";
        }
        if (this.location){
            result = result + " -location";
        }
        if (this.eol){
            result = result + " -eol";
        }
        if (this.compound_symbols){
            result = result +  " -compound_symbols";
        }
        if (this.terminal_symbols){
            result = result + " -terminal_symbols";
        }
        if (this.edge_pairs){
            result = result + " -edge_pairs";
        }
        if (this.unbounded){
            result = result + " -unbounded";
        }
        if (this.synonyms){
            result = result + " -synonyms";
        }
        if (this.window_size > 1){
            result = result + " -window_size " + Integer.toString(this.window_size);
        }
        
        if (result.equals("")){
            result = "base";
        }
        return result;
    }
}
