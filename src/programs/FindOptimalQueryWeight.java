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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import search.Search;
import utilities.ProjectLogger;

/**
 * Program to find the optimal weight between text and math for each query.
 *
 * @author Dallas
 */
public class FindOptimalQueryWeight {
  /**
   * The main entry to the program.
   *
   * @param args - see usage
   * @throws Exception - an issue when running the program
   */
  public static void main(String[] args) throws Exception {
    final String usage =
        "Usage:\tjava programs.FindOptimalQueryWeight [-index dir]"
            + "[-queries file] [-judgements file] [-logFile file]";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }
    Path index =
        Paths.get(System.getProperty("user.dir"), "resources", "index", "full_arXiv", "tunaIndex");
    Path queries =
        Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-ArXiv.xml");
    Path results =
        Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR12-ArXiv-Math.dat");
    final String date = new SimpleDateFormat("dd-MM-yyyy:HH:mm").format(new Date());
    final Path output =
        Paths.get(
            System.getProperty("user.dir"),
            "resources",
            "output",
            "arXiv",
            "weights",
            date + ".csv");
    Path logFile =
        Paths.get(System.getProperty("user.dir"), "resources", "output", "arXiv", date + ".log");
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
      } else if ("-logFile".equals(args[i])) {
        logFile = Paths.get(args[i + 1]);
      }
    }
    logFile.toFile().createNewFile();
    ProjectLogger.setLevel(Level.INFO);
    ProjectLogger.setLogFile(logFile);
    BufferedWriter outputWriter = null;
    try {
      //  write out the queries
      final File outputText = output.toFile();
      outputText.createNewFile();
      final FileOutputStream outputIs = new FileOutputStream(outputText);
      final OutputStreamWriter osw = new OutputStreamWriter(outputIs);
      outputWriter = new BufferedWriter(osw);
      // setup the config file by loading what it is in the index
      final ConvertConfig config = new ConvertConfig();
      config.loadConfig(index);
      // config.setQueryType(ConvertConfig.BM25TP_QUERY);
      config.setQueryType(ConvertConfig.DIFFERENT_WEIGHTED_QUERY);
      // do the actual searching
      final Search searcher = new Search(index, config);
      searcher.optimizePerQuery(queries, results);
      outputWriter.close();
    } catch (final IOException e) {
      System.err.println("Problem writing to the file statsTest.txt");
      e.printStackTrace();
      if (outputWriter != null) {
        outputWriter.close();
      }
    }
  }
}
