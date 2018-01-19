package testing.misc;

import static org.junit.Assert.*;

import org.apache.lucene.util.BytesRef;
import org.junit.Test;

public class TestingPayloadEncoding {

    @Test
    public void test() {
        BytesRef by = new BytesRef("nnn:3");
        assertEquals(by.toString(), "[6e 6e 6e 3a 33]");
    }

}
