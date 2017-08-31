package naiveMathIndexer.query;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.BooleanClause;
import org.xml.sax.SAXException;

import naiveMathIndexer.index.ConvertConfig;
import naiveMathIndexer.index.MathAnalyzer;
import query.MathQuery;
import results.Results;

public class Search {
    private final String WILDCARD = "'*'";
    private final String WILDCHARACTER = "'???'";
    public Search(){
        
    }
    public Search(Path index,
                  Path queries,
                  Path results,
                  BufferedWriter queryWriter,
                  BufferedWriter resultsWriter,
                  BufferedWriter scoreWriter)
            throws  IOException,
                    XPathExpressionException,
                    ParserConfigurationException,
                    SAXException,
                    InterruptedException,
                    ParseException{
        String field = "contents";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(index));
        IndexSearcher searcher = new IndexSearcher(reader);
        Similarity similarity = new BM25Similarity();
        searcher.setSimilarity(similarity);
        Analyzer analyzer = new MathAnalyzer();
        ParseQueries queryLoader = new ParseQueries(queries.toFile());
        ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
        QueryBuilder builder = new QueryBuilder(analyzer);
        Results answers = new Results(results.toFile());
        for (MathQuery mq: mathQueries){
            System.out.println("Query: " + mq.getQuery().replaceAll("//", "//") +
                               " Query: name: " + mq.getQueryName());
            Query realQuery = builder.createBooleanQuery(field, mq.getQuery());
            if (realQuery == null){
                System.out.println("Query has no elements");
                resultsWriter.write(mq.getQueryName() + ",0,0,0,0,0,0,0,0");
                resultsWriter.newLine();
            }else{
                System.out.println(realQuery);
                System.out.println(realQuery.toString());
                
                BooleanQuery.Builder bq = new BooleanQuery.Builder();
                Query buildQuery = mq.buildQuery(realQuery.toString().split("contents:"),
                                                 field,
                                                 bq);
                System.out.println("Boolean Query Size:" + bq.build().clauses().size());
                System.out.println("BuildQuery:" + buildQuery);
                System.out.println("RealQuery:" + realQuery);
                TopDocs searchResultsWild = searcher.search(buildQuery, 20);
                TopDocs searchResultsTerm = searcher.search(realQuery, 20);
                this.checkResults(searcher,
                                  searchResultsWild,
                                  mq,
                                  answers,
                                  queryWriter,
                                  resultsWriter,
                                  buildQuery);
                this.differentResults(searchResultsWild, searcher.search(buildQuery, 20));
                searchResultsWild = searcher.search(buildQuery, 100);
                this.scoreDeviations(searcher,
                                     searchResultsWild,
                                     mq,
                                     answers,
                                     scoreWriter,
                                     buildQuery);
            }
        }
        queryLoader.deleteFile();
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
        System.out.println("Both queries are the same:" + same);
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
    public void scoreDeviations(IndexSearcher searcher,
                                TopDocs searchResults,
                                MathQuery query,
                                Results results,
                                BufferedWriter scoreWriter,
                                Query q
                                ) throws IOException{
        ScoreDoc[] hits = searchResults.scoreDocs;
        float mean = this.getMean(hits);
        float std = this.getStandardDeviation(hits, mean);
        Float rank;
        Float score = (float) 0;
        for (ScoreDoc hit : hits){
            Document doc = searcher.doc(hit.doc);
            rank = results.findResult(query, this.parseTitle(doc.get("path")));
            if (rank >= 0){
                if (rank == 0){
                    score += (-4 * (std / (hit.score - mean)));
                }else{
                    score += (rank * (std / (hit.score - mean)));
                }
            }
        }
        System.out.println("Query:" + query.getQueryName() + " Score:" + score);
        scoreWriter.write(query.getQueryName() + "," + score);
        scoreWriter.newLine();
    }

    public void checkResults(IndexSearcher searcher,
                              TopDocs searchResults,
                              MathQuery query,
                              Results results,
                              BufferedWriter queryWriter,
                              BufferedWriter resultsWriter,
                              Query q) throws IOException{
        ScoreDoc[] hits = searchResults.scoreDocs;
        Float rank;
        Float rLower = new Float(2.0);
        Float pLower = new Float(0.0);
        int rk5 = 0;
        int rk10 = 0;
        int rk15 = 0;
        int rk20 = 0;
        int pk5 = 0;
        int pk10 = 0;
        int pk15 = 0;
        int pk20 = 0;
        int index = 0;
        for (ScoreDoc hit : hits){
            Document doc = searcher.doc(hit.doc);
            queryWriter.write(query.getQueryName() +
                              " 1 " +
                              doc.get("path") +
                              " " +
                              (index+1) +
                              " " +
                              hit.score +
                              " RITUW");
            queryWriter.newLine();
            rank = results.findResult(query, this.parseTitle(doc.get("path")));
//            System.out.println("Rank:" + rank + " Title:" + this.parseTitle(doc.get("path")) + " Path:" + doc.get("path"));
//            System.out.println("Explanation:" + searcher.explain(q, hit.doc));
            if (rank > rLower){
                if (index < 5){
                    rk5 += 1;
                }
                if (index < 10){
                    rk10 += 1;
                }
                if (index < 15){
                    rk15 += 1;
                }
                rk20 += 1;
            }
            if (rank > pLower){
                if (index < 5){
                    pk5 += 1;
                }
                if (index < 10){
                    pk10 += 1;
                }
                if (index < 15){
                    pk15 += 1;
                }
                pk20 += 1;
            }
            index += 1;
        }
        resultsWriter.write(query.getQueryName() + "," + rk5 + "," + rk10 + ","+ rk15 + ","+ rk20
                            + "," + pk5 + "," + pk10 + ","+ pk15 + ","+ pk20);
        resultsWriter.newLine();
        System.out.println(query.getQueryName() + "," + rk5 + "," + rk10 + ","+ rk15 + ","+ rk20
                           + "," + pk5 + "," + pk10 + ","+ pk15 + ","+ pk20);
    }
    public String parseTitle(String title){
        String[] parts = title.split("/");
        String filename = parts[parts.length -1];
        String[] temp = filename.split("\\.");
        String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
        return String.join(".", nameparts);
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
        String date = new SimpleDateFormat("dd-MM-yyyy:HH:mm").format(new Date());
        Path queryOutput = Paths.get(System.getProperty("user.dir"), "resources", "output", date + "-queries.txt");
        Path resultOutput = Paths.get(System.getProperty("user.dir"), "resources", "output", date + "-results.txt");
        Path scoreOutput = Paths.get(System.getProperty("user.dir"), "resources", "output", date + "-score.txt");
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
        BufferedWriter queryWriter = null;
        BufferedWriter resultsWriter = null;
        BufferedWriter scoreWriter = null;
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
            // write out the score results for each query
            File scoreText = scoreOutput.toFile();
            scoreText.createNewFile();
            FileOutputStream sis = new FileOutputStream(scoreText);
            OutputStreamWriter sosw = new OutputStreamWriter(sis);
            scoreWriter = new BufferedWriter(sosw);
            // do the actual searching
            new Search(index, queries, results, queryWriter, resultsWriter, scoreWriter);
            // close the files
            resultsWriter.close();
            queryWriter.close();
            scoreWriter.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
            if(queryWriter != null){
                queryWriter.close();
            }
            if (resultsWriter != null){
                resultsWriter.close();
            }
            if(scoreWriter != null){
                scoreWriter.close();
            }
        }
    }
}
