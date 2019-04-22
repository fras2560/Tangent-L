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
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import search.Search;
import utilities.ProjectLogger;

/**
 * A program to find the optimal weight between math and text for the whole set of queries.
 *
 * @author Dallas
 */
public class FindOptimalWeight {
  /**
   * The main entry into the program.
   *
   * @param args - see usage
   * @throws Exception - exception when running program, probably incorrect parameters or missing
   *     file
   */
  public static void main(String[] args) throws Exception {
    final String usage =
        "Usage:\tjava programs.FindOptimalWeight [-index dir]"
            + "[-queries file] [-judgments file] [-logFile file]";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }
    Path index =
        Paths.get(
            System.getProperty("user.dir"), "resources", "index", "ntcir-12-wikipedia", "current");
    Path queries =
        Paths.get(
            System.getProperty("user.dir"), "resources", "query", "NTCIR12-MathWiki-main.xml");
    Path results =
        Paths.get(
            System.getProperty("user.dir"), "resources", "results", "NTCIR12-MathWiki-main.dat");
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
      float bestMapr = 0f;
      float bestMappr = 0f;
      float bestMathWeight = 0f;
      float bestMathWeightPr = 0f;
      float tempMapR;
      float tempMapPr;
      float[] result;
      searcher.setBeta(1f);
      // config.setMathBM25(true);
      for (float mathWeight = 0.01f; mathWeight <= 3f; mathWeight = mathWeight + 0.01f) {
        searcher.setAlpha(mathWeight);
        result = searcher.runNtcirTest(queries, results);
        System.out.println(Arrays.toString(result));
        tempMapR = (result[0] / 5 + result[1] / 10 + result[2] / 15 + result[3] / 20) / 4;
        tempMapPr = (result[4] / 5 + result[5] / 10 + result[6] / 15 + result[7] / 20) / 4;
        outputWriter.write(mathWeight + "," + tempMapR + "," + tempMapPr);
        outputWriter.newLine();
        if (tempMapR > bestMapr) {
          bestMathWeightPr = mathWeight;
          bestMapr = tempMapR;
        }
        if (tempMapPr > bestMappr) {
          bestMathWeight = mathWeight;
          bestMappr = tempMapPr;
        }
      }
      System.out.println(
          "Best alpha for Relevant Results: " + bestMapr + " @ mathWeight = " + bestMathWeight);
      System.out.println(
          "Best alpha for Partially Relevant Results: "
              + bestMappr
              + " @ mathWeight = "
              + bestMathWeightPr);
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
