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

package programs;

import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
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
import java.util.Scanner;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import search.Search.SearchConfigException;
import utilities.CommandLineArguments;
import utilities.CommandLineException;
import utilities.ProjectLogger;

/**
 * A program to evalaute a list of features by holding each one constant.
 *
 * @author Dallas Fraser
 * @since 2018-02-02
 */
public class EvaluateFeatures extends FindOptimal {
  private double bestMrr;
  private ConvertConfig bestConfig;
  private ConvertConfig base;

  /**
   * Constructor.
   *
   * @param documents the document directory
   * @param index the path to the Lucene index
   * @param output BufferedWriter to output the results
   * @param queries file path to the queries
   * @param results file path to the results to use when judging
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   */
  public EvaluateFeatures(
      Path documents, Path index, BufferedWriter output, Path queries, Path results)
      throws IOException, InterruptedException, XPathExpressionException,
          ParserConfigurationException, SAXException {
    this(documents, index, output, queries, results, false);
  }

  /**
   * Constructor.
   *
   * @param documents the document directory
   * @param queries file path to the queries
   * @param results file path to the results to use when judging
   * @param documentLevel True if evaluate at the document level
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   */
  public EvaluateFeatures(
      Path documents,
      Path indexDirectory,
      BufferedWriter outputWriter,
      Path queries,
      Path results,
      boolean documentLevel)
      throws XPathExpressionException, IOException, InterruptedException,
          ParserConfigurationException, SAXException {
    super(documents, indexDirectory, outputWriter, queries, results, documentLevel);
    this.bestMrr = 0d;
    if (documentLevel) {
      this.evaulateAtDocumentLevel();
      ;
    }
  }

  /**
   * outputs the different between the feature being on and off.
   *
   * @param scoreLookup - lookup for a feature and its score
   * @param copyFeatures - a list of the other features that were measured
   * @param feature - the current feature being measured
   * @param measurement - what is being measure (precision, recall, etc)
   * @return the average of the feature being included
   * @throws IOException - issue with writing to file
   */
  public double outputFeatureMeasure(
      Map<String, double[]> scoreLookup, List<String> copyFeatures, String feature, int measurement)
      throws IOException {
    // now check it versus all its input
    ConvertConfig config;
    double mrrPos;
    double mrrNeg;
    double delta;
    double sumDelta = 0.0d;
    double count = 0.0d;
    for (int i = 0; i < Math.pow(2, copyFeatures.size()); i++) {
      try {
        config = this.initConfig(i, copyFeatures);
        // get score when feature is true\

        config.setBooleanAttribute(feature, true);
        mrrPos = scoreLookup.get(config.toString())[measurement];
        // get score when feature is false
        config.setBooleanAttribute(feature, false);
        if (feature.equals(ConvertConfig.SHORTENED)) {
          config.setBooleanAttribute(ConvertConfig.UNBOUNDED, false);
        }
        mrrNeg = scoreLookup.get(config.toString())[measurement];
        // output the difference
        delta = (mrrPos - mrrNeg);
        sumDelta += delta;
        this.output.write(delta + ",");
        count += 1d;
      } catch (final ConfigException e) {
        System.out.println(e.getMessage());
      }
    }
    return sumDelta / count;
  }

  /**
   * evaluate a list of features and output the average gain (mrr) occurred by the feature.
   *
   * @param features the list of features
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public void evaluateFeatures(List<String> features, ConvertConfig baseConfig)
      throws IOException, XPathExpressionException, ConvertConfigException, InterruptedException,
          ParserConfigurationException, SAXException, SearchConfigException {
    this.base = baseConfig;
    this.logger.log(Level.INFO, "Scoring all features");
    final Map<String, double[]> scoreLookup = this.scoresIndexes(features);
    this.logger.log(Level.INFO, "Done scoring all features");
    double average;
    final List<String> copyFeatures = new ArrayList<String>(features);
    for (int measurement = 0; measurement < 2; measurement++) {
      if (measurement == 0) {
        this.output.write("-------------------------------");
        this.output.newLine();
        this.output.write("         RECALL                ");
        this.output.newLine();
        this.output.write("-------------------------------");
        this.output.newLine();
      } else {
        this.output.write("-------------------------------");
        this.output.newLine();
        this.output.write("         PRECISION                ");
        this.output.newLine();
        this.output.write("-------------------------------");
        this.output.newLine();
      }
      for (final String feature : features) {
        this.output.write(feature + ":");
        copyFeatures.remove(feature);
        average = this.outputFeatureMeasure(scoreLookup, copyFeatures, feature, measurement);
        this.output.write("Total:" + Double.toString(average));
        this.output.newLine();
        copyFeatures.add(feature);
      }
    }
    this.output.newLine();
    this.output.write("Best config: " + this.bestConfig + " @ " + this.bestMrr);
    this.output.newLine();
  }

  /**
   * Returns a map for looking up the scores of each config It uses the config string as the key.
   *
   * @param featureList the list of features to score
   * @return Map a map with the key being config to the score of the config
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public Map<String, double[]> scoresIndexes(List<String> featureList)
      throws IOException, ConvertConfigException, XPathExpressionException, InterruptedException,
          ParserConfigurationException, SAXException, SearchConfigException {
    final Map<String, double[]> scorings = new HashMap<String, double[]>();
    ConvertConfig config;
    Path index;
    double[] scores;
    for (int i = 0; i < Math.pow(2, featureList.size()); i++) {
      this.logger.log(Level.INFO, "completed " + i + " of " + Math.pow(2, featureList.size()));
      try {
        config = this.initConfig(i, featureList);
        index = this.createIndex(config);
        scores = this.scoreIndex(index, config);
        scorings.put(config.toString(), scores);
        this.output.write(config.toString() + "," + scores[0] + "," + scores[1]);
        this.output.newLine();
        ;
        if (scores[1] > this.bestMrr) {
          this.bestMrr = scores[1];
          this.bestConfig = config;
        }
      } catch (final ConfigException e) {
        // e.printStackTrace();
        System.out.println(e.getMessage());
      }
    }
    return scorings;
  }

  /**
   * Initializes a config file with feature a position i as on.
   *
   * @param i - the feature at position i that is being tested
   * @param featureList the list of features
   * @return ConvertConfig config to use for indexing and searching
   * @throws ConfigException - unable to set attribute
   */
  public ConvertConfig initConfig(int i, List<String> featureList) throws ConfigException {
    final String bitString = this.generateBitString(i, featureList.size());
    final ConvertConfig config = this.base.copy();
    for (int pos = 0; pos < bitString.length(); pos++) {
      if (bitString.charAt(pos) == '0') {
        config.setBooleanAttribute(featureList.get(pos), false);
      } else {
        config.setBooleanAttribute(featureList.get(pos), true);
      }
    }
    if (config.getAttribute(ConvertConfig.SHORTENED) == true) {
      config.setBooleanAttribute(ConvertConfig.UNBOUNDED, true);
    }
    return config;
  }

  /**
   * generates a bit string of a certain size.
   *
   * @param i the number the bitstring represents
   * @param size the size of the bitstring should be
   * @return String - bit string
   */
  public String generateBitString(int i, int size) {
    String bitString = Integer.toBinaryString(i);
    while (bitString.length() < size) {
      bitString = "0" + bitString;
    }
    if (size == 0) {
      // if size is zero the should just use default config
      bitString = "";
    }
    return bitString;
  }

  /**
   * A Program that evaluates the features.
   *
   * <p>Runs an experiment used to evaluate the effectiveness. Arguments: indexDirectory : the path
   * to the index queries: a path to file with the queries results: a path to the results file to
   * use documents: a path to the documents logFile: a path to the file to log to
   *
   * @param args - see usage
   * @throws IOException - exception when parsing file
   */
  public static void main(String[] args) throws IOException {
    final String usage =
        "Usage:\tjava programs.EvaluateFeatures [-indexDirectory dir]"
            + " [-queries file] [-results file] [-documents dir] [-logFile file]";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }
    // default arguments
    Path documents;
    Path indexDirectory;
    Path output;
    Path queries;
    Path results;
    Path logFile;
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new java.util.Date());
    // parse command line arguments
    final List<String> required = new ArrayList<String>();
    required.add(CommandLineArguments.DOCUMENTSDIRECTORY);
    required.add(CommandLineArguments.INDEXDIRECTORY);
    required.add(CommandLineArguments.QUERIES);
    required.add(CommandLineArguments.JUDGEMENTS);
    required.add(CommandLineArguments.RESULTSFILE);
    CommandLineArguments cla;
    BufferedWriter outputWriter = null;
    try {
      cla = new CommandLineArguments(args, required);
      documents = cla.getPath(CommandLineArguments.DOCUMENTSDIRECTORY);
      indexDirectory = cla.getPath(CommandLineArguments.INDEXDIRECTORY);
      output =
          Paths.get(
              cla.getPath(CommandLineArguments.RESULTSFILE).toString(),
              timeStamp + "-evaluateFeatures.txt");
      queries = cla.getPath(CommandLineArguments.QUERIES);
      results = cla.getPath(CommandLineArguments.JUDGEMENTS);
      if (cla.getPath(CommandLineArguments.LOGFILE) != null) {
        logFile =
            Paths.get(
                cla.getPath(CommandLineArguments.LOGFILE).toString(),
                timeStamp = "-evaluateFeatures.log");
        ProjectLogger.setLogFile(logFile);
      }
      // get some input from the user
      boolean formulaLevel = false;
      final ConvertConfig config = new ConvertConfig();
      final ArrayList<String> features = new ArrayList<String>();
      // get the users what features to run and what level to perform at
      // lay out what features to use
      config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
      config.setBooleanAttribute(ConvertConfig.UNBOUNDED, true);
      final Scanner reader = new Scanner(System.in); // Reading from System.in
      System.out.println("Evaluate at Formula Level(y/n): ");
      String readLine = reader.nextLine().trim().toLowerCase();
      if (readLine.equals("y") || readLine.equals("yes")) {
        formulaLevel = true;
      }
      System.out.println("Use treat formula as bags (y/n)");
      readLine = reader.nextLine().trim().toLowerCase();
      if (readLine.equals("y") || readLine.equals("yes")) {
        config.setBooleanAttribute(ConvertConfig.BAGS_OF_WORDS, true);
      } else {
        config.setBooleanAttribute(ConvertConfig.BAGS_OF_WORDS, false);
      }
      System.out.println("Use Unbound Window Size with no Path (y/n)");
      readLine = reader.nextLine().trim().toLowerCase();
      if (readLine.equals("y") || readLine.equals("yes")) {
        features.add(ConvertConfig.SHORTENED);
      }
      System.out.println("Use Unbound Window Size with shortened Path (y/n)");
      readLine = reader.nextLine().trim().toLowerCase();
      if (readLine.equals("y") || readLine.equals("yes")) {
        features.add(ConvertConfig.UNBOUNDED);
      }
      System.out.println("Use Terminal Symbols (y/n)");
      readLine = reader.nextLine().trim().toLowerCase();
      if (readLine.equals("y") || readLine.equals("yes")) {
        features.add(ConvertConfig.TERMINAL);
      }
      System.out.println("Use Compound Symbols (y/n)");
      readLine = reader.nextLine().trim().toLowerCase();
      if (readLine.equals("y") || readLine.equals("yes")) {
        features.add(ConvertConfig.COMPOUND);
      }
      System.out.println("Use Edge Pairs (y/n)");
      readLine = reader.nextLine().trim().toLowerCase();
      if (readLine.equals("y") || readLine.equals("yes")) {
        features.add(ConvertConfig.EDGE);
      }
      System.out.println("Use Expand Tuples to include Location (y/n)");
      readLine = reader.nextLine().trim().toLowerCase();
      if (readLine.equals("y") || readLine.equals("yes")) {
        features.add(ConvertConfig.EXPAND_LOCATION);
      }
      // once finished
      reader.close();
      // setup the logger
      ProjectLogger.setLevel(Level.INFO);

      // write out the queries
      final File outputText = output.toFile();
      outputText.createNewFile();
      final FileOutputStream outputIs = new FileOutputStream(outputText);
      final OutputStreamWriter osw = new OutputStreamWriter(outputIs);
      outputWriter = new BufferedWriter(osw);
      // find the optimal
      EvaluateFeatures fo;
      fo = new EvaluateFeatures(documents, indexDirectory, outputWriter, queries, results, false);
      if (formulaLevel) {
        fo.evaulateAtFormulaLevel();

      } else {
        fo.evaulateAtDocumentLevel();
      }
      config.setBooleanAttribute(ConvertConfig.BAGS_OF_WORDS, false);
      System.out.println(config);
      fo.evaluateFeatures(features, config);
      outputWriter.close();
    } catch (final CommandLineException e1) {
      System.out.println("Issues with the command line");
      e1.printStackTrace();
    } catch (final IOException e) {
      System.err.println("Problem writing to the file statsTest.txt");
      e.printStackTrace();
      if (outputWriter != null) {
        outputWriter.close();
      }
    } catch (XPathExpressionException
        | InterruptedException
        | ParserConfigurationException
        | SAXException e) {
      e.printStackTrace();
      if (outputWriter != null) {
        outputWriter.close();
      }
    } catch (final SearchConfigException e) {
      System.err.println("Config files did not match");
      e.printStackTrace();
      if (outputWriter != null) {
        outputWriter.close();
      }
    } catch (final ConvertConfigException e) {
      System.err.println("Index did not have config file");
      e.printStackTrace();
      if (outputWriter != null) {
        outputWriter.close();
      }
    }
  }

  /**
   * A Private Exception that is raised when config features are conflicting.
   *
   * @author Dallas Fraser
   * @since 2018-02-02
   */
  private class ConfigException extends Exception {
    private static final long serialVersionUID = 1L;
  }
}
