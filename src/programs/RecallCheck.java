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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.xml.sax.SAXException;
import query.MathQuery;
import query.ParseQueries;
import search.Judgments;
import search.Search;
import search.Search.SearchConfigException;
import utilities.ProjectLogger;

/**
 * A program to check the recall of an index for the given queries and results.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class RecallCheck {
  private final Logger logger;

  /**
   * Class Constructor.
   *
   * @param index the path to the index
   * @param queries the path to the queries
   * @param results the path to the expected results
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public RecallCheck(Path index, Path queries, Path results)
      throws IOException, XPathExpressionException, ParserConfigurationException, SAXException,
          InterruptedException, ParseException, SearchConfigException, ConvertConfigException {
    this(index, queries, results, new ConvertConfig(), ProjectLogger.getLogger());
  }

  /**
   * Class Constructor.
   *
   * @param index the path to the index
   * @param queries the path to the queries
   * @param results the path to the expect results
   * @param config - the config of features
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public RecallCheck(Path index, Path queries, Path results, ConvertConfig config)
      throws IOException, XPathExpressionException, ParserConfigurationException, SAXException,
          InterruptedException, ParseException, SearchConfigException, ConvertConfigException {
    this(index, queries, results, config, ProjectLogger.getLogger());
  }

  /**
   * Class Constructor.
   *
   * @param index the path to the index
   * @param queries the path to the queries
   * @param results the path to the results
   * @param config the config to use
   * @param logger the logger to use
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public RecallCheck(Path index, Path queries, Path results, ConvertConfig config, Logger logger)
      throws IOException, XPathExpressionException, ParserConfigurationException, SAXException,
          InterruptedException, ParseException, SearchConfigException, ConvertConfigException {
    final Search searcher = new Search(index, logger, config);
    this.logger = logger;
    boolean increasing = true;
    float previous = (float) 0.0;
    final int stepSize = 100;
    int size = 100;
    final int maxSize = 2000;
    ArrayList<String> files;
    final ParseQueries queryLoader = new ParseQueries(queries.toFile(), config);
    final ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
    queryLoader.deleteFile();
    int relevantDocs;
    int partialRelevantDocs;
    int relevantFound;
    int partialRelevantFound;
    int[] recalls;
    final Judgments answers = new Judgments(results.toFile());
    while (increasing && size <= maxSize) {
      relevantDocs = 0;
      partialRelevantDocs = 0;
      relevantFound = 0;
      partialRelevantFound = 0;
      for (final MathQuery mq : mathQueries) {
        files = searcher.searchQueryFiles(mq, size);
        recalls = answers.recallResult(mq, files);
        relevantDocs += recalls[0];
        relevantFound += recalls[1];
        partialRelevantDocs += recalls[2];
        partialRelevantFound += recalls[3];
      }
      this.logger.log(
          Level.INFO,
          ((float) relevantFound / (float) relevantDocs)
              + " "
              + ((float) partialRelevantFound / (float) partialRelevantDocs));
      size += stepSize;
      increasing = false;
      if (((float) partialRelevantFound / (float) partialRelevantDocs) > previous) {
        increasing = true;
        previous = ((float) partialRelevantFound / (float) partialRelevantDocs);
      }
    }
  }

  /**
   * Main entry into the program.
   *
   * @param args - see usage
   */
  public static void main(String[] args) {
    final String usage =
        "Usage:\tjava naiveMathIndexer..RecallCheck [-index dir]"
            + " [-queries file] [-results file] [-log logFile]";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }
    final ConvertConfig config = new ConvertConfig();
    // default values
    Path index =
        Paths.get(System.getProperty("user.dir"), "resources", "index", "arXiv", "current");
    Path queries =
        Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-ArXiv.xml");
    Path results =
        Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR12-ArXiv-Math.dat");
    Path logFile =
        Paths.get(
            System.getProperty("user.dir"), "resources", "output", "arXiv", "recallChceck.log");
    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        index = Paths.get(args[i + 1]);
        i++;
      } else if ("-results".equals(args[i])) {
        results = Paths.get(args[i + 1]);
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = Paths.get(args[i + 1]);
        i++;
      } else if ("-log".equals(args[i])) {
        logFile = Paths.get(args[i + 1]);
        i++;
      }
    }
    try {
      // setup the logger
      ProjectLogger.setLevel(Level.INFO);
      ProjectLogger.setLogFile(logFile);
      // load the config from the index
      config.loadConfig(index);
      // write out the queries
      // do the actual searching
      new RecallCheck(index, queries, results, config);
      // close the files
    } catch (final IOException e) {
      System.err.println("Problem writing to the file statsTest.txt");
      e.printStackTrace();
    } catch (final XPathExpressionException e) {
      e.printStackTrace();
    } catch (final ParserConfigurationException e) {
      e.printStackTrace();
    } catch (final SAXException e) {
      e.printStackTrace();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    } catch (final ParseException e) {
      e.printStackTrace();
    } catch (final SearchConfigException e) {
      System.err.println("Config files did not match");
      e.printStackTrace();
    } catch (final ConvertConfigException e) {
      System.err.println("Config file not found");
      e.printStackTrace();
    }
  }
}
