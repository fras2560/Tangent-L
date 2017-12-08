package testing.utilities;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utilities.Functions;

public class TestFormulaCount {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        assertEquals(Functions.countTuples("#(start)# #('/*','!0','n')# #(end)#"), 1);
        assertEquals(Functions.countTuples("hey you there #(start)# #('/*','!0','n')# #(end)#"), 1);
        assertEquals(Functions.countTuples("#(start)# #('/*','!0','n')# #('/*','!0','n')# #(end)#"), 2);
        assertEquals(Functions.countTuples("(start) ('/*','!0','n') (end)"), 1);
        assertEquals(Functions.countTuples("(start) ('/*','!0','n') ('/*','!0','n') (end)"), 2);
    }
}
