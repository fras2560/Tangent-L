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
import index.ConvertConfig;
import query.ParseQueries;
import search.Judgements;
import search.Search;
import query.MathQuery;
import utilities.ProjectLogger;


public class RecallCheck {
    private Logger logger;

    public RecallCheck(Path index, Path queries, Path results) throws IOException,
                                                                                            XPathExpressionException,
                                                                                            ParserConfigurationException,
                                                                                            SAXException,
                                                                                            InterruptedException,
                                                                                            ParseException{
        this(index, queries, results, new ConvertConfig(), ProjectLogger.getLogger());
    }

    public RecallCheck(Path index, Path queries, Path results, ConvertConfig config) throws IOException,
                                                                      XPathExpressionException,
                                                                      ParserConfigurationException,
                                                                      SAXException,
                                                                      InterruptedException,
                                                                      ParseException{
        this(index, queries, results, config, ProjectLogger.getLogger());
    }

    public RecallCheck(Path index, Path queries, Path results, ConvertConfig config, Logger logger) throws IOException,
                                                                                     XPathExpressionException,
                                                                                     ParserConfigurationException,
                                                                                     SAXException,
                                                                                     InterruptedException,
                                                                                     ParseException{
        Search searcher = new Search(index, logger, config);
        this.logger = logger;
        boolean increasing = true;
        float previous = (float) 0.0;
        int stepSize = 100;
        int size = 100;
        int maxSize = 2000;
        ArrayList<String> files;
        ParseQueries queryLoader = new ParseQueries(queries.toFile(), config);
        ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
        queryLoader.deleteFile();
        int r_docs, pr_docs, r_found, pr_found;
        int[] recalls;
        Judgements answers = new Judgements(results.toFile());
        while (increasing && size <= maxSize){
            r_docs = 0;
            pr_docs = 0;
            r_found = 0;
            pr_found = 0;
            for (MathQuery mq: mathQueries){
                files = searcher.searchQueryFiles(mq, size);
                recalls = answers.recallResult(mq, files);
                r_docs += recalls[0];
                r_found += recalls[1];
                pr_docs += recalls[2];
                pr_found += recalls[3];
            }
            this.logger.log(Level.INFO, ((float) r_found / (float) r_docs) +
                            " " +
                            ((float) pr_found / (float)pr_docs));
            size += stepSize;
            increasing = false;
            if (((float) pr_found / (float) pr_docs) > previous){
                increasing=  true;
                previous = ((float) pr_found / (float) pr_docs);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava naiveMathIndexer..RecallCheck [-index dir] [-queries file] [-results file] [-log logFile]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        ConvertConfig config = new ConvertConfig();
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, false);
        // default values
        Path index = Paths.get(System.getProperty("user.dir"), "resources", "index", "arXiv", "current");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-ArXiv.xml");
        Path results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR12-ArXiv-Math.dat");
        Path logFile = Paths.get(System.getProperty("user.dir"), "resources", "output", "arXiv", "recallChceck.log");
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
          }else if ("-log".equals(args[i])){
            logFile = Paths.get(args[i+1]);
            i++;
          }
        }
        // setup the logger
        ProjectLogger.setLevel(Level.INFO);
        ProjectLogger.setLogFile(logFile);
        try {
            // write out the queries
            // do the actual searching
            new RecallCheck(index, queries, results, config);
            // close the files
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
        }
    }
}
