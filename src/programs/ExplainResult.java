package programs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
import search.Search;
import search.Search.SearchConfigException;
import utilities.ProjectLogger;

public class ExplainResult {
    public static void main(String[] args) throws IOException{
        String usage = "Usage:\tjava program.ExplainResult [-indexDirectory dir] [-queries file] [-results file] [-logFile file]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        // default arguments
        Path indexDirectory = Paths.get(System.getProperty("user.dir"), "resources", "index", "wikipedia", "findOptimal", "-COMPOUND_SYMBOLS-TERMINAL_SYMBOLS-EDGE_PAIRS-UNBOUNDED-SYNONYMS");
        Path output = Paths.get(System.getProperty("user.dir"), "resources", "output", "wikipedia", "explain.txt");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR11-Math-Wikipedia-Sample.xml");
        Path results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR11-wikipedia-11.txt");
        Path logFile = Paths.get(System.getProperty("user.dir"), "resources", "output", "wikipedia", "explainResults.log");
        Path documents = Paths.get(System.getProperty("user.dir"), "resources", "documents", "wikipedia");
        // check command line for override default methods
        for(int i = 0;i < args.length;i++) {
          if ("-index".equals(args[i])) {
              indexDirectory = Paths.get(args[i+1]);
              i++;
          } else if ("-results".equals(args[i])) {
              results = Paths.get(args[i+1]);
              i++;
          } else if ("-queries".equals(args[i])) {
              queries = Paths.get(args[i+1]);
              i++;
          }else if ("-documents".equals(args[i])){
              documents = Paths.get(args[i+1]);
              i++;
          }else if ("-logFile".equals(args[i])){
              logFile = Paths.get(args[i+1]);
              i++;
          }
        }
        // setup the logger
        ProjectLogger.setLevel(Level.FINER);
        ProjectLogger.setLogFile(logFile);
        // set the config file
        ConvertConfig config = new ConvertConfig();
        // lay out what features to user
        // this are all backwards compatible
        BufferedWriter outputWriter = null;
        try {
            // load the config file
            config.loadConfig(indexDirectory);
            // explain the results
            Search search = new Search(indexDirectory, config);
            search.explainQueries(queries, 2);
            // find the score
            // write out the queries
            File outputText = output.toFile();
            FileOutputStream outputIS = new FileOutputStream(outputText);
            OutputStreamWriter osw = new OutputStreamWriter(outputIS);
            outputWriter = new BufferedWriter(osw);
            FindOptimal fo = new FindOptimal(documents, indexDirectory, outputWriter, queries, results);
            double[] score = fo.scoreIndex(indexDirectory, config);
            System.out.println(score[0] + "," + score[1]);
            outputWriter.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
            if(outputWriter != null){
                outputWriter.close();
            }
        }catch (XPathExpressionException | InterruptedException | ParserConfigurationException | SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if(outputWriter != null){
                outputWriter.close();
            }
        }catch (SearchConfigException e) {
            // TODO Auto-generated catch block
            System.err.println("Config files did not match");
            e.printStackTrace();
            if(outputWriter != null){
                outputWriter.close();
            }
        } catch (ConvertConfigException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if(outputWriter != null){
                outputWriter.close();
            }
        }
    }
}
