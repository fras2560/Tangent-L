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
import query.MathQuery;
import query.ParseQueries;
import search.Search;
import search.SearchResult;
import testing.BaseTest;

public class TestDiceSimilarity2 extends BaseTest{
    private Path folder;
    private Path index;
    private Path documents;
    private Search searcher;
    private Path queries;

    @Before
    public void setUp() throws Exception{
        // uncomment for debugging
        // this.debugLogger();
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "test_dice_similarity2");
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
        ConvertConfig config = new ConvertConfig();
        config.setBooleanAttribute(ConvertConfig.BAGS_OF_WORDS, true);
        config.setQueryType(ConvertConfig.DICE_QUERY);
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
        indexer.indexDirectory(this.index, this.documents, true, config);
        // init the searching object
        this.searcher = new Search(this.index, config);
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
            expect2.add("22538");
            expect2.add("13014");
            // the score should be about
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
