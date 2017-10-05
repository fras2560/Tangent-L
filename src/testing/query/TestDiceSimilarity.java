package testing.query;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import index.ConvertConfig;
import index.IndexFiles;
import query.DiceSimilarity;
import query.MathQuery;
import query.ParseQueries;
import search.Search;
import search.SearchResult;
import testing.BaseTest;

public class TestDiceSimilarity extends BaseTest{
    private Path folder;
    private Path index;
    private Path documents;
    private Search searcher;
    private Path queries;

    @Before
    public void setUp() throws Exception{
        // uncomment for debugging
        // this.debugLogger();
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "test_dice_similarity");
        this.documents = Paths.get(this.folder.toString(), "documents");
        this.index = Paths.get(this.folder.toString(), "index");
        this.queries = Paths.get(this.folder.toString(), "queries", "queries.xml");
        File dir = this.index.toFile();
        // attempt to create the directory here
        boolean successful = dir.mkdir();
        if (!successful){
          this.deleteDirectory(this.index);
          successful = dir.mkdir();
          if (!successful) {
              // creating the directory failed
              System.out.println("failed trying to create the directory");
              throw new Exception("Failed to create directory");
          }
        }
        // create the index
        IndexFiles indexer = new IndexFiles();
        indexer.indexDirectory(this.index, this.documents, true, new ConvertConfig(), new DiceSimilarity());
        // init the searching object
        this.searcher = new Search(this.index);
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
        this.deleteDirectory(this.index);
    }

    @Test
    public void testSearchQuery() {
        try {
            ParseQueries queryLoader = new ParseQueries(queries.toFile(), searcher.getConfig());
            ArrayList<MathQuery> mathQueries;
            mathQueries = queryLoader.getQueries();
            queryLoader.deleteFile();
            MathQuery query = mathQueries.get(0);
            SearchResult result = this.searcher.searchQuery(query);
            ArrayList<String> expect2 = new ArrayList<String>();
            expect2.add("math-ph0607065_1_57");
            expect2.add("1301.6848_1_17");
            expect2.add("1303.3122_1_41");
            expect2.add("1307.6316_1_108");
            System.out.println(result);
            result.explainResults(this.searcher.getSearcher());
            assertEquals(this.compareResults(expect2, result, this.searcher), true);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Fail IO exception");
        } catch(InterruptedException e){
            e.printStackTrace();
            fail("Interruped exception");
        } catch(XPathExpressionException e){
            e.printStackTrace();
            fail("XPath Expression exception");
        } catch(ParserConfigurationException e){
            e.printStackTrace();
            fail("Parser Configuration exception");
        } catch(SAXException e){
            e.printStackTrace();
            fail("SAX exception");
        }
    }

}
