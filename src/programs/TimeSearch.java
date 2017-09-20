package programs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.xml.sax.SAXException;
import index.ConvertConfig;
import index.MathAnalyzer;
import query.ParseQueries;
import search.Search;
import query.MathQuery;
import utilities.ProjectLogger;

public class TimeSearch extends Search{
    private Logger logger;
    public TimeSearch(Path index, Path queries, int size) throws IOException,
                                                                  XPathExpressionException,
                                                                  ParserConfigurationException,
                                                                  SAXException,
                                                                  InterruptedException,
                                                                  ParseException{
        this(index, queries, new ConvertConfig(), size, ProjectLogger.getLogger());
    }
    public TimeSearch(Path index,
                       Path queries,
                       ConvertConfig config,
                       int size) throws IOException,
                                        XPathExpressionException,
                                        ParserConfigurationException,
                                        SAXException,
                                        InterruptedException,
                                        ParseException{
        this(index, queries, config, size, ProjectLogger.getLogger());
    }
    public TimeSearch(Path index,
                       Path queries,
                       ConvertConfig config,
                       int size,
                       Logger logger) throws IOException,
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
        ParseQueries queryLoader = new ParseQueries(queries.toFile(), config);
        ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
        QueryBuilder builder = new QueryBuilder(analyzer);
        Date start, end;
        ArrayList<Double> times = new ArrayList<Double>();
        for (MathQuery mq: mathQueries){
            Query realQuery = builder.createBooleanQuery(field, mq.getQuery());
            if (realQuery == null){
                this.logger.log(Level.WARNING, "Query has no elements: " + mq);
            }else{
                BooleanQuery.Builder bq = new BooleanQuery.Builder();
                Query buildQuery = mq.buildQuery(realQuery.toString().split("contents:"), field, bq);
                start = new Date();
                searcher.search(buildQuery, size);
                end = new Date();
                times.add(new Double(end.getTime() - start.getTime()));
            }
        }
        Double min = new Double(0);
        Double max = new Double(0);
        Double total = new Double(0);
        Double variance = new Double(0);
        Double std = new Double(0);
        Double mean = new Double(0);
        if (times.size() > 0){
            min = times.get(0);
            max = times.get(0);
            for (Double time: times){
                if (time < min){
                    min = time;
                }
                if(time > max){
                    max = time;
                }
                total += time;
            }
            mean = total / times.size();
            for (Double time: times){
                variance += Math.pow((time - mean), 2);
            }
            std = Math.sqrt((variance / times.size()));
        }
        
        System.out.println(" Total time:" + total  +
                           " Mean:" + mean +
                           " Min:" + min + 
                           " Max:" + max +
                           " Std:" + std
                           );
        queryLoader.deleteFile();
    }
    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava naiveMathIndexer.TimeQueries [-index dir] [-queries file] [-precision precision] [-log logFile]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        ConvertConfig config = new ConvertConfig();
        config.optimalConfig();
        // default values
        int precision = 1000;
        Path index = Paths.get(System.getProperty("user.dir"), "resources", "index", "arXiv", "config", "compound-unbounded-edge_pairs");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-ArXiv.xml");
        Path logFile = Paths.get(System.getProperty("user.dir"), "resources", "output", "arXiv", "recallChceck.log");
        for(int i = 0;i < args.length;i++) {
          if ("-index".equals(args[i])) {
            index = Paths.get(args[i+1]);
            i++;
          } else if ("-queries".equals(args[i])) {
            queries = Paths.get(args[i+1]);
            i++;
          }else if ("-log".equals(args[i])){
            logFile = Paths.get(args[i+1]);
            i++;
          }else if("-precision".equals(args[i])){
            precision = Integer.parseInt(args[i+1]);
            i++;
          }
        }
        // setup the logger

        ProjectLogger.setLevel(Level.INFO);
        ProjectLogger.setLogFile(logFile);
        try {
            // time all the different queries
            new TimeSearch(index, queries, config, precision);
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
        }
    }
}
