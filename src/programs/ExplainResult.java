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
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import search.Search;
import search.Search.SearchConfigException;
import utilities.ProjectLogger;

/**
 * A program to explain the results of a running queries on a index with the expect results.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class ExplainResult {
  /**
   * Given an index, query and results, explain the scoring for each result.
   *
   * @param args - see usage
   * @throws IOException - exception when parsing file
   */
  public static void main(String[] args) throws IOException {
    final String usage =
        "Usage:\tjava program.ExplainResult [-indexDirectory dir] [-queries file]"
            + " [-results file] [-logFile file]";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }
    final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new java.util.Date());
    // default arguments
    Path indexDirectory;
    indexDirectory =
        Paths.get(
            System.getProperty("user.dir"), "resources", "index", "wikipedia_formula", "current");
    final Path output =
        Paths.get(
            System.getProperty("user.dir"),
            "resources",
            "output",
            "wikipedia_formula",
            timeStamp + "explain.txt");
    Path queries =
        Paths.get(
            System.getProperty("user.dir"),
            "resources",
            "query",
            "NTCIR11-Math-Wikipedia-Sample.xml");
    Path logFile =
        Paths.get(
            System.getProperty("user.dir"),
            "resources",
            "output",
            "wikipedia_formula",
            timeStamp + "explainResults.log");
    // check command line for override default methods
    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        indexDirectory = Paths.get(args[i + 1]);
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = Paths.get(args[i + 1]);
        i++;
      } else if ("-logFile".equals(args[i])) {
        logFile = Paths.get(args[i + 1]);
        i++;
      }
    }
    // setup the logger
    ProjectLogger.setLevel(Level.FINER);
    ProjectLogger.setLogFile(logFile);
    // set the config file
    final ConvertConfig config = new ConvertConfig();
    // lay out what features to user
    // this are all backwards compatible
    try {
      // load the config file
      config.loadConfig(indexDirectory);
      // explain the results
      final Search search = new Search(indexDirectory, config);
      // write out the queries
      search.explainQueries(queries, output, 100);
      // find the score
    } catch (IOException
        | XPathExpressionException
        | InterruptedException
        | ParserConfigurationException
        | SAXException
        | ConvertConfigException e) {
      System.err.println("Problem writing to the file statsTest.txt");
      e.printStackTrace();
    } catch (final SearchConfigException e) {
      System.err.println("Config files did not match");
      e.printStackTrace();
    }
  }
}
