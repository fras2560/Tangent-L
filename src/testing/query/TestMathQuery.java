package testing.query;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import query.MathQuery;

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
        String[] terms = {"hello", "hello", "there"};
        ArrayList<String> result = mq.uniqueTerms(terms);
        assertEquals(result.get(0) , "hello");
        assertEquals(result.get(1), "there");
        assertEquals(result.size(), 2);
    }

}
