package testing.utilities;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import utilities.Functions;

public class TestParseFormulas {

    @Test
    public void testParseFormula1(){
        List<String> expect = new ArrayList<String>();
        expect.add("('/*','!0','n')");
        List<String> result = Functions.parseFormulas("#(start)# #('/*','!0','n')# #(end)#");
        assertEquals(result.size(), expect.size());
        assertEquals(result.get(0), expect.get(0));
        // another test with words mixed in
        result = Functions.parseFormulas("hey you there #(start)# #('/*','!0','n')# #(end)#");
        assertEquals(result.size(), expect.size());
        assertEquals(result.get(0), expect.get(0));
        // formula of size two
        result = Functions.parseFormulas("#(start)# #('/*','!0','n')# #('/*','!0','n')# #(end)#");
        expect.set(0, "('/*','!0','n') ('/*','!0','n')");
        assertEquals(result.size(), expect.size());
        assertEquals(result.get(0), expect.get(0));
    }
    
    @Test
    public void testParseFormula2(){
        List<String> expect = new ArrayList<String>();
        expect.add("('/*','!0','n')");
        expect.add("('/*','!0','n')");
        List<String> result;
        result = Functions.parseFormulas("#(start)# #('/*','!0','n')# #(end)# #(start)# #('/*','!0','n')# #(end)#");
        assertEquals(result.size(), expect.size());
        assertEquals(result.get(0), expect.get(0));
        assertEquals(result.get(1), expect.get(1));
        // mix in some words
        result = Functions.parseFormulas("hello #(start)# #('/*','!0','n')# #(end)# #(start)# #('/*','!0','n')# #(end)# breka");
        assertEquals(result.size(), expect.size());
        assertEquals(result.get(0), expect.get(0));
        assertEquals(result.get(1), expect.get(1));
    }
}
