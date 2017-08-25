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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.BooleanClause;
import org.xml.sax.SAXException;
import naiveMathIndexer.index.MathAnalyzer;
import query.MathQuery;
import results.Results;
import naiveMathIndexer.query.Search;
public class RecallCheck  extends Search{
    private final String WILDCARD = "'*'";
    private final String WILDCHARACTER = "'???'";
    public RecallCheck(Path index, Path queries, Path results)
            throws  IOException,
                    XPathExpressionException,
                    ParserConfigurationException,
                    SAXException,
                    InterruptedException,
                    ParseException{
        String field = "contents";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(index));
        IndexSearcher searcher = new IndexSearcher(reader);
        Similarity similarity = new ClassicSimilarity();
        searcher.setSimilarity(similarity);
        Analyzer analyzer = new MathAnalyzer();
        ParseQueries queryLoader = new ParseQueries(queries.toFile());
        ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
        QueryBuilder builder = new QueryBuilder(analyzer);
        Results answers = new Results(results.toFile(), false, false);
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
                    System.out.println("Query has no elements");
                }else{
                    BooleanQuery.Builder bq = new BooleanQuery.Builder();
                    Query buildQuery = this.buildQuery(realQuery.toString().split("contents:"), field, bq);
                    TopDocs searchResultsWild = searcher.search(buildQuery, size);
                    ArrayList<String> files = new ArrayList<String>();
                    ScoreDoc[] hits = searchResultsWild.scoreDocs;
                    for (ScoreDoc hit: hits){
                        Document doc = searcher.doc(hit.doc);
                        files.add(this.parseTitle(doc.get("path")));
//                        System.out.println("Query name:" + mq.getQueryName() + " " + searcher.explain(buildQuery, hit.doc));
                    }
                    int[] recalls = answers.recallResult(mq, files);
                    r_docs += recalls[0];
                    r_found += recalls[1];
                    pr_docs += recalls[2];
                    pr_found += recalls[3];
                }
            }
            System.out.println(((float) r_found / (float) r_docs) + " " + ((float) pr_found / (float)pr_docs));
//            System.out.println("Recall at " + size + " " + r_found + " / " + r_docs + " = " + ((float) r_found / (float) r_docs) +
//                               " PRRecall at " + pr_found + " / " + pr_docs + " = " + ((float) pr_found / (float)pr_docs));
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
        String date = new SimpleDateFormat("dd-MM-yyyy:HH:mm").format(new Date());
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
            // do the actual searching
            new RecallCheck(index, queries, results);
            // close the files
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
