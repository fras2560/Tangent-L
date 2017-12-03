package testing.misc;

import static org.junit.Assert.*;

import org.apache.lucene.analysis.payloads.IdentityEncoder;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

public class TestingPayloadEncoding {

    @Test
    public void test() {
        IdentityEncoder id = new IdentityEncoder();
        BytesRef by = new BytesRef("nnn:3");
        System.out.println(by);
        System.out.println();
    }

}
