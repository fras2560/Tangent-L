package testing.utilities;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import index.MathAnalyzer;
import query.TermCountPair;
import utilities.Payload;
import utilities.Payload.PayloadException;
import utilities.Constants;
import utilities.Functions;

public class TestPayloadAnalyzer {

    @Test
    public void test1() {
        String queryText = "hello #('a','b','n')||nn:5#";
        Map<String, TermCountPair> expect = new HashMap<String, TermCountPair>();
        expect.put("hello", new TermCountPair("hello"));
        TermCountPair tcp = new TermCountPair("('a','b','n')");
        tcp.addPayload(new Payload(5, "nn"));
        expect.put("('a','b','n')", tcp);
        try {
            List<TermCountPair> terms = Functions.getTermCountPair(new MathAnalyzer(), Constants.FIELD, queryText);
            for(TermCountPair term : terms){
                // check the term is the same
                assertEquals(term.getTerm(), expect.get(term.getTerm()).getTerm());
                // check the count is the same
                assertEquals(term.getCount(), expect.get(term.getTerm()).getCount(), 0.001d);
                // now check payload is the same
                assertArrayEquals(term.payloadFormulaSizes(), expect.get(term.getTerm()).payloadFormulaSizes());
                assertTrue(term.payloadLocations().equals(expect.get(term.getTerm()).payloadLocations()));
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            fail("IO Exception raised");
        }
    }

    @Test
    public void test2(){
        String queryText = "#('a','b','n')||nn:5# #('a','b','n')||nn:5#";
        Map<String, TermCountPair> expect = new HashMap<String, TermCountPair>();
        TermCountPair tcp = new TermCountPair("('a','b','n')");
        tcp.increment();
        tcp.addPayload(new Payload(5, "nn"));
        tcp.addPayload(new Payload(5, "nn"));
        expect.put("('a','b','n')", tcp);
        try {
            List<TermCountPair> terms = Functions.getTermCountPair(new MathAnalyzer(), Constants.FIELD, queryText);
            for(TermCountPair term : terms){
                // check the term is the same
                assertEquals(term.getTerm(), expect.get(term.getTerm()).getTerm());
                // check the count is the same
                System.out.println(term.getCount());
                assertEquals(term.getCount(), expect.get(term.getTerm()).getCount(), 0.001d);
                // now check payload is the same
                assertArrayEquals(term.payloadFormulaSizes(), expect.get(term.getTerm()).payloadFormulaSizes());
                assertTrue(term.payloadLocations().equals(expect.get(term.getTerm()).payloadLocations()));
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            fail("IO Exception raised");
        }
    }
}
