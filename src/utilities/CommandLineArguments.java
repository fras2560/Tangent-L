package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to parse the command line arguments
 * @author Dallas Fraser
 * @since 2018-02-02
 *
 */
public class CommandLineArguments {
    public static String INDEXDIRECTORY = "indexDirectory";
    public static String QUERIES = "queriesFile";
    public static String JUDGEMENTS = "judgements";
    public static String DOCUMENTSDIRECTORY = "documentsDirectory";
    public static String LOGFILE = "logFile";
    public static String RESULTSFILE = "resultsFile";
    public static String QUERIESOUTPUTFILE = "queriesOutputFile";
    public static String SEPERATOR = ":";
    private Map<String, Path> configLookup;
    private Path indexPath;
    private Path queriesPath;
    private Path judgementsPath;
    private Path documentsPath;
    private Path logPath;
    private Path resultsPath;
    private Path queriesOutputPath;
    /**
     * The command Line arguments
     * @param args the string of arguments from the command line
     * @throws CommandLineException raised when the help is passed as a parameter
     * @throws IOException 
     */
    public CommandLineArguments(String[] args) throws CommandLineException, IOException{
        this(args, new ArrayList<String>());
    }

    /**
     * Loads the config file
     * that should be located at root folder in a file called config.txt
     * @return Map the lookup of the config
     * @throws IOException
     */
    public static Map<String, Path> loadConfig() throws IOException{
        Path config = Paths.get(System.getProperty("user.dir"), "config.txt");
        BufferedReader fileReader = new BufferedReader(new FileReader(config.toFile()));
        String line;
        String attribute;
        Path value;
        String[] parts;
        Map<String, Path> configLookup = new HashMap<String, Path>();
        while ((line = fileReader.readLine()) != null && !line.trim().equals("")){
            if(!line.startsWith("#")){
                parts = line.split(CommandLineArguments.SEPERATOR);
                if (parts.length != 2){
                    // not sure what the file is
                    fileReader.close();
                    throw new IOException("Unrecongizable config file");
                }
                attribute = parts[0].trim();
                value = Paths.get(parts[1].trim());
                configLookup.put(attribute, value);
            }
            
        }
        System.out.println(configLookup);
        fileReader.close();
        return configLookup;
    }

    /**
     * Constructor
     * @param args the string of arguments from the command line
     * @param required a list of required arguments
     * @throws CommandLineException
     * @throws IOException
     */
    public CommandLineArguments(String[] args, List<String> required) throws CommandLineException, IOException{
        // check command line for override default methods
        this.configLookup = CommandLineArguments.loadConfig();
        this.indexPath = null;
        this.resultsPath = null;
        this.queriesOutputPath = null;
        this.documentsPath = null;
        this.logPath = null;
        this.queriesPath = null;
        this.judgementsPath = null;
        for(int i = 0;i < args.length;i++) {
            if (args[i].equals("-h") || args[i].equals("-help")){
              throw new CommandLineException("Help was request at the command line");
            }else if (("-" + CommandLineArguments.INDEXDIRECTORY).equals(args[i])) {
                this.indexPath = Paths.get(args[i+1]);
                i++;
            } else if (("-" + CommandLineArguments.RESULTSFILE).equals(args[i])) {
                this.resultsPath = Paths.get(args[i+1]);
                i++;
            } else if (("-" + CommandLineArguments.QUERIESOUTPUTFILE).equals(args[i])) {
                this.queriesOutputPath = Paths.get(args[i+1]);
                i++;
            }else if (("-" + CommandLineArguments.DOCUMENTSDIRECTORY).equals(args[i])){
                this.documentsPath = Paths.get(args[i+1]);
                i++;
            }else if (("-" + CommandLineArguments.LOGFILE).equals(args[i])){
                this.logPath = Paths.get(args[i+1]);
                i++;
            }else if (("-" + CommandLineArguments.QUERIES).equals(args[i])){
                this.queriesPath = Paths.get(args[i+1]);
                i++;
            }else if (("-" + CommandLineArguments.JUDGEMENTS).equals(args[i])){
                this.judgementsPath = Paths.get(args[i+1]);
                i++;
            }
        }
        for (String check : required){
            if (check.equals(CommandLineArguments.INDEXDIRECTORY) && this.indexPath == null) {
                this.indexPath = this.pathOrLookup(this.indexPath, CommandLineArguments.INDEXDIRECTORY);
                if(this.indexPath ==  null){
                    throw new CommandLineException("Missing Required IndexDirectory and not listed in Config");
                }
            } else if (check.equals(CommandLineArguments.RESULTSFILE) && this.resultsPath == null) {
                this.resultsPath = this.pathOrLookup(this.resultsPath, CommandLineArguments.RESULTSFILE);
                if(this.resultsPath ==  null){
                    throw new CommandLineException("Missing Required ResultsDirectory and not listed in Config");
                }
            } else if (check.equals(CommandLineArguments.QUERIESOUTPUTFILE) && this.queriesOutputPath == null) {
                this.queriesOutputPath = this.pathOrLookup(this.queriesOutputPath,
                                                           CommandLineArguments.QUERIESOUTPUTFILE);
                if(this.queriesOutputPath ==  null){
                    throw new CommandLineException("Missing Required Queries Output File and not listed in Config");
                }
            }else if (check.equals(CommandLineArguments.DOCUMENTSDIRECTORY) && this.documentsPath == null){
                this.documentsPath = this.pathOrLookup(this.documentsPath, CommandLineArguments.DOCUMENTSDIRECTORY);
                if(this.documentsPath ==  null){
                    throw new CommandLineException("Missing Required Documents Folder and not listed in Config");
                }
            }else if (check.equals(CommandLineArguments.LOGFILE) && this.logPath == null){
                this.logPath = this.pathOrLookup(this.logPath, CommandLineArguments.LOGFILE);
                if(this.logPath ==  null){
                    throw new CommandLineException("Missing  Required Log File and not listed in Config");
                }
            }else if (check.equals(CommandLineArguments.QUERIES) && this.queriesPath == null){
                this.queriesPath  = this.pathOrLookup(this.queriesPath , CommandLineArguments.QUERIES);
                if(this.queriesPath  ==  null){
                    throw new CommandLineException("Missing Required Queries File and not listed in Config");
                }
            }else if (check.equals(CommandLineArguments.JUDGEMENTS) && this.judgementsPath == null){
                this.judgementsPath  = this.pathOrLookup(this.judgementsPath , CommandLineArguments.JUDGEMENTS);
                if(this.judgementsPath  ==  null){
                    throw new CommandLineException("Missing Required Judgements File and not listed in Config");
                }
            }
        }
    }

    /**
     * Returns a Path or Looks it up
     * @param check a possible path if null then lookup it up
     * @param argument the argument path
     * @return Path the path
     */
    public Path pathOrLookup(Path check, String argument){
        Path result = check;
        if(check == null){
            result = this.configLookup.get(argument);
        }
        System.out.println("Lookup result" + result);
        return result;
    }

    /**
     * Get the path for some parameter
     * @param parameter the parameter to get a path for
     * @return Path the resulting path for the parameter
     * @throws CommandLineException
     */
    public Path getPath(String parameter) throws CommandLineException{
        Path p = null;
        if(parameter.equals(CommandLineArguments.INDEXDIRECTORY)){
            p = this.indexPath;
        }else if(parameter.equals(CommandLineArguments.RESULTSFILE)){
            p = this.resultsPath;
        }else if(parameter.equals(CommandLineArguments.QUERIESOUTPUTFILE)){
            p = this.queriesOutputPath;
        }else if(parameter.equals(CommandLineArguments.DOCUMENTSDIRECTORY)){
            p = this.documentsPath;
        }else if(parameter.equals(CommandLineArguments.LOGFILE)){
            p = this.logPath;
        }else if(parameter.equals(CommandLineArguments.QUERIES)){
            p = this.queriesPath;
        }else if(parameter.equals(CommandLineArguments.JUDGEMENTS)){
            p = this.judgementsPath;
        }else{
            throw new CommandLineException("Unknown Command Line Parameter: " + parameter);
        }
        return p;
    }
}
