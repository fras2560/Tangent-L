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
package naiveMathIndexer.query;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.xml.sax.SAXException;
import naiveMathIndexer.index.MathAnalyzer;
import query.MathQuery;
import results.Results;
import utilities.ProjectLogger;
import naiveMathIndexer.query.Search;


public class RecallCheck  extends Search{
    private Logger logger;
    public RecallCheck(Path index, Path queries, Path results) throws IOException,
                                                                      XPathExpressionException,
                                                                      ParserConfigurationException,
                                                                      SAXException,
                                                                      InterruptedException,
                                                                      ParseException{
        this(index, queries, results, ProjectLogger.getLogger());
    }

    public RecallCheck(Path index, Path queries, Path results, Logger logger) throws IOException,
                                                                                     XPathExpressionException,
                                                                                     ParserConfigurationException,
                                                                                     SAXException,
                                                                                     InterruptedException,
                                                                                     ParseException{
        this.logger = logger;
        String field = "contents";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(index));
        IndexSearcher searcher = new IndexSearcher(reader);
        Similarity similarity = new ClassicSimilarity();
        searcher.setSimilarity(similarity);
        Analyzer analyzer = new MathAnalyzer();
        ParseQueries queryLoader = new ParseQueries(queries.toFile());
        ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
        QueryBuilder builder = new QueryBuilder(analyzer);
        Results answers = new Results(results.toFile());
        boolean increasing = true;
        float previous = (float) 0.0;
        int stepSize = 100;
        int size = 100;
        int maxSize = 2000;
        while (increasing && size <= maxSize){
            int r_docs = 0;
            int pr_docs = 0;
            int r_found = 0;
            int pr_found = 0;
            for (MathQuery mq: mathQueries){
                Query realQuery = builder.createBooleanQuery(field, mq.getQuery());
                if (realQuery == null){
                    this.logger.log(Level.WARNING, "Query has no elements: " + mq);
                }else{
                    BooleanQuery.Builder bq = new BooleanQuery.Builder();
                    Query buildQuery = mq.buildQuery(realQuery.toString().split("contents:"), field, bq);
                    TopDocs searchResultsWild = searcher.search(buildQuery, size);
                    ArrayList<String> files = new ArrayList<String>();
                    ScoreDoc[] hits = searchResultsWild.scoreDocs;
                    for (ScoreDoc hit: hits){
                        Document doc = searcher.doc(hit.doc);
                        files.add(this.parseTitle(doc.get("path")));
                        this.logger.log(Level.FINER, "Query name:" +
                                                            mq.getQueryName() +
                                                            " " +
                                                            searcher.explain(buildQuery, hit.doc));
                    }
                    int[] recalls = answers.recallResult(mq, files);
                    r_docs += recalls[0];
                    r_found += recalls[1];
                    pr_docs += recalls[2];
                    pr_found += recalls[3];
                }
            }
            this.logger.log(Level.FINE, ((float) r_found / (float) r_docs) +
                                                " " +
                                               ((float) pr_found / (float)pr_docs));
            size += stepSize;
            increasing = false;
            if (((float) pr_found / (float) pr_docs) > previous){
                increasing=  true;
                previous = ((float) pr_found / (float) pr_docs);
            }
        }
        queryLoader.deleteFile();
    }

    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-queries file] [-results file]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        Path index = Paths.get(System.getProperty("user.dir"), "resources", "index", "all-pairs", "eol-ws-1-no-path");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "simple-queries.xml");
        Path results = Paths.get(System.getProperty("user.dir"), "resources", "results", "simple-results.dat");
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
          }
        }
        try {
            // write out the queries
            // do the actual searching
            new RecallCheck(index, queries, results);
            // close the files
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
        }
    }
}
