package naiveMathIndexer.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

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

public class findOptimal {
    private Path documents;
    private Path index;
    private BufferedWriter output;
    private ArrayList<MathQuery> mathQueries;
    private Results answers;
    private static String field = "contents";
    private static Float rLower = new Float(0.0);
    private static int TOP_K = 10000;
    private QueryBuilder builder;
    public findOptimal(ConvertConfig config,
                       Path documents,
                       Path index,
                       BufferedWriter output,
                       Path queries,
                       Path results) throws IOException,
                                            InterruptedException,
                                            XPathExpressionException,
                                            ParserConfigurationException,
                                            SAXException{
        ArrayList<String> features = new ArrayList<String>();
        features.add(ConvertConfig.COMPOUND);
        features.add(ConvertConfig.EDGE);
        features.add(ConvertConfig.EOL);
        features.add(ConvertConfig.LOCATION);
        features.add(ConvertConfig.TERMINAL);
        features.add(ConvertConfig.UNBOUNDED);
        features.add(ConvertConfig.SHORTENED);
        this.documents = documents;
        this.index = index;
        this.output = output;
        ParseQueries queryLoader = new ParseQueries(queries.toFile());
        this.mathQueries = queryLoader.getQueries();
        this.answers = new Results(results.toFile());
        Analyzer analyzer = new MathAnalyzer();
        this.builder = new QueryBuilder(analyzer);
    }
    public void optimize(ConvertConfig config, ArrayList<String>features) throws IOException{
        Path indexPath;
        float baseline = scoreIndex(createIndex(config));
        float check;
        float bestFeatureScore = (float) 0.0;
        String bestFeature = "";
        ArrayList<String> keepFeatures = new ArrayList<String>();
        for(String feature : features){
            config.flipBit(feature);
            indexPath = this.createIndex(config);
            config.flipBit(feature);
            check = this.scoreIndex(indexPath);
            if (check > baseline){
                keepFeatures.add(feature);
                if (check > bestFeatureScore){
                    bestFeature = feature;
                    bestFeatureScore = check;
                }
            }
        }
        if (!bestFeature.equals("")){
            // then information to gain by a feature
            ConvertConfig new_config = config.copy();
            new_config.flipBit(bestFeature);
            this.optimize(config, keepFeatures);
        }else{
            // then no information gain from any of the features
            this.output.write("Best Features" + config.toString());
        }
        return;
        
    }
    public String parseTitle(String title){
        String[] parts = title.split("/");
        String filename = parts[parts.length -1];
        String[] temp = filename.split("\\.");
        String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
        return String.join(".", nameparts);
    }
    public float reciprocal_rank(IndexSearcher searcher,
                                 TopDocs searchDocs,
                                 MathQuery query) throws IOException{
        ScoreDoc[] hits = searchDocs.scoreDocs;
        float rank = (float) 0.0;
        int count = 0;
        for (ScoreDoc hit : hits){
            Document doc = searcher.doc(hit.doc);
            rank = this.answers.findResult(query, this.parseTitle(doc.get("path")));
            if (rank > findOptimal.rLower){
                rank = 1 / (float) count;
                break;
            }
            count += 1;
        }
        return rank;
    }

    public float found_answer(IndexSearcher searcher, TopDocs searchDocs, MathQuery query) throws IOException{
        ScoreDoc[] hits = searchDocs.scoreDocs;
        ArrayList<String> filenames = new ArrayList<String>();
        for (ScoreDoc hit : hits){
            // build the list of filenames to check against the answers
            Document doc = searcher.doc(hit.doc);
            filenames.add(this.parseTitle(doc.get("path")));
        }
        return this.answers.resultsContainAnswers(query, filenames);
    }

    public float scoreIndex(Path index) throws IOException{
        float mean = (float) 0.0;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(index));
        IndexSearcher searcher = new IndexSearcher(reader);
        int count = 0;
        for (MathQuery mq: this.mathQueries){
            Query realQuery = this.builder.createBooleanQuery(findOptimal.field, mq.getQuery());
            if (realQuery == null){
                System.out.println("Query has no elements");
            }else{
                BooleanQuery.Builder bq = new BooleanQuery.Builder();
                Query buildQuery = mq.buildQuery(realQuery.toString().split("contents:"),
                                                 field,
                                                 bq);
                TopDocs searchResults = searcher.search(buildQuery, findOptimal.TOP_K);
                mean += this.reciprocal_rank(searcher, searchResults, mq);
            }
            count += 1;
        }
        mean = mean / (float) count;
        return mean;
    }
    public Path createIndex(ConvertConfig config) throws IOException{
        String name = config.toString().replace(" ", "");
        Path directoryPath = Paths.get(this.index.toString(), name);
        File directory = directoryPath.toFile();
        // already created
        if (directory.exists()){
            return directoryPath;
        }
        if(!directory.mkdir()){
            throw new IOException("Unable to create directory");
        }
        // now create directory just like normal
        IndexFiles idf = new IndexFiles();
        this.output.write("Creating Index" + name);
        idf.indexDirectory(directoryPath.toString(), this.documents.toString(), true, config);
        return directoryPath;
    }
}
