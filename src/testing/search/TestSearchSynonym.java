package testing.search;

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
import query.MathQuery;
import search.Search;
import search.SearchResult;
import testing.BaseTest;

public class TestSearchSynonym extends BaseTest{
    private Path folder;
    private Path index;
    private Path documents;
    private Search searcher;
    private Path queries;

    @Before
    public void setUp() throws Exception{
        // uncomment for debugging
        // this.debugLogger();
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "index_test_1");
        this.documents = Paths.get(this.folder.toString(), "documents");
        this.index = Paths.get(this.folder.toString(), "index");
        this.queries = Paths.get(this.folder.toString(), "queries", "queries.xml");
        File dir = this.index.toFile();
        // attempt to create the directory here
        boolean successful = dir.mkdir();
        if (!successful){
          // creating the directory failed
          System.out.println("failed trying to create the directory");
          throw new Exception("Failed to create directory");
        }
        // create the index
        ConvertConfig config = new ConvertConfig();
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
        IndexFiles indexer = new IndexFiles();
        indexer.indexDirectory(this.index, this.documents, true, config);
        // init the searching object
        this.searcher = new Search(this.index, config);
    }

    @After
    public void tearDown(){
        // remove the index created
        this.deleteDirectory(this.index);
    }

    @Test
    public void testSearchQueries() {
        try {
            ArrayList<SearchResult> results = this.searcher.searchQueries(this.queries);
            assertEquals(results.size(), 1);
            ArrayList<String> expect = new ArrayList<String>();
            expect.add("math-ph0607065_1_57");
            expect.add("1307.6316_1_108");
            expect.add("1301.6848_1_17");
            expect.add("1303.3122_1_41");
            assertEquals(this.compareResults(expect, results.get(0), this.searcher), true);
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

    @Test
    public void testSearchQuery() {
        try {
            MathQuery mq = new MathQuery("test1");
            mq.addTerm("#('+','*','n')#");
            SearchResult results = this.searcher.searchQuery(mq);
            ArrayList<String> expect = new ArrayList<String>();
            expect.add("1307.6316_1_108");
            expect.add("math-ph0607065_1_57");
            expect.add("1301.6848_1_17");
            expect.add("1303.3122_1_41");
            assertEquals(this.compareResults(expect, results, this.searcher), true);
            System.out.println(results);
            results.explainResults(this.searcher.getSearcher());
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Fail IO exception");
        }
    }
}
