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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private boolean symbol_pairs;
    private int window_size;
    /*
     * The possible features that Tangent can use
     */
    public final static String SHORTENED = "SHORTENED";
    public final static String EOL = "EOL";
    public final static String COMPOUND = "COMPOUND_SYMBOLS";
    public final static String TERMINAL = "TERMINAL_SYMBOLS";
    public final static String EDGE = "EDGE_PAIRS";
    public final static String UNBOUNDED = "UNBOUNDED";
    public final static String LOCATION = "LOCATION";
    public final static String SYNONYMS = "SYNONYMS";
    public final static String SYMBOLS = "SYMBOL_PAIRS";
    private final static String DELIMINTER = "-";
    private final static String SEPERATOR = ":";
    private final static String WINDOW_SIZE = "WINDOW_SIZE";
    private static String FILENAME = "index.config";
    /*
     * Class constructor
     */
    public ConvertConfig(){
        this.initConfig();
    }

    private void initConfig(){
        this.window_size = 1;
        this.shortened = true;
        this.eol = false;
        this.compound_symbols = false;
        this.terminal_symbols = false;
        this.edge_pairs = false;
        this.unbounded = false;
        this.location = false;
        this.synonyms = false;
        this.symbol_pairs = true;
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
                && this.synonyms == c.synonyms
                && this.symbol_pairs == c.symbol_pairs;
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
        }else if (attribute.equals(ConvertConfig.SYMBOLS)){
            this.symbol_pairs = !this.symbol_pairs;
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
     * Getter for different attributes
     * @param attribute the Attribute to get
     * @return result the boolean value of the attribute
     */
    public boolean getAttribute(String attribute){
        // just assume false
        boolean result = false;
        if (attribute.equals(ConvertConfig.SHORTENED)){
            result = this.shortened;
        }else if(attribute.equals(ConvertConfig.EOL)){
            result = this.eol;
        }else if(attribute.equals(ConvertConfig.COMPOUND)){
            result = this.compound_symbols;
        }else if(attribute.equals(ConvertConfig.TERMINAL)){
            result = this.terminal_symbols;
        }else if(attribute.equals(ConvertConfig.EDGE)){
            result = this.edge_pairs;
        }else if(attribute.equals(ConvertConfig.UNBOUNDED)){
            result = this.unbounded;
        }else if(attribute.equals(ConvertConfig.LOCATION)){
            result = this.location;
        }else if (attribute.equals(ConvertConfig.SYNONYMS)){
            result = this.synonyms;
        }else if (attribute.equals(ConvertConfig.SYMBOLS)){
            result = this.symbol_pairs;
        }
        return result;
    }

    /*
     * Getter for window size
     * @return int the window size
     */
    public int getWindowsSize(){
        return this.window_size;
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
        }else if (attribute.equals(ConvertConfig.SYMBOLS)){
            this.symbol_pairs = setting;
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
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.SHORTENED.toLowerCase());
        }
        if (this.location){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.LOCATION.toLowerCase());
        }
        if (this.eol){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.EOL.toLowerCase());
        }
        if (this.compound_symbols){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.COMPOUND.toLowerCase());
        }
        if (this.terminal_symbols){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.TERMINAL.toLowerCase());
        }
        if (this.edge_pairs){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.EDGE.toLowerCase());
        }
        if (this.unbounded){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.UNBOUNDED.toLowerCase());
        }
        if (this.synonyms){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.SYNONYMS.toLowerCase());
        }
        if (!this.symbol_pairs){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.SYMBOLS.toLowerCase());
        }
        if (this.window_size > 1){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.WINDOW_SIZE.toLowerCase());
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
        config.setBooleanAttribute(ConvertConfig.SYMBOLS, this.symbol_pairs);
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, this.synonyms);
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
     * Checks if the this config file is compatible with the given config
     * @param config the configuration to check is compatible
     * @returns boolean True if this is compatible with config
     */
    public boolean compatible(ConvertConfig config){
        // assume compatible
        boolean result = true;
        if (config.shortened != this.shortened || config.location != this.location){
            result = false;
        }else if(this.eol != true && this.eol != config.eol){
            // eol is backwards compatible
            result = false;
        }else if(this.compound_symbols != true && this.compound_symbols != config.compound_symbols){
            // compound symbol is backwards compatible
            result = false;
        }else if(this.terminal_symbols != true && this.terminal_symbols != config.terminal_symbols){
            // terminal symbol is backwards compatible
            result = false;
        }else if(this.edge_pairs != true && this.edge_pairs != config.edge_pairs){
            // edge pairs is backwards compatible
            result = false;
        }else if(this.synonyms != true && this.synonyms != config.synonyms){
            // synonyms is backwards compatible
            result = false;
        }else if(this.unbounded != true && this.unbounded != config.unbounded){
            // unbounded is backwards compatible
            result = false;
        }else if (this.window_size < config.window_size){
            // window size should be bigger or same size
            // unbounded is not a substitute for this
            result = false;
        }else if (this.symbol_pairs != true && this.symbol_pairs != config.symbol_pairs){
            // symbol pairs is backwards compatible
            result = false;
        }
        return result;
    }

    /*
     * Saves a config file in the directory given
     * @param directory the directory to save the config file
     */
    public void saveConfig(Path directory) throws IOException{
        Path filename = Paths.get(directory.toString(), ConvertConfig.FILENAME);
        File file = filename.toFile();
        // if previous file then just remove
        if (file.exists()){
            file.delete();
        }
        // create the new file
        file.createNewFile();
        BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        fileWriter.write(ConvertConfig.COMPOUND + ConvertConfig.SEPERATOR + this.compound_symbols);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.EDGE + ConvertConfig.SEPERATOR + this.edge_pairs);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.EOL + ConvertConfig.SEPERATOR + this.eol);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.LOCATION + ConvertConfig.SEPERATOR + this.location);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.SHORTENED + ConvertConfig.SEPERATOR + this.shortened);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.SYNONYMS + ConvertConfig.SEPERATOR + this.synonyms);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.TERMINAL + ConvertConfig.SEPERATOR + this.terminal_symbols);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.UNBOUNDED + ConvertConfig.SEPERATOR + this.unbounded);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.WINDOW_SIZE + ConvertConfig.SEPERATOR + this.window_size);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.SYMBOLS + ConvertConfig.SEPERATOR + this.symbol_pairs);
        fileWriter.newLine();
        fileWriter.close();
        
    }

    /*
     * Loads a config file from the direcotyr given
     * @param directory the directory to save the config file
     */
    public void loadConfig(Path directory) throws IOException, ConvertConfigException{
        Path filename = Paths.get(directory.toString(), ConvertConfig.FILENAME);
        File file = filename.toFile();
        // check to see if file exists
        if (file.exists()){
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String line;
            String attribute;
            boolean setting;
            String[] parts;
            while ((line = fileReader.readLine()) != null && !line.trim().equals("")){
                parts = line.split(ConvertConfig.SEPERATOR);
                if (parts.length != 2){
                    // not sure what the file is
                    fileReader.close();
                    throw new IOException("Unrecongizable config file");
                }
                attribute = parts[0].trim();
                if (attribute.equals(ConvertConfig.WINDOW_SIZE)){
                    int window_size = Integer.parseInt(parts[1].trim());
                    this.setWindowSize(window_size);
                }else{
                    setting = Boolean.parseBoolean(parts[1].trim());
                    this.setBooleanAttribute(attribute, setting);
                }
                
            }
            fileReader.close();
        }else{
            throw new ConvertConfigException("Index did not have config");
        }
    }

    public class ConvertConfigException extends Exception{
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ConvertConfigException(String message){
            super(message);
        }
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
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.SHORTENED;
        }
        if (!this.symbol_pairs){
            result = result + " " + ConvertConfig.DELIMINTER + "!" +ConvertConfig.SYMBOLS;
        }
        if (this.location){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.LOCATION;
        }
        if (this.eol){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.EOL;
        }
        if (this.compound_symbols){
            result = result +  " " + ConvertConfig.DELIMINTER + ConvertConfig.COMPOUND;
        }
        if (this.terminal_symbols){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.TERMINAL;
        }
        if (this.edge_pairs){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.EDGE;
        }
        if (this.unbounded){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.UNBOUNDED;
        }
        if (this.synonyms){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.SYNONYMS;
        }
        if (this.window_size > 1){
            result = (result + " "+ ConvertConfig.DELIMINTER + ConvertConfig.WINDOW_SIZE + ConvertConfig.SEPERATOR +
                      Integer.toString(this.window_size));
        }
        if (result.equals("")){
            result = "base";
        }
        return result;
    }
}
