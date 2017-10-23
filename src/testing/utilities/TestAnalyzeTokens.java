package testing.utilities;

import static org.junit.Assert.*;
import org.junit.Test;
import index.MathAnalyzer;
import utilities.Functions;

public class TestAnalyzeTokens {

    @Test
    public void test() {
        String[] result = Functions.analyzeTokens(new MathAnalyzer(), "contents",
                                                  "arthmetic #('m!()1x2','v!x','w')#"+
                                                  " #('v!x','comma','n')# #('v!x','v!σ','e')#");
        assertEquals(result.length, 4);
        assertEquals(result[0], "arthmet");
        assertEquals(result[1], "('m!()1x2','v!x','w')");
        assertEquals(result[2], "('v!x','comma','n')");
        assertEquals(result[3], "('v!x','v!σ','e')");
    }

    @Test
    public void test2() {
        String[] result = Functions.analyzeTokens(new MathAnalyzer(), "contents",
                                                  "#('n!1','+','n')# #('+','*','n')# " + 
                                                  "Progression Geometric"); 
        assertEquals(result.length, 4);
        assertEquals(result[0], "('n!1','+','n')");
        assertEquals(result[1], "('+','*','n')");
        assertEquals(result[2], "progress");
        assertEquals(result[3], "geometr");
    }

    @Test
    public void test3() {
        String t = "#(start)# #('v!x','+','n')# #('+','*','n')# #(end)# Mean Arithmetic";
        String[] result = Functions.analyzeTokens(new MathAnalyzer(), "contents", t); 
        assertEquals(result.length, 4);
        assertEquals(result[0], "('v!x','+','n')");
        assertEquals(result[1], "('+','*','n')");
        assertEquals(result[2], "mean");
        assertEquals(result[3], "arithmet");
    }
}
