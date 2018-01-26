package programs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
import search.Search.SearchConfigException;
import utilities.ProjectLogger;

public class EvaluateFeatures extends FindOptimal{
    private double bestMRR;
    private ConvertConfig bestConfig;
    private ConvertConfig base;
    public EvaluateFeatures(Path documents, Path index, BufferedWriter output, Path queries, Path results)
            throws IOException, InterruptedException, XPathExpressionException, ParserConfigurationException,
            SAXException {
        super(documents, index, output, queries, results);
        // TODO Auto-generated constructor stub
        this.bestMRR = 0d;
    }

    public EvaluateFeatures(Path documents,
                            Path indexDirectory,
                            BufferedWriter outputWriter,
                            Path queries,
                            Path results,
                            boolean b) throws XPathExpressionException,
                                              IOException,
                                              InterruptedException,
                                              ParserConfigurationException,
                                              SAXException {
        super(documents, indexDirectory, outputWriter, queries, results, b);
        this.bestMRR = 0d;
        this.evaulateAtDocumentLevel();;
    }

    /**
     * evaluate a list of features and output the average gain (mrr) occurred by the feature
     * @param features the list of features
     * @throws IOException
     * @throws XPathExpressionException
     * @throws ConvertConfigException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws SearchConfigException
     */
    public void evaluateFeatures(List<String> features, ConvertConfig baseConfig) throws IOException,
                                                                                         XPathExpressionException,
                                                                                         ConvertConfigException,
                                                                                         InterruptedException,
                                                                                         ParserConfigurationException,
                                                                                         SAXException,
                                                                                         SearchConfigException{
        this.base = baseConfig;
        this.logger.log(Level.INFO, "Scoring all features");
        Map<String, Double> scoreLookup = this.scoresIndexes(features);
        this.logger.log(Level.INFO, "Done scoring all features");
        Double count;
        Double sumDelta;
        Double delta;
        Double mrrPos;
        Double mrrNeg;
        Double one = new Double(1d);
        ConvertConfig config;
        List<String> copyFeatures = new ArrayList<String>(features);
        for(String feature: features){
            this.output.write(feature + ":");
            count = new Double(0d);
            sumDelta = new Double(0d);
            copyFeatures.remove(feature);
            // now check it versus all its input
            for(int i = 0; i < Math.pow(2, copyFeatures.size()); i++){
                try {
                    config = this.initConfig(i, copyFeatures);
                    if(feature.equals(config.getAttribute(ConvertConfig.SHORTENED)) &&
                       config.getAttribute(ConvertConfig.UNBOUNDED) == false){
                        // only care about shortened when it is unbounded
                        throw new ConfigException("Skipping this config");
                    }
                    if(feature.equals(config.getAttribute(ConvertConfig.UNBOUNDED)) &&
                       config.getAttribute(ConvertConfig.SHORTENED) == true){
                        // should have another config that tests for this
                        throw new ConfigException("Skipping this config");
                    }
                    // get score when feature is true
                    config.setBooleanAttribute(feature, true);
                    mrrPos = scoreLookup.get(config.toString());
                    // get score when feature is false
                    config.setBooleanAttribute(feature, false);
                    mrrNeg = scoreLookup.get(config.toString());
                    // output the difference
                    delta = (mrrPos - mrrNeg);
                    sumDelta += delta;
                    this.output.write(delta.toString() + ",");
                    count += one;
                } catch (ConfigException e) {
                    // TODO Auto-generated catch block
                    System.out.println("Skipped config");
                }
            }
            this.output.write(new Double(sumDelta / count).toString());
            this.output.newLine();
            copyFeatures.add(feature);
        }
        this.output.write("Best config: " + this.bestConfig + " @ " + this.bestMRR);
        this.output.newLine();
    }


    /**
     * Returns a map for looking up the scores of each config
     * It uses the config string as the key
     * @param featureList the list of features to score 
     * @return Map
     * @throws IOException
     * @throws ConvertConfigException
     * @throws XPathExpressionException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws SearchConfigException
     */
    public Map<String, Double> scoresIndexes(List<String> featureList) throws IOException,
                                                                              ConvertConfigException,
                                                                              XPathExpressionException,
                                                                              InterruptedException,
                                                                              ParserConfigurationException,
                                                                              SAXException,
                                                                              SearchConfigException{
        Map<String, Double> scorings = new HashMap<String, Double>();
        ConvertConfig config;
        Path index;
        double[] scores;
        for(int i = 0; i < Math.pow(2, featureList.size()); i++){
            this.logger.log(Level.INFO, "completed " + i + " of " + Math.pow(2, featureList.size()));
            try {
                config = this.initConfig(i, featureList);
                index = this.createIndex(config);
                scores = this.scoreIndex(index, config);
                scorings.put(config.toString(), new Double(scores[0]));
                if(scores[1] > this.bestMRR){
                    this.bestMRR = scores[1];
                    this.bestConfig = config;
                }
            } catch (ConfigException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                System.out.println("Skip config");
            }

        }
        return scorings;
    }

    /**
     * Initializes a config file based on the bitstring and the list of features
     * @param bitString the bitstring
     * @param featureList the list of features
     * @return ConvertConfig
     * @throws ConfigException 
     */
    public ConvertConfig initConfig(int i, List<String> featureList) throws ConfigException{
        String bitString = this.generateBitString(i, featureList.size());
        ConvertConfig config = this.base.copy();
        
        for (int pos = 0; pos <  bitString.length(); pos ++){
            if(bitString.charAt(pos) == '0'){
                config.setBooleanAttribute(featureList.get(pos), false);
            }else{
                config.setBooleanAttribute(featureList.get(pos), true);
            }
        }
        if(config.getAttribute(ConvertConfig.UNBOUNDED) == false && config.getAttribute(ConvertConfig.SHORTENED)){
            throw new ConfigException("Cannot shortened when not unbounded");
        }
        return config;
    }

    /**
     * generates a bit string of a certain size
     * @param i the number the bitstring represents
     * @param size the size of the bitstring should be
     * @return String
     */
    public String generateBitString(int i, int size){
        String bitString = Integer.toBinaryString(i);
        while(bitString.length() < size){
            bitString = "0" + bitString;
        }
        if (size == 0){
            // if size is zero the should just use default config
            bitString = "";
        }
        return bitString;
    }

    public static void main(String[] args) throws IOException{
        String usage = "Usage:\tjava programs.EvaluateFeatures [-indexDirectory dir] [-queries file] [-results file] [-documents dir] [-logFile file]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        // default arguments
        boolean wiki = true;
        Path documents, indexDirectory, output,queries, results, logFile;
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new java.util.Date());
        boolean formulaLevel = true;
        boolean documentLevel = true;
        if (!wiki){
            documents = Paths.get(System.getProperty("user.dir"), "resources", "documents", "arXiv");
            indexDirectory = Paths.get(System.getProperty("user.dir"), "resources", "index", "arXiv", "findOptimal");
            output = Paths.get(System.getProperty("user.dir"), "resources", "output", "arXiv", timeStamp + "evaluateFeatures.txt");
            queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-ArXiv.xml");
            results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR12-ArXiv-Math.dat");
            logFile = Paths.get(System.getProperty("user.dir"), "resources", "output", "arXiv", timeStamp + "evaluateFeatures.log");
        }else{
            if (formulaLevel){
                documents = Paths.get(System.getProperty("user.dir"), "resources", "documents", "wikipedia_formula");
                indexDirectory = Paths.get(System.getProperty("user.dir"), "resources", "index", "wikipedia_formula", "findOptimal");
                output = Paths.get(System.getProperty("user.dir"), "resources", "output", "wikipedia_formula", timeStamp + "evaluateFeatures.txt");
                queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR11-Math-Wikipedia.xml");
                if (documentLevel){
                    results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR11-wikipedia-11.txt");
                }else{
                    results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR11-wikipedia-formula-11.txt");
                }
                
                logFile = Paths.get(System.getProperty("user.dir"), "resources", "output", "wikipedia_formula", timeStamp + "evaluateFeatures.log");
            }else{
                documents = Paths.get(System.getProperty("user.dir"), "resources", "documents", "wikipedia");
                indexDirectory = Paths.get(System.getProperty("user.dir"), "resources", "index", "wikipedia", "findOptimal");
                output = Paths.get(System.getProperty("user.dir"), "resources", "output", "wikipedia", timeStamp + "evaluateFeatures.txt");
                queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR11-Math-Wikipedia.xml");
                results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR11-wikipedia-11.txt");
                logFile = Paths.get(System.getProperty("user.dir"), "resources", "output", "wikipedia", timeStamp + "evaluateFeatures.log");
            }
        }
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
        ProjectLogger.setLevel(Level.INFO);
        ProjectLogger.setLogFile(logFile);
        // set the config file
        ConvertConfig config = new ConvertConfig();
        // lay out what features to use
        ArrayList<String> features = new ArrayList<String>();
        config.flipBit(ConvertConfig.SYNONYMS);
        // config.setMathBM25(true);
        features.add(ConvertConfig.SHORTENED);
        features.add(ConvertConfig.TERMINAL);
        features.add(ConvertConfig.COMPOUND);
        features.add(ConvertConfig.EDGE);
        features.add(ConvertConfig.UNBOUNDED);
        features.add(ConvertConfig.EXPAND_LOCATION);
        BufferedWriter outputWriter = null;
        try {
            // write out the queries
            File outputText = output.toFile();
            outputText.createNewFile();
            FileOutputStream outputIS = new FileOutputStream(outputText);
            OutputStreamWriter osw = new OutputStreamWriter(outputIS);
            outputWriter = new BufferedWriter(osw);
            // find the optimal
            EvaluateFeatures fo;
            fo = new EvaluateFeatures(documents,
                                      indexDirectory,
                                      outputWriter,
                                      queries,
                                      results,
                                      false);
            if(formulaLevel && !documentLevel){
                fo.evaulateAtFormulaLevel();
            }
            fo.evaluateFeatures(features, config);
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
            System.err.println("Index did not have config file");
            e.printStackTrace();
            if(outputWriter != null){
                outputWriter.close();
            }
        }
    }
    private class ConfigException extends Exception {
        public ConfigException(String message) {
            super(message);
        }
    }
}
