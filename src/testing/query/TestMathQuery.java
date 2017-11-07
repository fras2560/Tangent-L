package testing.query;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import query.MathQuery;
import query.TermCountPair;

public class TestMathQuery {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUniqueTerms() {
        MathQuery mq = new MathQuery("test");
        ArrayList<String> terms = new ArrayList<String>();
        terms.add("hello");
        terms.add("hello");
        terms.add("there");
        ArrayList<TermCountPair> result = mq.uniqueTerms(terms);
        assertEquals(result.get(0) , "hello");
        assertEquals(result.get(0).getCount() == 2f, true);
        assertEquals(result.get(1), "there");
        assertEquals(result.get(1).getCount() == 1f, true);
        assertEquals(result.size(), 2);
    }

}
