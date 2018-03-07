package testing.utilities;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utilities.Functions;

public class TestParseDocumentName {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSpecial(){
        String path = "/home/arkane/arxiv/unzipped/xhtml5/3/gr-qc0502086/gr-qc0502086_1_10.xhtml";
        String document = Functions.parseDocumentName(path);
        assertEquals(document, "gr");
    }
    @Test
    public void test() {
        String path = "/home/d6fraser/Documents/Research/Datasets/wikipedia_formula/00004-1.html";
        String document = Functions.parseDocumentName(path);
        assertEquals(document, "00004");
    }

    @Test
    public void test2() {
        String path = "00004-1.html";
        String document = Functions.parseDocumentName(path);
        assertEquals(document, "00004");
    }

    @Test
    public void test3() {
        String path = "00004.html";
        String document = Functions.parseDocumentName(path);
        assertEquals(document, "00004");
    }
}
