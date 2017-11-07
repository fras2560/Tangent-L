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
package search;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.BooleanQuery;
import org.xml.sax.SAXException;
import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
import query.ParseQueries;
import query.MathQuery;
import query.MathSimilarity;
import utilities.Functions;
import utilities.ProjectLogger;

/**
 * A class used to search the index
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class Search {
    private Logger logger ;
    private IndexSearcher searcher;
    private ConvertConfig config;
    private boolean synonym;
    private IndexReader reader;
    private static final int DEFAULT_K = 100;
    private static final int MAX_CLUASES = 4096;

    /**
     * Class Constructor
     * @param index path to the Lucene index
     * @throws IOException
     * @throws SearchConfigException
     * @throws ConvertConfigException
     */
    public Search(Path index) throws IOException, SearchConfigException, ConvertConfigException{
        this(index, ProjectLogger.getLogger(), new ConvertConfig(), MathSimilarity.getSimilarity());
    }

    /**
     * Class Constructor
     * @param index path to the Lucene index
     * @param similarity the similarity to use when searching
     * @throws IOException
     * @throws SearchConfigException
     * @throws ConvertConfigException
     */
    public Search(Path index, Similarity similarity)throws IOException, SearchConfigException, ConvertConfigException{
        this(index, ProjectLogger.getLogger(), new ConvertConfig(), similarity);
    }

    /**
     * Class Constructor
     * @param index path to the Lucene index
     * @param config the config to convert MathML and determine Query Type
     * @param similarity the similarity to use when searching
     * @throws IOException
     * @throws SearchConfigException
     * @throws ConvertConfigException
     */
    public Search(Path index, Similarity similarity, ConvertConfig config) throws IOException,
                                                                                  SearchConfigException,
                                                                                  ConvertConfigException{
        this(index, ProjectLogger.getLogger(), config, similarity);
    }

    /**
     * Class Constructor
     * @param index path to the Lucene index
     * @param logger the logger to use
     * @throws IOException
     * @throws SearchConfigException
     * @throws ConvertConfigException
     */
    public Search(Path index, Logger logger) throws IOException, SearchConfigException, ConvertConfigException{
        this(index, logger, new ConvertConfig(), MathSimilarity.getSimilarity());
    }

    /**
     * Class Constructor
     * @param index path to the Lucene index
     * @param config the config to convert MathML and determine Query Type
     * @throws IOException
     * @throws SearchConfigException
     * @throws ConvertConfigException
     */
    public Search(Path index, ConvertConfig config) throws IOException, SearchConfigException, ConvertConfigException{
        this(index, ProjectLogger.getLogger(), config, MathSimilarity.getSimilarity());
    }

    /**
     * Class Constructor
     * @param index path to the Lucene index
     * @param logger the logger to use
     * @param config the config to convert MathML and determine Query Type
     * @throws IOException
     * @throws SearchConfigException
     * @throws ConvertConfigException
     */
    public Search(Path index, Logger logger, ConvertConfig config)throws IOException,
                                                                          SearchConfigException,
                                                                          ConvertConfigException{
        this(index, logger, config, MathSimilarity.getSimilarity());
    }

    /**
     * Class Constructor
     * @param index path to the Lucene index
     * @param logger the logger to use
     * @param config the config to convert MathML and determine Query Type
     * @param similarity the similarity to use when searching
     * @throws IOException
     * @throws SearchConfigException
     * @throws ConvertConfigException
     */
    public Search(Path index,
                  Logger logger,
                  ConvertConfig config,
                  Similarity similarity) throws IOException, SearchConfigException, ConvertConfigException{
        // increase the clause count since formulas can be a slight bit longer
        BooleanQuery.setMaxClauseCount(Search.MAX_CLUASES);
        // remember if synonyms were used when indexing
        this.synonym = config.getAttribute(ConvertConfig.SYNONYMS);
        this.config = config.getSearchConfig();
        // make sure the config and index are compatible
        ConvertConfig indexConfig = new ConvertConfig();
        indexConfig.loadConfig(index);
        // index config needs to be compatible with the searching config (not necessarily reverse direction)
        if (!indexConfig.compatible(config)){
            logger.log(Level.WARNING, "Incompatible config files: " + config + " vs " + indexConfig);
            throw new SearchConfigException("Config did not match index");
        }
        this.reader = DirectoryReader.open(FSDirectory.open(index));
        this.searcher = new IndexSearcher(reader);
        this.searcher.setSimilarity(similarity);
        this.config = config;
        this.logger = logger;
    }

    /**
     * Closes the reader of the index
     * @throws IOException
     */
    public void close() throws IOException {
        this.reader.close();
    }

    /**
     * Returns whether Indexer used synonyms or not
     * @return boolean True if indexer used synonyms
     */
    public boolean getSynonym(){
        return this.synonym;
    }
    /**
     * Search using the query and return a list of the documents file paths
     * @param mathQuery the query to search
     * @return ArrayList the list of files returned by the query
     * @throws IOException
     */
    public ArrayList<String> searchQueryFiles(MathQuery mathQuery) throws IOException{
        return this.searchQueryFiles(mathQuery, Search.DEFAULT_K);
    }

    /**
     * Search using the query and return a list of the documents file paths
     * @param mathQuery the query to search
     * @param k the number of documents to return
     * @return ArrayList the list of files returned by the query
     * @throws IOException
     */
    public ArrayList<String>searchQueryFiles(MathQuery mathQuery, int k) throws IOException{
        this.logger.log(Level.FINER, "Query: " + mathQuery.getQuery().replaceAll("//", "//") +
                "Query: name: " + mathQuery.getQueryName());
        ArrayList<String> files = new ArrayList<String>();
        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        Query buildQuery = mathQuery.buildQuery(mathQuery.getFieldName(),
                                                bq,
                                                this.synonym,
                                                this.config,
                                                this.searcher.collectionStatistics(mathQuery.getFieldName()));
        this.logger.log(Level.FINEST, "BuildQuery:" + buildQuery);
        TopDocs searchResultsWild = this.searcher.search(buildQuery, k);
        ScoreDoc[] hits = searchResultsWild.scoreDocs;
        for (ScoreDoc hit: hits){
            Document doc = searcher.doc(hit.doc);
            files.add(Functions.parseTitle(doc.get("path")));
        }
        return files;
    }

    /**
     * Returns search results of a query
     * @param mathQuery the query to search
     * @return SearchResult the results of the search
     * @throws IOException
     */
    public SearchResult searchQuery(MathQuery mathQuery) throws IOException{
        return this.searchQuery(mathQuery, Search.DEFAULT_K);
    }

    /**
     * Returns search results of a query
     * @param mathQuery the query to search
     * @param k the number of documents to return
     * @return SearchResult the results of the search
     * @throws IOException
     */
    public SearchResult searchQuery(MathQuery mathQuery, int k) throws IOException{
        this.logger.log(Level.FINER,
                        "Query: " +
                        mathQuery.getQuery().replaceAll("//", "//") +
                        " Query: name: " +
                        mathQuery.getQueryName());
        SearchResult result = null;
        if (mathQuery.getTerms().size() <= 0){
            this.logger.log(Level.WARNING, "Query has no elements: " + mathQuery.getQueryName());
            result = new SearchResult(null, mathQuery, k, null);
        }else{
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            Query buildQuery = mathQuery.buildQuery(mathQuery.getFieldName(),
                                                    bq,
                                                    this.synonym,
                                                    this.config,
                                                    this.searcher.collectionStatistics(mathQuery.getFieldName()));
            this.logger.log(Level.FINEST, "Boolean Query Size:" + mathQuery.getTerms().size());
            this.logger.log(Level.FINEST, "BuildQuery:" + buildQuery);
            TopDocs searchResultsWild = this.searcher.search(buildQuery, k);
            result = new SearchResult(searchResultsWild, mathQuery, k, buildQuery);
        }
        return result;
    }

    /**
     * Returns a list of search results for queries found in a file
     * @param queries the path to the file of queries
     * @return ArrayList a list of search results
     * @throws XPathExpressionException
     * @throws IOException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public ArrayList<SearchResult> searchQueries(Path queries) throws XPathExpressionException,
                                                                      IOException,
                                                                      InterruptedException,
                                                                      ParserConfigurationException,
                                                                      SAXException{
        return this.searchQueries(queries, Search.DEFAULT_K);
    }

    /**
     * Returns a list of search results for queries found in a file
     * @param queries the path to the file of queries
     * @param k the number of documents to return for each query
     * @return ArrayList a list of search results
     * @throws XPathExpressionException
     * @throws IOException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public ArrayList<SearchResult> searchQueries(Path queries, int k) throws IOException,
                                                                             InterruptedException,
                                                                             XPathExpressionException,
                                                                             ParserConfigurationException,
                                                                             SAXException{
        // init the list of query results
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        // parse the query file
        ParseQueries queryLoader = new ParseQueries(queries.toFile(), this.config);
        ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
        queryLoader.deleteFile();
        for (MathQuery mq: mathQueries){
            results.add(this.searchQuery(mq, k));
        }
        return results;
    }

    /**
     * Explains the queries results and the scoring of each document
     * @param queries the path to the queries
     * @throws IOException
     * @throws XPathExpressionException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void explainQueries(Path queries) throws IOException,
                                                    XPathExpressionException,
                                                    InterruptedException,
                                                    ParserConfigurationException,
                                                    SAXException{
        this.explainQueries(queries, Search.DEFAULT_K);
    }

    /**
     * Explains the queries results and the scoring of each document
     * @param queries the path to the queries
     * @param k the number of documents for each query
     * @throws IOException
     * @throws XPathExpressionException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void explainQueries(Path queries, int k) throws IOException,
                                                    XPathExpressionException,
                                                    InterruptedException,
                                                    ParserConfigurationException,
                                                    SAXException{
        ArrayList<SearchResult> queryResults = this.searchQueries(queries, k);
        for (SearchResult queryResult : queryResults){
            // loop through each query and explain it
            System.out.println("Returned: " + queryResult.hitsNumber());
            queryResult.explainResults(searcher);
        }
    }

    /**
     * Explains the queries results and the scoring of each document and outputs to a file
     * @param queries the path to the queries
     * @param output the file to output to
     * @throws IOException
     * @throws XPathExpressionException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void explainQueries(Path queries, Path output)throws IOException,
                                                                XPathExpressionException,
                                                                InterruptedException,
                                                                ParserConfigurationException,
                                                                SAXException{
        this.explainQueries(queries, output, Search.DEFAULT_K);
    }

    /**
     * Explains the queries results and the scoring of each document and outputs to a file
     * @param queries the path to the queries
     * @param output the file to output to
     * @param k the number of documents for each query
     * @throws IOException
     * @throws XPathExpressionException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void explainQueries(Path queries, Path output, int k)throws IOException,
                                                                XPathExpressionException,
                                                                InterruptedException,
                                                                ParserConfigurationException,
                                                                SAXException{
        ArrayList<SearchResult> queryResults = this.searchQueries(queries, k);
        // create the file and a writer to it
        File outputText = output.toFile();
        outputText.createNewFile();
        FileOutputStream fos = new FileOutputStream(outputText);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        for (SearchResult queryResult : queryResults){
            // loop through each query and explain it
            queryResult.explainResults(searcher, bw);
        }
        bw.close();
        fos.close();
    }

    /**
     * Records the results of the queries
     * @param queries path to the queries
     * @param queryWriter the file to output the query results to
     * @throws IOException
     * @throws XPathExpressionException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void recordQueries(Path queries, BufferedWriter queryWriter) throws IOException,
                                                                               XPathExpressionException,
                                                                               InterruptedException,
                                                                               ParserConfigurationException,
                                                                               SAXException{
        this.recordQueries(queries, queryWriter, Search.DEFAULT_K);
    }

    /**
     * Record the results of the queries
     * @param queries path to the queries
     * @param queryWriter the file to output the query results to
     * @param k the number of documents to return for each query
     * @throws IOException
     * @throws XPathExpressionException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void recordQueries(Path queries, BufferedWriter queryWriter, int k) throws IOException,
                                                                                      XPathExpressionException,
                                                                                      InterruptedException,
                                                                                      ParserConfigurationException,
                                                                                      SAXException{
        ArrayList<SearchResult> queryResults = this.searchQueries(queries, k);
        MathQuery query;
        Document doc;
        ScoreDoc[] hits;
        int index;
        for (SearchResult queryResult : queryResults){
            // loop through each query
            query = queryResult.getMathQuery();
            hits = queryResult.getResults().scoreDocs;
            index = 0;
            for (ScoreDoc hit : hits){
                // loop through every result of the 
                doc = this.searcher.doc(hit.doc);
                queryWriter.write(query.getQueryName() + " 1 " + doc.get("path") + " " + (index+1) + " " + hit.score + " RITUW");
                queryWriter.newLine();
                index += 1;
            }
        }
    }

    /**
     * runs the ntcir test scoring precision for P5, P10, P15, P20 on relevant and partially relevant documents
     * @param queries path to the queries to run
     * @param results the expected results used to check relevance
     * @param resultsWriter the file to output to
     * @throws IOException
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws InterruptedException
     * @throws ParseException
     */
    public void ntcirTest(Path queries, Path results, BufferedWriter resultsWriter) throws IOException,
                                                                                           XPathExpressionException,
                                                                                           ParserConfigurationException,
                                                                                           SAXException,
                                                                                           InterruptedException,
                                                                                           ParseException{
        ArrayList<SearchResult> queryResults = this.searchQueries(queries, 20);
        Judgements answers = new Judgements(results.toFile());
        ArrayList<ArrayList<Double>> scores;
        for (SearchResult queryResult: queryResults){
            if (queryResult.getMathQuery() == null){
                resultsWriter.write(queryResult.getMathQuery().getQueryName() + ",0,0,0,0,0,0,0,0");
                resultsWriter.newLine();
            }else{
                scores = this.arxivScore(queryResult.getResults(), queryResult.getMathQuery(), answers);
                resultsWriter.write(queryResult.getMathQuery().getQueryName() + "," + 
                                    scores.get(0).get(0) + "," +
                                    scores.get(0).get(1) +  "," +
                                    scores.get(0).get(2) +  "," +
                                    scores.get(0).get(3) +  "," +
                                    scores.get(1).get(0) +  "," +
                                    scores.get(1).get(1) +  "," +
                                    scores.get(1).get(2) +  "," +
                                    scores.get(1).get(3)
                                   );
                resultsWriter.newLine();
            }
        }
    }

    /**
     * Checks if the two results are the same
     * @param tp1 first set of results
     * @param tp2 second set of results
     */
    public void differentResults(TopDocs tp1, TopDocs tp2){
        ScoreDoc[] sd1 = tp1.scoreDocs;
        ScoreDoc[] sd2 = tp2.scoreDocs;
        boolean same = true;
        if (sd1.length == sd2.length){
            for (int i = 0; i < sd1.length;i++){
                if (sd1[i].doc != sd2[i].doc){
                    same = false;
                }
            }
        }else{
            same = false;
        }
        this.logger.log(Level.FINEST, "Both queries are the same:" + same);
    }

    /**
     * Returns the mean of the results
     * @param hits the results returned by a query
     * @return float the mean score
     */
    public float getMean(ScoreDoc[] hits){
        float total = 0;
        for (ScoreDoc hit : hits){
            total += hit.score;
        }
        return total / hits.length;
    }

    /**
     * Determines the standard deviation of the results
     * @param hits the results returned by a query
     * @param mean the mean score of the query
     * @return float the standard deviation
     */
    public float getStandardDeviation(ScoreDoc[] hits, float mean){
        float std = 0;
        for (ScoreDoc hit: hits){
            std += (hit.score - mean) * (hit.score - mean);
        }
        return std / hits.length;
    }

    /**
     * Scores the query based upon the number of deviations the relevant vs non-relevant documents
     * @param searchResults
     * @param query
     * @param judgements
     * @param scoreWriter the file to output to
     * @throws IOException
     */
    public void scoreDeviations(TopDocs searchResults,
                                MathQuery query,
                                Judgements judgements,
                                BufferedWriter scoreWriter
                                ) throws IOException{
        ScoreDoc[] hits = searchResults.scoreDocs;
        float mean = this.getMean(hits);
        float std = this.getStandardDeviation(hits, mean);
        Float rank;
        Float score = (float) 0;
        for (ScoreDoc hit : hits){
            Document doc = this.searcher.doc(hit.doc);
            rank = judgements.findResult(query, Functions.parseTitle(doc.get("path")));
            if (rank >= 0){
                if (rank == 0){
                    score += (-4 * (std / (hit.score - mean)));
                }else{
                    score += (rank * (std / (hit.score - mean)));
                }
            }
        }
        this.logger.log(Level.FINE, "Query:" + query.getQueryName() + " Score:" + score);
        scoreWriter.write(query.getQueryName() + "," + score);
        scoreWriter.newLine();
    }

    /**
     * Returns arxiv score for partially relevant and relevant with Precision at K for K in {5,10,15,20}
     * @param searchResults the documents return by the query
     * @param query the query to score
     * @param judgements the judgements of the query
     * @return ArrayList
     * @throws IOException
     */
    public ArrayList<ArrayList<Double>> arxivScore(TopDocs searchResults,
                                                   MathQuery query,
                                                   Judgements judgements) throws IOException{
        ScoreDoc[] hits = searchResults.scoreDocs;
        Float rank;
        ArrayList<Double>relevantScores = new ArrayList<Double>();
        ArrayList<Double>partialScores = new ArrayList<Double>();
        relevantScores.add(new Double (0));
        relevantScores.add(new Double (0));
        relevantScores.add(new Double (0));
        relevantScores.add(new Double (0));
        partialScores.add(new Double (0));
        partialScores.add(new Double (0));
        partialScores.add(new Double (0));
        partialScores.add(new Double (0));
        int index = 0;
        for (ScoreDoc hit : hits){
            Document doc = this.searcher.doc(hit.doc);
            rank = judgements.findResult(query, Functions.parseTitle(doc.get("path")));
            this.logger.log(Level.FINEST,
                            "Rank:" +
                            rank +
                            " Title:" +
                            Functions.parseTitle(doc.get("path")) +
                            " Path:" +
                            doc.get("path"));
            if (rank > Judgements.rLower){
                if (index < 5){
                    relevantScores.set(0, relevantScores.get(0) + new Double(1));
                }
                if (index < 10){
                    relevantScores.set(1, relevantScores.get(1) + new Double(1));
                }
                if (index < 15){
                    relevantScores.set(2, relevantScores.get(2) + new Double(1));
                }
                if (index < 20){
                    relevantScores.set(3, relevantScores.get(3) + new Double(1));
                }
            }
            if (rank > Judgements.pLower){
                if (index < 5){
                    partialScores.set(0, partialScores.get(0) + new Double(1));
                }
                if (index < 10){
                    partialScores.set(1, partialScores.get(1) + new Double(1));
                }
                if (index < 15){
                    partialScores.set(2, partialScores.get(2) + new Double(1));
                }
                if (index < 20){
                    partialScores.set(3, partialScores.get(3) + new Double(1));
                }
            }
            index += 1;
        }
        ArrayList<ArrayList<Double>> scores = new ArrayList<ArrayList<Double>>();
        scores.add(relevantScores);
        scores.add(partialScores);
        return scores;
    }


    /**
     * Returns the config used when searching
     * @return ConvertConfig
     */
    public ConvertConfig getConfig(){
        return this.config;
    }

    /**
     * Returns the IndexSearcher
     * @return IndexSearcher
     */
    public IndexSearcher getSearcher(){
        return this.searcher;
    }

    /**
     * An Exception that is thrown when the Search Config does not match the Index Config used when indexing
     * @author Dallas Fraser
     * @since 2017-11-06
     */
    public class SearchConfigException extends Exception{
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public SearchConfigException(String message){
            super(message);
        }
    }

}
