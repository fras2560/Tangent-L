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

import index.ConvertConfig;
import search.Search;
import utilities.ProjectLogger;

/**
 * A program to run the NTCIR test on an index given a set of queries and outputs the results to a few different files
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class NTCIR_TEST {
    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava programs.NTCIR_TEST [-index dir] [-queries file] [-judgements file] [-resultsOutput file] [-queriesOutput file] [-logFile file]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        Path index = Paths.get(System.getProperty("user.dir"), "resources", "index", "ntcir-12-wikipedia-formula", "current");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-MathWiki-formula.xml");
        Path results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR12-MathWiki-formula-dashes.dat");
        String date = new SimpleDateFormat("dd-MM-yyyy:HH:mm").format(new Date());
        Path logFile = Paths.get(System.getProperty("user.dir"),
                                 "resources",
                                 "output",
                                 "arXiv",
                                 date + ".log");
        Path queryOutput = Paths.get(System.getProperty("user.dir"),
                                     "resources",
                                     "output",
                                     "arXiv",
                                     date + "-queries.txt");
        Path resultOutput = Paths.get(System.getProperty("user.dir"),
                                      "resources",
                                      "output",
                                      "arXiv",
                                      date + "-results.txt");
        for(int i = 0;i < args.length;i++) {
          if ("-index".equals(args[i])) {
              index = Paths.get(args[i+1]);
              i++;
          } else if ("-results".equals(args[i])) {
              results = Paths.get(args[i+1]);
              i++;
          } else if ("-queries".equals(args[i])) {
              queries = Paths.get(args[i+1]);
              i++;
          } else if("-resultsOutput".equals(args[i])){
              resultOutput = Paths.get(args[i+1]);
              i++;
          }else if("-queriesOutput".equals(args[i])){
              queryOutput = Paths.get(args[i+1]);
              i++;
          }else if("-logFile".equals(args[i])){
              logFile = Paths.get(args[i+1]);
          }
        }
        logFile.toFile().createNewFile();
        ProjectLogger.setLevel(Level.INFO);
        ProjectLogger.setLogFile(logFile);
        BufferedWriter queryWriter = null;
        BufferedWriter resultsWriter = null;
        try {
            // write out the queries
            File queryText = queryOutput.toFile();
            queryText.createNewFile();
            FileOutputStream qis = new FileOutputStream(queryText);
            OutputStreamWriter qosw = new OutputStreamWriter(qis);
            queryWriter = new BufferedWriter(qosw);
            // write out the precisions results of each file
            File resultsText = resultOutput.toFile();
            resultsText.createNewFile();
            FileOutputStream ris = new FileOutputStream(resultsText);
            OutputStreamWriter rosw = new OutputStreamWriter(ris);  
            resultsWriter = new BufferedWriter(rosw);
            // setup the config file by loading what it is in the index
            ConvertConfig config = new ConvertConfig();
            config.loadConfig(index);
            config.setBooleanAttribute(ConvertConfig.TERMINAL, true);
            config.setBooleanAttribute(ConvertConfig.COMPOUND, true);
            config.setBooleanAttribute(ConvertConfig.EXPAND_LOCATION, true);
            config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
            // config.setBooleanAttribute(ConvertConfig.PROXIMITY, true);
            config.setBooleanAttribute(ConvertConfig.BAGS_OF_WORDS, true);
            // config.setQueryType(ConvertConfig.BM25_DISTANCE_QUERY);
            // do the actual searching
            Search searcher = new Search(index, config);
            // searcher.setAlpha(0.41f);
            searcher.ntcirTest(queries, results, resultsWriter);
            searcher.recordQueries(queries, queryWriter, 1000);;
            // close the files
            resultsWriter.close();
            queryWriter.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
            if(queryWriter != null){
                queryWriter.close();
            }
            if (resultsWriter != null){
                resultsWriter.close();
            }
        }
    }
}
