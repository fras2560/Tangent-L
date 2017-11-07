package testing.query;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.xml.sax.SAXException;
import index.ConvertConfig;
import index.IndexFiles;
import query.MathQuery;
import query.ParseQueries;
import search.Search;
import testing.BaseTest;
import utilities.Functions;

public class TestMathQueryBase extends BaseTest{
    public Search indexDirectory(Path folder, Path documents, Path index, ConvertConfig config) throws Exception{
        File dir = index.toFile();
        // attempt to create the directory here
        boolean successful = dir.mkdir();
        if (!successful){
          this.deleteDirectory(index);
          successful = dir.mkdir();
          if (!successful) {
              // creating the directory failed
              System.out.println("failed trying to create the directory");
              throw new Exception("Failed to create directory");
          }
        }
        // create the index
        IndexFiles indexer = new IndexFiles();
        indexer.indexDirectory(index, documents, true, config);
        // init the searching object
        return new Search(index, config);
    }

    public List<MathQuery> loadQueries(Path queries, ConvertConfig config) throws IOException,
                                                                                  InterruptedException,
                                                                                  XPathExpressionException,
                                                                                  ParserConfigurationException,
                                                                                  SAXException{
        ParseQueries pq = new ParseQueries(queries.toFile(), config);
        List<MathQuery> q = pq.getQueries();
        pq.deleteFile();
        return q;
    }

    public Map<String, Float> runQuery(MathQuery mq, Search searcher, int k) throws IOException{
        Map<String, Float> results = new HashMap<String, Float>();
        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        Query buildQuery = mq.buildQuery(mq.getFieldName(),
                                         bq,
                                         searcher.getSynonym(),
                                         searcher.getConfig(),
                                         searcher.getSearcher().collectionStatistics(mq.getFieldName()));
        TopDocs searchResultsWild = searcher.getSearcher().search(buildQuery, k);
        ScoreDoc[] hits = searchResultsWild.scoreDocs;
        System.out.println("Query: " + mq.toString());
        System.out.println("Documents Returned: " + hits.length);
        for (ScoreDoc hit: hits){
            Document doc = searcher.getSearcher().doc(hit.doc);
            results.put(Functions.parseTitle(doc.get("path")), hit.score);
        }
        return results;
        
    }
}
