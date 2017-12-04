package testing.index;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import index.MathAnalyzer;
import utilities.Constants;
import utilities.Functions;

public class TestMathAnalyzer {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        List<String> tokens = Functions.analyzeTokens(new MathAnalyzer(), Constants.FIELD, "Hello #()#");
        assertEquals(tokens.get(0), "hello");
        assertEquals(tokens.get(1), "()");
        assertEquals(tokens.size(), 2);
    }

    @Test
    public void testMath() {
        List<String> tokens = Functions.analyzeTokens(new MathAnalyzer(), Constants.MATHFIELD, "Hello #()#");
        assertEquals(tokens.get(0), "()");
        assertEquals(tokens.size(), 1);
    }

    @Test
    public void testText(){
        List<String> tokens = Functions.analyzeTokens(new MathAnalyzer(), Constants.TEXTFIELD, "Hello #()#");
        assertEquals(tokens.get(0), "hello");
        assertEquals(tokens.size(), 1);
    }

}
