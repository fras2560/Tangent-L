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
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.xml.sax.SAXException;
import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
import index.MathAnalyzer;
import query.ParseQueries;
import query.MathQuery;
import query.MathSimilarity;
import utilities.Functions;
import utilities.ProjectLogger;


public class Search {
    private Logger logger ;
    private IndexSearcher searcher;
    private QueryBuilder builder;
    private ConvertConfig config;
    private boolean synonym;
    private static final int DEFAULT_K = 100;
    private static final int MAX_CLUASES = 4096;

    public Search(Path index) throws IOException, SearchConfigException, ConvertConfigException{
        this(index, ProjectLogger.getLogger(), new ConvertConfig());
    }

    public Search(Path index, Logger logger) throws IOException, SearchConfigException, ConvertConfigException{
        this(index, logger, new ConvertConfig());
    }

    public Search(Path index, ConvertConfig config) throws IOException, SearchConfigException, ConvertConfigException{
        this(index, ProjectLogger.getLogger(), config);
    }

    public Search(Path index, Logger logger, ConvertConfig config) throws IOException,
                                                                          SearchConfigException,
                                                                          ConvertConfigException{
        // increase the clause count since formulas can be a slight bit longer
        BooleanQuery.setMaxClauseCount(Search.MAX_CLUASES);
        // remember if synonyms were used when indexing
        this.synonym = config.getSynonym();
        // dont want synonyms if not needed if in the analyzor
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, false);
        // make sure the config and index are compatible
        ConvertConfig indexConfig = new ConvertConfig();
        indexConfig.loadConfig(index);
        // index config needs to be compatible with the searching config (not necessarily reverse direction)
        if (!indexConfig.compatible(config)){
            System.out.println(config + " vs " + indexConfig);
            throw new SearchConfigException("Config did not match index");
        }
        IndexReader reader = DirectoryReader.open(FSDirectory.open(index));
        this.searcher = new IndexSearcher(reader);
        Similarity similarity = MathSimilarity.getSimilarity();
        this.searcher.setSimilarity(similarity);
        this.builder = new QueryBuilder(new MathAnalyzer(config));
        this.config = config;
        this.logger = logger;
    }

    public ArrayList<String> searchQueryFiles(MathQuery mathQuery) throws IOException{
        return this.searchQueryFiles(mathQuery, Search.DEFAULT_K);
    }

    public ArrayList<String>searchQueryFiles(MathQuery mathQuery, int k) throws IOException{
        this.logger.log(Level.FINER, "Query: " + mathQuery.getQuery().replaceAll("//", "//") +
                "Query: name: " + mathQuery.getQueryName());
        Query realQuery = this.builder.createBooleanQuery(mathQuery.getFieldName(), mathQuery.getQuery());
        ArrayList<String> files = new ArrayList<String>();
        if (realQuery != null){
            this.logger.log(Level.FINEST, realQuery.toString());
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            Query buildQuery = mathQuery.buildQuery(realQuery.toString().split(mathQuery.getFieldName() + ":"),
                                                    mathQuery.getFieldName(),
                                                    bq,
                                                    this.synonym);
            this.logger.log(Level.FINEST, "Boolean Query Size:" + bq.build().clauses().size());
            this.logger.log(Level.FINEST, "BuildQuery:" + buildQuery);
            this.logger.log(Level.FINEST, "RealQuery:" + realQuery);
            TopDocs searchResultsWild = this.searcher.search(buildQuery, k);
            ScoreDoc[] hits = searchResultsWild.scoreDocs;
            for (ScoreDoc hit: hits){
                Document doc = searcher.doc(hit.doc);
                files.add(Functions.parseTitle(doc.get("path")));
            }
        }
        return files;
    }

    public SearchResult searchQuery(MathQuery mathQuery) throws IOException{
        return this.searchQuery(mathQuery, Search.DEFAULT_K);
    }

    public SearchResult searchQuery(MathQuery mathQuery, int k) throws IOException{
        this.logger.log(Level.FINER,
                        "Query: " +
                        mathQuery.getQuery().replaceAll("//", "//") +
                        " Query: name: " +
                        mathQuery.getQueryName());
        Query realQuery = this.builder.createBooleanQuery(mathQuery.getFieldName(), mathQuery.getQuery());
        SearchResult result = null;
        if (realQuery == null){
            this.logger.log(Level.WARNING, "Query has no elements: " + mathQuery.getQueryName());
            result = new SearchResult(null, mathQuery, k, null);
        }else{
            this.logger.log(Level.FINEST, realQuery.toString());
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            Query buildQuery = mathQuery.buildQuery(realQuery.toString().split(mathQuery.getFieldName() + ":"),
                                                    mathQuery.getFieldName(),
                                                    bq,
                                                    this.synonym);
            this.logger.log(Level.FINEST, "Boolean Query Size:" + bq.build().clauses().size());
            this.logger.log(Level.FINEST, "BuildQuery:" + buildQuery);
            this.logger.log(Level.FINEST, "RealQuery:" + realQuery);
            TopDocs searchResultsWild = this.searcher.search(buildQuery, k);
            result = new SearchResult(searchResultsWild, mathQuery, k, buildQuery);
        }
        return result;
    }

    public ArrayList<SearchResult> searchQueries(Path queries) throws XPathExpressionException,
                                                                      IOException,
                                                                      InterruptedException,
                                                                      ParserConfigurationException,
                                                                      SAXException{
        return this.searchQueries(queries, Search.DEFAULT_K);
    }

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

    public void explainQueries(Path queries) throws IOException,
                                                    XPathExpressionException,
                                                    InterruptedException,
                                                    ParserConfigurationException,
                                                    SAXException{
        this.explainQueries(queries, Search.DEFAULT_K);
    }
    
    public void explainQueries(Path queries, int k) throws IOException,
                                                    XPathExpressionException,
                                                    InterruptedException,
                                                    ParserConfigurationException,
                                                    SAXException{
        ArrayList<SearchResult> queryResults = this.searchQueries(queries, k);
        for (SearchResult queryResult : queryResults){
            // loop through each query and explain it
            queryResult.explainResults(searcher);
        }
    }

    public void explainQueries(Path queries, Path output)throws IOException,
                                                                XPathExpressionException,
                                                                InterruptedException,
                                                                ParserConfigurationException,
                                                                SAXException{
        this.explainQueries(queries, output, Search.DEFAULT_K);
    }

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
    }
    
    public void recordQueries(Path queries, BufferedWriter queryWriter) throws IOException,
                                                                               XPathExpressionException,
                                                                               InterruptedException,
                                                                               ParserConfigurationException,
                                                                               SAXException{
        this.recordQueries(queries, queryWriter, Search.DEFAULT_K);
    }

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

    public float getMean(ScoreDoc[] hits){
        float total = 0;
        for (ScoreDoc hit : hits){
            total += hit.score;
        }
        return total / hits.length;
    }

    public float getStandardDeviation(ScoreDoc[] hits, float mean){
        float std = 0;
        for (ScoreDoc hit: hits){
            std += (hit.score - mean) * (hit.score - mean);
        }
        return std / hits.length;
    }

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



    public ConvertConfig getConfig(){
        return this.config;
    }

    public IndexSearcher getSearcher(){
        return this.searcher;
    }

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
