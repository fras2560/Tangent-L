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
import index.IndexFiles;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.xml.sax.SAXException;
import query.MathQuery;
import query.ParseQueries;
import search.Judgments;
import search.Search;
import search.Search.SearchConfigException;
import search.SearchResult;
import utilities.Functions;
import utilities.ProjectLogger;

/*
 * This class is used to be the optimal configuration of ConvertConfig.
 *
 *
 * There is a main program that can be executed to find the optimal
 *
 * @author Dallas Fraser
 * @see ConvertConfig
 * @since 2017-09-06
 */
public class FindOptimal {
  private final Path documents;
  protected Path index;
  protected BufferedWriter output;
  private ArrayList<MathQuery> mathQueries;
  private final Judgments answers;
  private static Float RELEVANT_LOWER = Float.valueOf(0);
  private static int TOP_K = 10000;
  private final Path queries;
  protected Logger logger;
  private final boolean greedy;
  private boolean formulaLevel;

  /**
   * Class Constructor.
   *
   * @param documents path to the directory of documents
   * @param index path to the directory which will hold all the created indexes
   * @param output file where findings of the program will be printed to
   * @param queries path to where the queries
   * @param results path to the ground truth of the queries
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   */
  public FindOptimal(Path documents, Path index, BufferedWriter output, Path queries, Path results)
      throws IOException, InterruptedException, XPathExpressionException,
          ParserConfigurationException, SAXException {
    this(documents, index, output, queries, results, ProjectLogger.getLogger(), true);
  }

  /**
   * Class Constructor.
   *
   * @param documents path to the directory of documents
   * @param index path to the directory which will hold all the created indexes
   * @param output file where findings of the program will be printed to
   * @param queries path to where the queries
   * @param results path to the ground truth of the queries
   * @param greedy True if a greedy algorithm should be used, False then it is exhaustive
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   */
  public FindOptimal(
      Path documents, Path index, BufferedWriter output, Path queries, Path results, boolean greedy)
      throws IOException, InterruptedException, XPathExpressionException,
          ParserConfigurationException, SAXException {
    this(documents, index, output, queries, results, ProjectLogger.getLogger(), greedy);
  }

  /**
   * Class Constructor with both logger and recall setting.
   *
   * @param documents path to the directory of documents
   * @param index path to the directory which will hold all the created indexes
   * @param output file where findings of the program will be printed to
   * @param queries path to where the queries
   * @param results path to the ground truth of the queries
   * @param logger the specified logger to use
   * @param greedy True if a greedy algorithm should be used, False then it is exhaustive
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   */
  public FindOptimal(
      Path documents,
      Path index,
      BufferedWriter output,
      Path queries,
      Path results,
      Logger logger,
      boolean greedy)
      throws IOException, InterruptedException, XPathExpressionException,
          ParserConfigurationException, SAXException {
    this.documents = documents;
    this.index = index;
    this.output = output;
    this.answers = new Judgments(results.toFile());
    this.logger = logger;
    this.queries = queries;
    this.greedy = greedy;
    this.formulaLevel = true;
  }

  /** Set to evaluate query results at the Document Level and ignore formula number. */
  public void evaulateAtDocumentLevel() {
    this.formulaLevel = false;
  }

  /** Set to evaluate query results at the Formula Level By Default this is true. */
  public void evaulateAtFormulaLevel() {
    this.formulaLevel = true;
  }

  /**
   * Updates the queries based uopon configuration.
   *
   * @param config Tangent features to be used when converting the queries
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   */
  public void updateQueries(ConvertConfig config)
      throws XPathExpressionException, ParserConfigurationException, SAXException, IOException,
          InterruptedException {
    final ParseQueries queryLoader = new ParseQueries(this.queries.toFile(), config);
    this.mathQueries = queryLoader.getQueries();
    queryLoader.deleteFile();
  }

  /**
   * finds the optimal features to be used *recursive) and outputs the results to a file.
   *
   * @param config Tangent features to be used when converting the queries
   * @param features list of features to look at
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public void optimize(ConvertConfig config, ArrayList<String> features)
      throws IOException, XPathExpressionException, InterruptedException,
          ParserConfigurationException, SAXException, SearchConfigException,
          ConvertConfigException {
    Path indexPath;
    this.updateQueries(config);
    final double[] baseLine = this.scoreIndex(this.createIndex(config), config);
    double[] results;
    this.output.write("---------------------------------------------");
    this.output.newLine();
    this.output.write("Baseline");
    this.output.newLine();
    this.output.write(config + "," + baseLine[0] + "," + baseLine[1]);
    this.output.newLine();
    this.output.write("---------------------------------------------");
    this.output.newLine();
    final double[] bestFeatureScore = {0.0, 0.0};
    String bestFeature = "";
    final ArrayList<String> keepFeatures = new ArrayList<String>();
    for (final String feature : features) {
      config.flipBit(feature);
      this.output.newLine();
      indexPath = this.createIndex(config);
      results = this.scoreIndex(indexPath, config);
      this.output.write(config + "," + results[0] + "," + results[1]);
      this.output.newLine();
      config.flipBit(feature);
      if (results[0] > baseLine[0] || results[1] > baseLine[1]) {
        keepFeatures.add(feature);
        if (results[0] > bestFeatureScore[0]) {
          bestFeatureScore[0] = results[0];
          bestFeature = feature;
        }
        if (results[1] > bestFeatureScore[1]) {
          bestFeatureScore[1] = results[1];
          bestFeature = feature;
        }
      }
    }
    if (!bestFeature.equals("")) {
      // then information to gain by a feature
      ConvertConfig newConfig;
      if (this.greedy) {
        // just flip the best feature
        newConfig = config.copy();
        newConfig.flipBit(bestFeature);
        features.remove(bestFeature);
        this.optimize(newConfig, features);
      } else {
        // branch on all features
        for (final String feature : keepFeatures) {
          // setup the new conversion configuration
          newConfig = config.copy();
          newConfig.flipBit(feature);
          // remove the feature from the list to try
          features.remove(feature);
          this.optimize(newConfig, features);
          // add it back it
          features.add(feature);
        }
      }
    } else {
      // then no information gain from any of the features
      this.output.write(
          "Best Features: "
              + config.toString()
              + " Score:"
              + bestFeatureScore[0]
              + ","
              + bestFeatureScore[1]);
      this.output.newLine();
    }
    return;
  }

  /**
   * Parses the title of the file.
   *
   * @param title title to parse
   * @return the parsed title name
   */
  public String parseTitle(String title) {
    final String[] parts = title.split("/");
    final String filename = parts[parts.length - 1];
    final String[] temp = filename.split("\\.");
    final String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
    return String.join(".", nameparts);
  }

  /**
   * Returns the reciprocal rank of the results.
   *
   * @param searcher searcher that produced the results
   * @param result - the results returned by the searcher
   * @return a the reciprocal rank (less than 1.0)
   * @throws IOException - issue with reading/writing to file
   */
  public double reciprocal_rank(IndexSearcher searcher, SearchResult result) throws IOException {
    double reciprocal = 0.0;
    String docName;
    if (result.getResults() != null) {
      final ScoreDoc[] hits = result.getResults().scoreDocs;
      double rank = 0.0;
      int count = 1;
      for (final ScoreDoc hit : hits) {
        final Document doc = searcher.doc(hit.doc);
        if (this.formulaLevel) {
          docName = this.parseTitle(doc.get("path"));
        } else {
          docName = Functions.parseDocumentName(doc.get("path"));
        }
        rank = this.answers.findResult(result.getMathQuery(), docName);
        if (rank > FindOptimal.RELEVANT_LOWER) {
          this.logger.log(Level.FINER, "Count:" + count + " Rank:" + rank + "Doc:" + doc);
          reciprocal = (double) 1 / (double) count;
          break;
        }
        count += 1;
      }
      this.logger.log(
          Level.FINEST, result.getMathQuery().getQueryName() + " Reciprocal: " + reciprocal);
    }
    return reciprocal;
  }

  /**
   * Returns 1 if the result is found.
   *
   * @param searcher searcher that produced the results
   * @param result - search result returned by the searcher
   * @return 1 if answer is found
   * @throws IOException - issue with reading/writing to file
   */
  public double found_answer(IndexSearcher searcher, SearchResult result) throws IOException {
    double found = 0.0;
    if (result.getResults() != null) {
      final ScoreDoc[] hits = result.getResults().scoreDocs;
      final ArrayList<String> filenames = new ArrayList<String>();
      for (final ScoreDoc hit : hits) {
        // build the list of filenames to check against the answers
        final Document doc = searcher.doc(hit.doc);
        if (this.formulaLevel) {
          filenames.add(this.parseTitle(doc.get("path")));
        } else {
          filenames.add(Functions.parseDocumentName(doc.get("path")));
        }
      }
      final int[] results = this.answers.recallResult(result.getMathQuery(), filenames);
      if (results[3] > 0) {
        found = 1.0;
      }
      this.logger.log(
          Level.FINEST, result.getMathQuery().getQueryName() + " Answer found: " + found);
    }
    return found;
  }

  /**
   * Remove the duplicates from the results.
   *
   * @param results the results to remove duplicates from
   * @throws IOException - issue with reading/writing to file
   */
  public void removeDuplicates(IndexSearcher searcher, SearchResult results, int size)
      throws IOException {
    ScoreDoc[] unique = new ScoreDoc[size];
    final HashMap<String, ScoreDoc> lookup = new HashMap<String, ScoreDoc>();

    if (results.getResults() != null) {
      final ScoreDoc[] hits = results.getResults().scoreDocs;
      String docName;
      int i = 0;
      for (final ScoreDoc hit : hits) {
        final Document doc = searcher.doc(hit.doc);
        docName = Functions.parseDocumentName(doc.get("path"));
        if (lookup.get(docName) == null) {
          // do not have the result
          unique[i] = hit;
          lookup.put(docName, hit);
          i += 1;
        }
        // fill up the array to a certain size
        if (i >= size) {
          break;
        }
      }
      if (i < size) {
        // shrink it down to the right size
        unique = Arrays.copyOfRange(unique, 0, i);
      }
      results.getResults().scoreDocs = unique;
    }
  }

  /**
   * Scores the index.
   *
   * @param index the path to the created index @
   * @return score of the index
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public double[] scoreIndex(Path index, ConvertConfig config)
      throws IOException, InterruptedException, XPathExpressionException,
          ParserConfigurationException, SAXException, SearchConfigException,
          ConvertConfigException {

    double foundMean = 0.0;
    double reciprocalMean = 0.0;
    final Search searcher = new Search(index, config);
    SearchResult result;
    int count = 0;
    double reciprocal;
    double found;
    this.updateQueries(config);
    for (final MathQuery mq : this.mathQueries) {
      if (this.formulaLevel) {
        result = searcher.searchQuery(mq, FindOptimal.TOP_K);
        found = this.found_answer(searcher.getSearcher(), result);
        reciprocal = this.reciprocal_rank(searcher.getSearcher(), result);
      } else {
        result = searcher.searchQuery(mq, 2 * FindOptimal.TOP_K);
        this.removeDuplicates(searcher.getSearcher(), result, FindOptimal.TOP_K);
        found = this.found_answer(searcher.getSearcher(), result);
        reciprocal = this.reciprocal_rank(searcher.getSearcher(), result);
      }

      // result.explainResults(searcher.getSearcher());
      System.out.println(mq.getQueryName());

      this.logger.log(
          Level.INFO,
          mq.getQueryName()
              + " found:"
              + found
              + " reciprocal:"
              + reciprocal
              + "Total Results:"
              + result.hitsNumber());
      this.output.write(mq.getQueryName() + "," + found + "," + reciprocal);
      this.output.newLine();
      foundMean += found;
      reciprocalMean += reciprocal;
      count += 1;
    }
    this.logger.log(
        Level.INFO,
        config.toString() + " Scores: " + (foundMean / count) + "," + (reciprocalMean / count));
    final double[] results = {foundMean / count, reciprocalMean / count};
    return results;
  }

  /**
   * Returns a path to the index that is created using the config features.
   *
   * @param config the features to be used in the index
   * @return the path to the created index
   * @throws IOException - issue with reading/writing to a file
   * @throws ConvertConfigException - issue with config
   */
  public Path createIndex(ConvertConfig config) throws IOException, ConvertConfigException {
    final String name = config.toString().replace(" ", "");
    this.logger.log(Level.INFO, "Creating index: " + "The name of config: " + name);
    final Path directoryPath = Paths.get(this.index.toString(), name);
    final File directory = directoryPath.toFile();
    // already created
    this.logger.log(Level.INFO, "" + directory + " exists: " + directory.exists());
    if (directory.exists()) {
      return directoryPath;
    }
    // look for compatible index
    final Path compatible = this.findCompatible(config);
    if (compatible != null) {
      return compatible;
    }
    if (!directory.mkdir()) {
      throw new IOException("Unable to create directory");
    }
    // now create directory just like normal
    final IndexFiles idf = new IndexFiles();
    this.logger.log(Level.INFO, "Creating Index: " + name);
    idf.indexDirectory(directoryPath, this.documents, true, config);
    this.logger.log(Level.INFO, "Directory: " + directoryPath);
    return directoryPath;
  }

  /**
   * Returns a path to an Index that used a compatible config file.
   *
   * @param config the config that needs to be compatible with index
   * @return the path to the compatible index
   * @throws ConvertConfigException - issue with config
   */
  public Path findCompatible(ConvertConfig config) throws ConvertConfigException {
    Path compatible = null;
    final File dir = new File(this.index.toString());
    final File[] directoryListing = dir.listFiles();
    final ConvertConfig indexConfig = new ConvertConfig();
    if (directoryListing != null) {
      for (final File child : directoryListing) {
        try {
          indexConfig.loadConfig(child.toPath());
        } catch (final IOException e) {
          e.printStackTrace();
          this.logger.warning("unable to load config:" + child.toPath());
        } catch (final ConvertConfigException e) {
          this.logger.warning("Folder did not have config file so was it was skipped");
        }
        if (indexConfig.compatible(config)) {
          compatible = child.toPath();
          break;
        }
      }
    }
    return compatible;
  }

  /**
   * The main entry point to the program.
   *
   * @param args - see usage
   * @throws IOException - issue with a file
   */
  public static void main(String[] args) throws IOException {
    final String usage =
        "Usage:\tjava programs.FindOptimal [-indexDirectory dir] [-queries file]"
            + "[-results file] [-documents dir] [-logFile file]";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }
    // default arguments
    final boolean wiki = true;
    final boolean formulaOnly = true;
    final boolean documentLevel = false;
    Path documents;
    Path indexDirectory;
    Path output;
    Path queries;
    Path results;
    Path logFile;
    final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new java.util.Date());
    if (!wiki) {
      documents = Paths.get("/home", "d6fraser", "Documents", "Research", "Datasets", "arXiv");
      indexDirectory =
          Paths.get(System.getProperty("user.dir"), "resources", "index", "arXiv", "findOptimal");
      output =
          Paths.get(
              System.getProperty("user.dir"),
              "resources",
              "output",
              "arXiv",
              timeStamp + "output.txt");
      queries =
          Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-ArXiv.xml");
      results =
          Paths.get(
              System.getProperty("user.dir"), "resources", "results", "NTCIR12-ArXiv-Math.dat");
      logFile =
          Paths.get(
              System.getProperty("user.dir"),
              "resources",
              "output",
              "arXiv",
              timeStamp + "findOptimal.log");
    } else {
      if (formulaOnly) {
        documents =
            Paths.get(
                "/home",
                "d6fraser",
                "Documents",
                "Research",
                "Datasets",
                "NTCIR11_wikipedia_formula");
        indexDirectory =
            Paths.get(
                System.getProperty("user.dir"),
                "resources",
                "index",
                "wikipedia_formula",
                "findOptimal");
        output =
            Paths.get(
                System.getProperty("user.dir"),
                "resources",
                "output",
                "wikipedia_formula",
                timeStamp + "output.txt");
        queries =
            Paths.get(
                System.getProperty("user.dir"), "resources", "query", "NTCIR11-Math-Wikipedia.xml");
        if (documentLevel) {
          results =
              Paths.get(
                  System.getProperty("user.dir"),
                  "resources",
                  "results",
                  "NTCIR11-wikipedia-11.txt");
        } else {
          results =
              Paths.get(
                  System.getProperty("user.dir"),
                  "resources",
                  "results",
                  "NTCIR11-wikipedia-formula-11.txt");
        }
        logFile =
            Paths.get(
                System.getProperty("user.dir"),
                "resources",
                "output",
                "wikipedia_formula",
                timeStamp + "findOptimal.log");
      } else {
        documents =
            Paths.get(
                "/home", "d6fraser", "Documents", "Research", "Datasets", "NTCIR11_wikipedia");
        indexDirectory =
            Paths.get(
                System.getProperty("user.dir"), "resources", "index", "wikipedia", "findOptimal");
        output =
            Paths.get(
                System.getProperty("user.dir"),
                "resources",
                "output",
                "wikipedia",
                timeStamp + "output.txt");
        queries =
            Paths.get(
                System.getProperty("user.dir"), "resources", "query", "NTCIR11-Math-Wikipedia.xml");
        results =
            Paths.get(
                System.getProperty("user.dir"), "resources", "results", "NTCIR11-wikipedia-11.txt");
        logFile =
            Paths.get(
                System.getProperty("user.dir"),
                "resources",
                "output",
                "wikipedia",
                timeStamp + "findOptimal.log");
      }
    }
    // check command line for override default methods
    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        indexDirectory = Paths.get(args[i + 1]);
        i++;
      } else if ("-results".equals(args[i])) {
        results = Paths.get(args[i + 1]);
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = Paths.get(args[i + 1]);
        i++;
      } else if ("-documents".equals(args[i])) {
        documents = Paths.get(args[i + 1]);
        i++;
      } else if ("-logFile".equals(args[i])) {
        logFile = Paths.get(args[i + 1]);
        i++;
      }
    }
    // setup the logger
    ProjectLogger.setLevel(Level.INFO);
    ProjectLogger.setLogFile(logFile);
    // set the config file
    final ConvertConfig config = new ConvertConfig();
    // lay out what features to use
    final ArrayList<String> features = new ArrayList<String>();
    config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);

    // this are all backwards compatible
    BufferedWriter outputWriter = null;
    try {
      // write out the queries
      final File outputText = output.toFile();
      outputText.createNewFile();
      final FileOutputStream outputIs = new FileOutputStream(outputText);
      final OutputStreamWriter osw = new OutputStreamWriter(outputIs);
      outputWriter = new BufferedWriter(osw);
      // find the optimal
      FindOptimal fo;
      fo = new FindOptimal(documents, indexDirectory, outputWriter, queries, results, false);
      fo.optimize(config, features);
      outputWriter.close();
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
}
