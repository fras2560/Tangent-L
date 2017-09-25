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
import query.ParseQueries;
import search.Judgements;
import search.Search;
import search.SearchResult;
import testing.BaseTest;

public class TestSearch extends BaseTest{
    private Path folder;
    private Path index;
    private Path documents;
    private Search searcher;
    private Path judgements;
    private Path queries;

    @Before
    public void setUp() throws Exception{
        // uncomment for debugging
        // this.debugLogger();
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "index_test_1");
        this.documents = Paths.get(this.folder.toString(), "documents");
        this.index = Paths.get(this.folder.toString(), "index");
        this.judgements = Paths.get(this.folder.toString(), "judgements", "judgements.txt");
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
        IndexFiles indexer = new IndexFiles();
        indexer.indexDirectory(this.index, this.documents, true, new ConvertConfig());
        // init the searching object
        this.searcher = new Search(this.index);
    }

    @After
    public void tearDown(){
        // remove the index created
        this.deleteDirectory(this.index);
    }

    @Test
    public void testDifferentResults() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetMean() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetStandardDeviation() {
        fail("Not yet implemented");
    }

    @Test
    public void testSearchQueries() {
        try {
            ArrayList<SearchResult> results = this.searcher.searchQueries(this.queries);
            assertEquals(results.size(), 1);
            ArrayList<String> expect = new ArrayList<String>();
            expect.add("1301.6848_1_17");
            expect.add("1307.6316_1_108");
            expect.add("1303.3122_1_41");
            expect.add("math-ph0607065_1_57");
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
            ParseQueries queryLoader = new ParseQueries(queries.toFile(), searcher.getConfig());
            ArrayList<MathQuery> mathQueries;
            mathQueries = queryLoader.getQueries();
            queryLoader.deleteFile();
            MathQuery query = mathQueries.get(0);
            SearchResult result = this.searcher.searchQuery(query);
            ArrayList<String> expect = new ArrayList<String>();
            expect.add("1301.6848_1_17");
            expect.add("1307.6316_1_108");
            expect.add("1303.3122_1_41");
            expect.add("math-ph0607065_1_57");
            assertEquals(this.compareResults(expect, result, this.searcher), true);
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
    public void testSearchQueryFiles() {
        try {
            ParseQueries queryLoader = new ParseQueries(queries.toFile(), searcher.getConfig());
            ArrayList<MathQuery> mathQueries;
            mathQueries = queryLoader.getQueries();
            queryLoader.deleteFile();
            MathQuery query = mathQueries.get(0);
            ArrayList<String> results = this.searcher.searchQueryFiles(query);
            assertEquals(results.get(0) , "1301.6848_1_17");
            assertEquals(results.get(1) , "1307.6316_1_108");
            assertEquals(results.get(2) , "1303.3122_1_41");
            assertEquals(results.get(3) , "math-ph0607065_1_57");
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
    public void testarxivScore() {
        // load the judgements
        Judgements answers = new Judgements(this.judgements.toFile());
        // load the queries
        ParseQueries queryLoader;
        ArrayList<ArrayList<Double>> scores;
        try {
            queryLoader = new ParseQueries(queries.toFile(), searcher.getConfig());
            ArrayList<MathQuery> mathQueries;
            mathQueries = queryLoader.getQueries();
            queryLoader.deleteFile();
            MathQuery query = mathQueries.get(0);
            SearchResult searchResults = this.searcher.searchQuery(query);
            scores = this.searcher.arxivScore(searchResults.getResults(), searchResults.getMathQuery(), answers);
            // check relevant scores
            assertEquals(scores.get(0).get(0), new Double(1.0));
            assertEquals(scores.get(0).get(1), new Double(1.0));
            assertEquals(scores.get(0).get(2), new Double(1.0));
            assertEquals(scores.get(0).get(3), new Double(1.0));
            // check partial scores
            assertEquals(scores.get(1).get(0), new Double(2.0));
            assertEquals(scores.get(1).get(1), new Double(2.0));
            assertEquals(scores.get(1).get(2), new Double(2.0));
            assertEquals(scores.get(1).get(3), new Double(2.0));
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
