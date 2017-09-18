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
package naiveMathIndexer.index;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.xml.sax.SAXException;
import naiveMathIndexer.query.ParseQueries;
import query.MathQuery;
import results.Results;
import utilities.ProjectLogger;

/*
 * This class is used to be the optimal configuration of ConvertConfig
 * There is a main program that can be executed to find the optimal
 * 
 * @author Dallas Fraser
 * @see ConvertConfig
 * @since 2017-09-06
 */
public class FindOptimal {
    private Path documents;
    private Path index;
    private BufferedWriter output;
    private ArrayList<MathQuery> mathQueries;
    private Results answers;
    private static String FIELD = "contents";
    private static Float RELEVANT_LOWER = new Float(0.0);
    private static int TOP_K = 10000;
    private QueryBuilder builder;
    private Path queries;
    private Logger logger;
    /*
     * Class Constructor
     * @param documents path to the directory of documents
     * @param index path to the directory which will hold all the created indexes
     * @param output file where findings of the program will be printed to
     * @param queries path to where the queries
     * @param results path to the ground truth of the queries
     * @exception IOException
     * @exception InterruptedException
     * @exception XPathExpressionException
     * @exception ParserConfigurationException
     * @exception SAXException
     */
    public FindOptimal(Path documents,
                       Path index,
                       BufferedWriter output,
                       Path queries,
                       Path results) throws IOException,
                                            InterruptedException,
                                            XPathExpressionException,
                                            ParserConfigurationException,
                                            SAXException{
        this(documents, index, output, queries, results, ProjectLogger.getLogger());
    }

    /*
     * Class Constructor with both logger and recall setting
     * @param documents path to the directory of documents
     * @param index path to the directory which will hold all the created indexes
     * @param output file where findings of the program will be printed to
     * @param queries path to where the queries
     * @param results path to the ground truth of the queries
     * @param logger the specified logger to use
     * @param recall True if should use recall for scoring
     * @exception IOException
     * @exception InterruptedException
     * @exception XPathExpressionException
     * @exception ParserConfigurationException
     * @exception SAXException
     */
    public FindOptimal(Path documents,
                       Path index,
                       BufferedWriter output,
                       Path queries,
                       Path results,
                       Logger logger) throws IOException,
                                              InterruptedException,
                                              XPathExpressionException,
                                              ParserConfigurationException,
                                              SAXException{
        this.documents = documents;
        this.index = index;
        this.output = output;
        this.answers = new Results(results.toFile());
        this.logger = logger;
        Analyzer analyzer = new MathAnalyzer();
        this.builder = new QueryBuilder(analyzer);
        this.queries = queries;
    }
    /*
     * Updates the queries based uopon configuration
     * @param config Tangent features to be used when converting the queries
     * @exception IOException
     * @exception InterruptedException
     * @exception XPathExpressionException
     * @exception ParserConfigurationException
     * @exception SAXException
     */
    public void updateQueries(ConvertConfig config) throws XPathExpressionException,
                                                           ParserConfigurationException,
                                                           SAXException,
                                                           IOException,
                                                           InterruptedException{
        ParseQueries queryLoader = new ParseQueries(this.queries.toFile(), config);
        this.mathQueries = queryLoader.getQueries();
        queryLoader.deleteFile();
    }
    /*
     * finds the optimal features to be used *recursive) and outputs the results to a file
     * @param config Tangent features to be used when converting the queries
     * @param features list of features to look at
     * @exception IOException
     * @exception InterruptedException
     * @exception XPathExpressionException
     * @exception ParserConfigurationException
     * @exception SAXException
     */
    public void optimize(ConvertConfig config, ArrayList<String>features) throws IOException,
                                                                                          XPathExpressionException,
                                                                                          InterruptedException,
                                                                                          ParserConfigurationException,
                                                                                          SAXException{
        Path indexPath;
        this.updateQueries(config);
        double[] baseLine = this.scoreIndex(this.createIndex(config));
        double[] results;
        this.output.write("---------------------------------------------");
        this.output.newLine();
        this.output.write("Baseline");
        this.output.newLine();
        this.output.write(config + "," + baseLine[0] + "," + baseLine[1]);
        this.output.newLine();
        this.output.write("---------------------------------------------");
        this.output.newLine();
        double[] bestFeatureScore = {0.0, 0.0};
        String bestFeature = "";
        ArrayList<String> keepFeatures = new ArrayList<String>();
        for(String feature : features){
            config.flipBit(feature);
            this.updateQueries(config);
            this.output.newLine();
            indexPath = this.createIndex(config);
            results = this.scoreIndex(indexPath);
            this.output.write(config + "," + results[0] + "," + results[1]);
            this.output.newLine();
            config.flipBit(feature);
            if (results[0] > baseLine[0] || results[1] > baseLine[1]){
                keepFeatures.add(feature);
                if (results[0] > bestFeatureScore[0]){
                    bestFeatureScore[0] = results[0];
                    bestFeature = feature;
                }
                if (results[1] > bestFeatureScore[1]){
                    bestFeatureScore[1] = results[1];
                    bestFeature = feature;
                }
            }
        }
        if (!bestFeature.equals("")){
            // then information to gain by a feature
            ConvertConfig new_config;
            for (String feature : keepFeatures){
                // setup the new conversion configuration
                new_config = config.copy();
                new_config.flipBit(feature);
                // remove the feature from the list to try
                features.remove(feature);
                this.optimize(new_config, features);
                // add it back it
                features.add(feature);
            }
            
        }else{
            // then no information gain from any of the features
            this.output.write("Best Features: " +
                              config.toString() +
                              " Score:" +
                              bestFeatureScore[0] + "," +
                              bestFeatureScore[1]);
            this.output.newLine();
        }
        return;
    }
    /*
     * Parses the title of the file
     * @param title title to parse
     * @return the parsed title name
     */
    public String parseTitle(String title){
        String[] parts = title.split("/");
        String filename = parts[parts.length -1];
        String[] temp = filename.split("\\.");
        String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
        return String.join(".", nameparts);
    }
    /*
     * Returns the reciprocal rank of the results
     * @param searcher searcher that produced the results
     * @param searchDocs the results returned by the searcher
     * @param query the query to check with
     * @exception IOException
     * @return a the reciprocal rank (<1.0)
     */
    public double reciprocal_rank(IndexSearcher searcher,
                                 TopDocs searchDocs,
                                 MathQuery query) throws IOException{
        ScoreDoc[] hits = searchDocs.scoreDocs;
        double rank = (double) 0.0;
        double reciprocal = (double) 0.0;
        int count = 1;
        for (ScoreDoc hit : hits){
            Document doc = searcher.doc(hit.doc);
            rank = this.answers.findResult(query, this.parseTitle(doc.get("path")));
            if (rank > FindOptimal.RELEVANT_LOWER){
                this.logger.log(Level.FINER, "Count:" + count + " Rank:" + rank + "Doc:" + doc);
                reciprocal = (double) 1 / (double) count;
                break;
            }
            count += 1;
        }
        this.logger.log(Level.FINEST, query  + " Reciprocal: " + reciprocal);
        return reciprocal;
    }
    /*
     * Returns 1 if the result is found
     * @param searcher searcher that produced the results
     * @param searchDocs the results returned by the searcher
     * @param query the query to check with
     * @exception IOException
     * @return 1 if answer is found
     */
    public double found_answer(IndexSearcher searcher, TopDocs searchDocs, MathQuery query) throws IOException{
        ScoreDoc[] hits = searchDocs.scoreDocs;
        ArrayList<String> filenames = new ArrayList<String>();
        for (ScoreDoc hit : hits){
            // build the list of filenames to check against the answers
            Document doc = searcher.doc(hit.doc);
            filenames.add(this.parseTitle(doc.get("path")));
        }
        int[] results = this.answers.recallResult(query, filenames);
        double found;
        if(results[3] > 0){
            found = (double) 1.0;
        }else{
            found = (double) 0.0;
        }
        this.logger.log(Level.FINEST, "Answer found: " + found);
        return found;
    }
    /*
     * Scores the index
     * @param index the path to the created index
     * @exception IOException
     * @exception InterruptedException
     * @exception XPathExpressionException
     * @exception ParserConfigurationException
     * @exception SAXException
     * @return score of the index
     */
    public double[] scoreIndex(Path index) throws IOException,
                                               InterruptedException,
                                               XPathExpressionException,
                                               ParserConfigurationException,
                                               SAXException{

        double found_mean = (double) 0.0;
        double reciprocal_mean = (double) 0.0;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(index));
        IndexSearcher searcher = new IndexSearcher(reader);
        int count = 0;
        double reciprocal;
        double found;
        for (MathQuery mq: this.mathQueries){
            Query realQuery = this.builder.createBooleanQuery(FindOptimal.FIELD, mq.getQuery());
            if (realQuery == null){
                this.logger.log(Level.WARNING, "Query has no elements  " + mq);
                this.logger.log(Level.FINER, mq.getQueryName() + " found:" + 0 + " reciprocal:" + 0);
                this.output.write(mq.getQueryName() + "," + 0 + "," + 0);
                this.output.newLine();
            }else{
                BooleanQuery.Builder bq = new BooleanQuery.Builder();
                Query buildQuery = mq.buildQuery(realQuery.toString().split("contents:"),
                                                 FindOptimal.FIELD,
                                                 bq);
                TopDocs searchResults = searcher.search(buildQuery, FindOptimal.TOP_K);
                found = this.found_answer(searcher, searchResults, mq);
                reciprocal = this.reciprocal_rank(searcher, searchResults, mq); 
                this.logger.log(Level.FINER,
                                mq.getQueryName() +
                                " found:" +
                                found +
                                " reciprocal:" +
                                reciprocal +
                                "Total Results:" +
                                searchResults.totalHits);
                this.output.write(mq.getQueryName() + "," + found + "," + reciprocal);
                this.output.newLine();
                found_mean += found;
                reciprocal_mean += reciprocal;
            }
            count += 1;
        }
        this.logger.log(Level.INFO,
                        "Scores: "+ (found_mean / (double) count) + "," + (reciprocal_mean / (double) count));
        double[] results = {found_mean / (double) count, reciprocal_mean / (double) count};
        return results;
    }
    /*
     * Returns a path to the index that is created using the config features
     * @param config the features to be used in the index
     * @exception IOException
     * @return the path to the created index
     */
    public Path createIndex(ConvertConfig config) throws IOException{
        String name = config.toString().replace(" ", "");
        this.logger.log(Level.INFO, "Creating index: " + "The name of config: " + name);
        Path directoryPath = Paths.get(this.index.toString(), name);
        File directory = directoryPath.toFile();
        // already created
        this.logger.log(Level.INFO, "" + directory +  " exists: " +directory.exists());
        if (directory.exists()){
            return directoryPath;
        }
        if(!directory.mkdir()){
            throw new IOException("Unable to create directory");
        }
        // now create directory just like normal
        IndexFiles idf = new IndexFiles();
        this.logger.log(Level.INFO, "Creating Index: " + name);
        idf.indexDirectory(directoryPath.toString(), this.documents.toString(), true, config);
        this.logger.log(Level.INFO, "Directory: " + directoryPath);
        return directoryPath;
    }
    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava nativeMathIndexer.index.FindOptimal [-indexDirectory dir] [-queries file] [-results file] [-documents documents] [logFile file";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        // default arguments
        Path documents = Paths.get(System.getProperty("user.dir"), "resources", "documents", "wikipedia");
        Path indexDirectory = Paths.get(System.getProperty("user.dir"), "resources", "index", "wikipedia", "findOptimal");
        Path output = Paths.get(System.getProperty("user.dir"), "resources", "output", "wikipedia", "output.txt");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR11-Math-Wikipedia.xml");
        Path results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR11-wikipedia-11.txt");
        Path logFile = Paths.get(System.getProperty("user.dir"), "resources", "output", "wikipedia", "findOptimal.log");
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
        features.add(ConvertConfig.COMPOUND);
        features.add(ConvertConfig.EDGE);
        features.add(ConvertConfig.TERMINAL);
        features.add(ConvertConfig.UNBOUNDED);
        features.add(ConvertConfig.SHORTENED);
        // features.add(ConvertConfig.EOL);
        // features.add(ConvertConfig.LOCATION);
        
        BufferedWriter outputWriter = null;
        try {
            // write out the queries
            File outputText = output.toFile();
            outputText.createNewFile();
            FileOutputStream outputIS = new FileOutputStream(outputText);
            OutputStreamWriter osw = new OutputStreamWriter(outputIS);
            outputWriter = new BufferedWriter(osw);
            // find the optimal
            FindOptimal fo = new FindOptimal(documents,
                                             indexDirectory,
                                             outputWriter,
                                             queries,
                                             results);
            fo.optimize(config, features);
            outputWriter.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
            if(outputWriter != null){
                outputWriter.close();
            }
        }
    }
}
