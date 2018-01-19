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


/**
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
    private boolean compoundSymbols;
    private boolean terminalSymbols;
    private boolean edgePairs;
    private boolean unbounded;
    private boolean location;
    private boolean synonyms;
    private boolean symbolPairs;
    private boolean query;
    private int windowSize;
    private boolean bags;
    private String queryType;
    private boolean boostedQueries;
    private boolean boostLocation;
    private boolean seperate;
    private boolean expandLocation;
    /*
     * The possible features that Tangent can use
     */
    public final static String BOOST_LOCATION = "BOOST_LOCATION";
    public final static String SHORTENED = "SHORTENED";
    public final static String EOL = "EOL";
    public final static String COMPOUND = "COMPOUND_SYMBOLS";
    public final static String TERMINAL = "TERMINAL_SYMBOLS";
    public final static String EDGE = "EDGE_PAIRS";
    public final static String UNBOUNDED = "UNBOUNDED";
    public final static String LOCATION = "LOCATION";
    public final static String EXPAND_LOCATION = "EXPAND_LOCATION";
    public final static String SYNONYMS = "SYNONYMS";
    public final static String SYMBOLS = "SYMBOL_PAIRS";
    public final static String BAGS_OF_WORDS = "BAG_OF_WORDS";
    public final static String DICE_QUERY = "DICE_QUERY";
    public final static String BM25TP_QUERY = "BM25TP_QUERY";
    public final static String BM25_DISTANCE_QUERY = "BM25_DISTANCE_QUERY";
    public final static String TERM_QUERY = "TERM_QUERY";
    public final static String BOOST_QUERIES = "BOOST_QUERIES";
    public final static String SEPERATE_MATH_TEXT = "SEPERATE_MATH_FROM_TEXT";
    public final static String TOMPA_QUERY = "BM25_TOMPA_QUERY";
    public final static String DIFFERENT_WEIGHTED_QUERY = "DIFFERENT_WEIGHT_FOR_MATH_AND_TEXT_QUERY";
    private final static String DELIMINTER = "-";
    private final static String SEPERATOR = ":";
    private final static String WINDOW_SIZE = "WINDOW_SIZE";
    private final static String QUERY = "QUERY";
    private static String FILENAME = "index.config";
    /**
     * Class constructor
     */
    public ConvertConfig(){
        this.initConfig();
    }

    private void initConfig(){
        this.windowSize = 1;
        this.shortened = true;
        this.eol = false;
        this.compoundSymbols = false;
        this.terminalSymbols = false;
        this.edgePairs = false;
        this.unbounded = false;
        this.location = false;
        this.synonyms = false;
        this.symbolPairs = true;
        this.query = false;
        this.bags = false;
        this.boostedQueries = false;
        this.boostLocation = false;
        this.seperate = false;
        this.expandLocation = false;
        this.queryType = ConvertConfig.TERM_QUERY;
    }

    public ConvertConfig getSearchConfig(){
        ConvertConfig searchConfig = this.copy();
        searchConfig.setBooleanAttribute(ConvertConfig.SYNONYMS, false);
        searchConfig.query = true;
        return searchConfig;
    }

    @Override
    /**
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
                && this.edgePairs == c.edgePairs
                && this.terminalSymbols == c.terminalSymbols
                && this.compoundSymbols == c.compoundSymbols
                && this.eol == c.eol
                && this.shortened == c.shortened
                && this.windowSize == c.windowSize
                && this.synonyms == c.synonyms
                && this.symbolPairs == c.symbolPairs;
    }

    /**
     * Flips the current attributes setting.
     * Parameter attribute is one of the static String of ConvertConfig
     * NOTE: EXPAND_LOCATION will also flip location
     * (e.g. <code> ConvertConfig.UNBOUNDED </code>)
     * @param attribute The attribute to flip
     */
    public void flipBit(String attribute){
        if (attribute.equals(ConvertConfig.SHORTENED)){
            this.shortened = !this.shortened;
        }else if (attribute.equals(ConvertConfig.EOL)){
            this.eol = !this.eol;
        }else if (attribute.equals(ConvertConfig.COMPOUND)){
            this.compoundSymbols = !this.compoundSymbols;
        }else if (attribute.equals(ConvertConfig.TERMINAL)){
            this.terminalSymbols = !this.terminalSymbols;
        }else if (attribute.equals(ConvertConfig.EDGE)){
            this.edgePairs = !this.edgePairs;
        }else if (attribute.equals(ConvertConfig.UNBOUNDED)){
            this.unbounded = !this.unbounded;
        }else if (attribute.equals(ConvertConfig.LOCATION)){
            this.location = !this.location;
        }else if (attribute.equals(ConvertConfig.SYNONYMS)){
            this.synonyms = !this.synonyms;
        }else if (attribute.equals(ConvertConfig.SYMBOLS)){
            this.symbolPairs = !this.symbolPairs;
        }else if (attribute.equals(ConvertConfig.BAGS_OF_WORDS)){
            this.bags = !this.bags;
        }else if (attribute.equals(ConvertConfig.BOOST_QUERIES)){
            this.boostedQueries = !this.boostedQueries;
        }else if (attribute.equals(ConvertConfig.BOOST_LOCATION)){
            this.boostLocation = !this.boostLocation;
        }else if (attribute.equals(ConvertConfig.SEPERATE_MATH_TEXT)){
            this.seperate = !this.seperate;
        }else if (attribute.equals(ConvertConfig.EXPAND_LOCATION)){
            this.expandLocation = !this.expandLocation;
            this.location = this.expandLocation;
            
        }
    }

    /**
     * sets the query type to be used
     * @param queryType the type of query to use
     */
    public void setQueryType(String queryType) throws Exception{
        if (queryType.equals(ConvertConfig.DICE_QUERY) ||
            queryType.equals(ConvertConfig.BM25TP_QUERY) ||
            queryType.equals(ConvertConfig.BM25_DISTANCE_QUERY) ||
            queryType.equals(ConvertConfig.TERM_QUERY) ||
            queryType.equals(ConvertConfig.TOMPA_QUERY) ||
            queryType.equals(ConvertConfig.DIFFERENT_WEIGHTED_QUERY)){
            this.queryType = queryType;
        }else{
            throw new Exception("Query type was not recgonized");
        }
    }

    /**
     * get the query type to be used
     * @return the query type to be used
     */
    public String getQueryType(){
        return this.queryType;
    }

    /**
     * Updates the window size that Tangent will use
     * @param n the size of the new window
     */
    public void setWindowSize(int n){
        if (n > 0){
            this.windowSize = n;
        }
    }
    /**
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
            result = this.compoundSymbols;
        }else if(attribute.equals(ConvertConfig.TERMINAL)){
            result = this.terminalSymbols;
        }else if(attribute.equals(ConvertConfig.EDGE)){
            result = this.edgePairs;
        }else if(attribute.equals(ConvertConfig.UNBOUNDED)){
            result = this.unbounded;
        }else if(attribute.equals(ConvertConfig.LOCATION)){
            result = this.location;
        }else if (attribute.equals(ConvertConfig.SYNONYMS)){
            result = this.synonyms;
        }else if (attribute.equals(ConvertConfig.SYMBOLS)){
            result = this.symbolPairs;
        }else if(attribute.equals(ConvertConfig.BAGS_OF_WORDS)){
            result = this.bags;
        }else if (attribute.equals(ConvertConfig.BOOST_QUERIES)){
            result  = this.boostedQueries;
        }else if (attribute.equals(ConvertConfig.BOOST_LOCATION)){
            result = this.boostLocation;
        }else if (attribute.equals(ConvertConfig.SEPERATE_MATH_TEXT)){
            result = this.seperate;
        }else if (attribute.equals(ConvertConfig.EXPAND_LOCATION)){
            result = this.expandLocation;
        }
        return result;
    }

    /**
     * Getter for window size
     * @return int the window size
     */
    public int getWindowsSize(){
        return this.windowSize;
    }

    /**
     * Sets the attribute to some new value
     * @param attribute The attribute to change
     * @param setting The boolean value to change it to
     */
    public void setBooleanAttribute(String attribute, boolean setting){
        if (attribute.equals(ConvertConfig.SHORTENED)){
            this.shortened = setting;
        }else if(attribute.equals(ConvertConfig.EOL)){
            this.eol = setting;
        }else if(attribute.equals(ConvertConfig.COMPOUND)){
            this.compoundSymbols = setting;
        }else if(attribute.equals(ConvertConfig.TERMINAL)){
            this.terminalSymbols = setting;
        }else if(attribute.equals(ConvertConfig.EDGE)){
            this.edgePairs = setting;
        }else if(attribute.equals(ConvertConfig.UNBOUNDED)){
            this.unbounded = setting;
        }else if(attribute.equals(ConvertConfig.LOCATION)){
            this.location = setting;
        }else if(attribute.equals(ConvertConfig.SYNONYMS)){
            this.synonyms = setting;
        }else if (attribute.equals(ConvertConfig.SYMBOLS)){
            this.symbolPairs = setting;
        }else if(attribute.equals(ConvertConfig.BAGS_OF_WORDS)){
            this.bags = setting;
        }else if (attribute.equals(ConvertConfig.BOOST_QUERIES)){
            this.boostedQueries = setting;
        }else if (attribute.equals(ConvertConfig.BOOST_LOCATION)){
            this.boostLocation = setting;
        }else if (attribute.equals(ConvertConfig.SEPERATE_MATH_TEXT)){
            this.seperate = setting;
        }else if (attribute.equals(ConvertConfig.EXPAND_LOCATION)){
            this.expandLocation = setting;
            this.location = setting;
        }
    }

    /**
     * Updates the config to the optimal configuration
     */
    public void optimalConfig(){
        this.compoundSymbols = true;
        this.edgePairs = true;
        this.unbounded = true;
        return;
    }

    /**
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
        if (this.compoundSymbols){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.COMPOUND.toLowerCase());
        }
        if (this.terminalSymbols){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.TERMINAL.toLowerCase());
        }
        if (this.edgePairs){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.EDGE.toLowerCase());
        }
        if (this.unbounded){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.UNBOUNDED.toLowerCase());
        }
        if (this.synonyms){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.SYNONYMS.toLowerCase());
        }
        if (!this.symbolPairs){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.SYMBOLS.toLowerCase());
        }
        if (this.expandLocation){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.EXPAND_LOCATION.toLowerCase());
        }
        if (this.windowSize > 1){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.WINDOW_SIZE.toLowerCase());
            commands.add(Integer.toString(this.windowSize));
        }
        if (this.query){
            commands.add(ConvertConfig.DELIMINTER + ConvertConfig.QUERY.toLowerCase());
        }
        String[] result = {};
        if (!commands.isEmpty()){
            result = commands.toArray(new String[0]); 
        }
        return result;
    }

    /**
     * Returns a copy of the ConvertConfig
     * @return a copy of the ConvertConfig
     */
    public ConvertConfig copy(){
        ConvertConfig config =  new ConvertConfig();
        config.setBooleanAttribute(ConvertConfig.COMPOUND, this.compoundSymbols);
        config.setBooleanAttribute(ConvertConfig.SHORTENED, this.shortened);
        config.setBooleanAttribute(ConvertConfig.EDGE, this.edgePairs);
        config.setBooleanAttribute(ConvertConfig.EOL, this.eol);
        config.setBooleanAttribute(ConvertConfig.TERMINAL, this.terminalSymbols);
        config.setBooleanAttribute(ConvertConfig.LOCATION, this.location);
        config.setBooleanAttribute(ConvertConfig.UNBOUNDED, this.unbounded);
        config.setBooleanAttribute(ConvertConfig.SYMBOLS, this.symbolPairs);
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, this.synonyms);
        config.setBooleanAttribute(ConvertConfig.BAGS_OF_WORDS, this.bags);
        config.setBooleanAttribute(ConvertConfig.EXPAND_LOCATION, this.expandLocation);
        config.setBooleanAttribute(ConvertConfig.BOOST_QUERIES, this.boostedQueries);
        config.setBooleanAttribute(ConvertConfig.BOOST_LOCATION, this.boostLocation);
        config.setBooleanAttribute(ConvertConfig.SEPERATE_MATH_TEXT, this.seperate);
        config.setWindowSize(this.windowSize);
        try {
            config.setQueryType(this.queryType);
        } catch (Exception e) {
            // really this should never been thrown since it would have been thrown already
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return config;
    }

    /**
     * Checks if the this config file is compatible with the given config
     * @param config the configuration to check is compatible
     * @return boolean True if this is compatible with config
     */
    public boolean compatible(ConvertConfig config){
        // assume compatible
        boolean result = true;
        if ((this.queryType.equals(ConvertConfig.DICE_QUERY) == true) &&
            (config.synonyms == false || config.bags == false)){
            // using dice requires bag of words and synonyms expansion
            result = false;
        }else if (config.shortened != this.shortened){
            // shortened is no backwards compatible
            result = false;
        }else if (config.location != this.location && this.expandLocation != true){
            // if expand location is true than do have  backwards compatible
            // otherwise we dont
            result = false;
        }else if(this.expandLocation != true && config.expandLocation != this.location){
            // expand Location is backwards compatible
            result = false;
        }else if(this.eol != true && this.eol != config.eol){
            // eol is backwards compatible
            result = false;
        }else if(this.compoundSymbols != true && this.compoundSymbols != config.compoundSymbols){
            // compound symbol is backwards compatible
            result = false;
        }else if(this.terminalSymbols != true && this.terminalSymbols != config.terminalSymbols){
            // terminal symbol is backwards compatible
            result = false;
        }else if(this.edgePairs != true && this.edgePairs != config.edgePairs){
            // edge pairs is backwards compatible
            result = false;
        }else if(this.synonyms != true && this.synonyms != config.synonyms){
            // synonyms is backwards compatible
            result = false;
        }else if(this.unbounded != true && this.unbounded != config.unbounded){
            // unbounded is backwards compatible
            result = false;
        }else if (this.windowSize < config.windowSize){
            // window size should be bigger or same size
            // unbounded is not a substitute for this
            result = false;
        }else if (this.symbolPairs != true && this.symbolPairs != config.symbolPairs){
            // symbol pairs is backwards compatible
            result = false;
        }
        return result;
    }

    /**
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
        fileWriter.write(ConvertConfig.COMPOUND + ConvertConfig.SEPERATOR + this.compoundSymbols);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.EDGE + ConvertConfig.SEPERATOR + this.edgePairs);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.EOL + ConvertConfig.SEPERATOR + this.eol);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.LOCATION + ConvertConfig.SEPERATOR + this.location);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.SHORTENED + ConvertConfig.SEPERATOR + this.shortened);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.SYNONYMS + ConvertConfig.SEPERATOR + this.synonyms);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.TERMINAL + ConvertConfig.SEPERATOR + this.terminalSymbols);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.UNBOUNDED + ConvertConfig.SEPERATOR + this.unbounded);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.WINDOW_SIZE + ConvertConfig.SEPERATOR + this.windowSize);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.SYMBOLS + ConvertConfig.SEPERATOR + this.symbolPairs);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.EXPAND_LOCATION + ConvertConfig.SEPERATOR + this.expandLocation);
        fileWriter.newLine();
        fileWriter.write(ConvertConfig.BAGS_OF_WORDS + ConvertConfig.SEPERATOR + this.bags);
        fileWriter.newLine();
        fileWriter.close();
    }

    /**
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

    /**
     * Returns a String representation of the object
     * @return a String representation
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        String result = "";
        if (!this.shortened){
            result = result + " " + ConvertConfig.DELIMINTER + "!" + ConvertConfig.SHORTENED;
        }
        if (!this.symbolPairs){
            result = result + " " + ConvertConfig.DELIMINTER + "!" +ConvertConfig.SYMBOLS;
        }
        if (this.location){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.LOCATION;
        }
        if (this.eol){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.EOL;
        }
        if (this.compoundSymbols){
            result = result +  " " + ConvertConfig.DELIMINTER + ConvertConfig.COMPOUND;
        }
        if (this.terminalSymbols){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.TERMINAL;
        }
        if (this.edgePairs){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.EDGE;
        }
        if (this.unbounded){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.UNBOUNDED;
        }
        if (this.synonyms){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.SYNONYMS;
        }
        if(this.bags){
            result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.BAGS_OF_WORDS;
        }
        if (this.query){
            if (this.boostedQueries){
                result = result + " " + ConvertConfig.DELIMINTER + ConvertConfig.BOOST_QUERIES;
            }
            result = result + " " + ConvertConfig.DELIMINTER + this.queryType; 
        }
        
        if (this.windowSize > 1){
            result = (result + " "+ ConvertConfig.DELIMINTER + ConvertConfig.WINDOW_SIZE + ConvertConfig.SEPERATOR +
                      Integer.toString(this.windowSize));
        }
        
        if (result.equals("")){
            result = "base";
        }
        return result;
    }
}
