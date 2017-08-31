package naiveMathIndexer.index;

import java.util.LinkedList;

public class ConvertConfig {
    private boolean shortened;
    private boolean eol;
    private boolean compound_symbols;
    private boolean terminal_symbols;
    private boolean edge_pairs;
    private boolean unbounded;
    private boolean location;
    private int window_size;
    public static String SHORTENED = "SHORTENED";
    public static String EOL = "EOL";
    public static String COMPOUND = "COMPOUND_SYMBOLS";
    public static String TERMINAL = "TERMINAL_SYMBOLS";
    public static String EDGE = "EDGE_PAIRS";
    public static String UNBOUNDED = "UNBOUNDED";
    public static String LOCATION = "LOCATION";
    public ConvertConfig(){
        this.window_size = 1;
        this.shortened = true;
        this.eol = false;
        this.compound_symbols = false;
        this.terminal_symbols = false;
        this.edge_pairs = false;
        this.unbounded = false;
        this.location = false;
    }
    @Override
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
                && this.window_size == c.window_size;
    }
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
        }
    }

    public void setWindowSize(int n){
        if (n > 0){
            this.window_size = n;
        }
    }

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
        }
    }

    public void optimalConfig(){
        return;
    }

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
        if (this.window_size > 1){
            result = result + " -window_size " + Integer.toString(this.window_size);
        }
        return result;
    }
}
