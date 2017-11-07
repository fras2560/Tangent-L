package testing.query;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import index.ConvertConfig;
import query.MathQuery;
import search.Search;

public class TestMathQueryBMMin extends TestMathQueryBase {
    private Path folder;
    private Path index;
    private Path documents;
    private Search searcher;
    private Path queries;
    private ConvertConfig config;

    @Before
    public void setUp() throws Exception{
        // uncomment for debugging
        // this.debugLogger();
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "test_bm25min_1");
        this.documents = Paths.get(this.folder.toString(), "documents");
        this.index = Paths.get(this.folder.toString(), "index");
        this.queries = Paths.get(this.folder.toString(), "queries.xml");
        this.config = new ConvertConfig();
        this.config.setQueryType(ConvertConfig.BM25_DISTANCE_QUERY);
    }

    @After
    public void tearDown(){
        // remove the index created
        try {
            this.searcher.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // this.deleteDirectory(this.index);
    }

    @Test
    public void testNormal(){
        try {
            this.searcher = this.indexDirectory(this.folder, this.documents, this.index, this.config);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<MathQuery> queries = null;
        try {
            queries = this.loadQueries(this.queries, config);
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals(queries.size(), 1);
        Map<String, Float> results = null;
        try {
            results = this.runQuery(queries.get(0), this.searcher, 3);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, Float> expect = new HashMap<String, Float>();
        for (String doc : results.keySet()) {
            System.out.println(doc + ":" + results.get(doc));
        }
    }

}
