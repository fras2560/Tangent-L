package testing.utilities;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import index.MathAnalyzer;
import utilities.Functions;

public class TestAnalyzeTokens {

    @Test
    public void test() {
        List<String> result = Functions.analyzeTokens(new MathAnalyzer(), "contents",
                                                  "arthmetic #('m!()1x2','v!x','w')#"+
                                                  " #('v!x','comma','n')# #('v!x','v!σ','e')#");
        assertEquals(result.size(), 4);
        assertEquals(result.get(0), "arthmet");
        assertEquals(result.get(1), "('m!()1x2','v!x','w')");
        assertEquals(result.get(2), "('v!x','comma','n')");
        assertEquals(result.get(3), "('v!x','v!σ','e')");
    }

    @Test
    public void test2() {
        List<String> result = Functions.analyzeTokens(new MathAnalyzer(), "contents",
                                                  "#('n!1','+','n')# #('+','*','n')# " + 
                                                  "Progression Geometric"); 
        assertEquals(result.size(), 4);
        assertEquals(result.get(0), "('n!1','+','n')");
        assertEquals(result.get(1), "('+','*','n')");
        assertEquals(result.get(2), "progress");
        assertEquals(result.get(3), "geometr");
    }

    @Test
    public void test3() {
        String t = "#(start)# #('v!x','+','n')# #('+','*','n')# #(end)# Mean Arithmetic";
        List<String> result = Functions.analyzeTokens(new MathAnalyzer(), "contents", t); 
        assertEquals(result.size(), 4);
        assertEquals(result.get(0), "('v!x','+','n')");
        assertEquals(result.get(1), "('+','*','n')");
        assertEquals(result.get(2), "mean");
        assertEquals(result.get(3), "arithmet");
    }
}
