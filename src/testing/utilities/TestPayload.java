package testing.utilities;

import static org.junit.Assert.*;

import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import utilities.Payload;
import utilities.Payload.PayloadException;

public class TestPayload {

    @Test
    public void test() {
        Payload pl;
        try {
            pl = new Payload(new BytesRef("nna:5".getBytes()));
            assertEquals(pl.getFormulaSize(), 5);
            assertEquals(pl.getLocation(), "nna");
        } catch (PayloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Error raised");
        }
        
    }

}
