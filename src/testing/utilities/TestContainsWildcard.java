package testing.utilities;

import static org.junit.Assert.*;

import org.junit.Test;

import utilities.Functions;

public class TestContainsWildcard {

    @Test
    public void test() {
        assertEquals(Functions.containsWildcard("'*','!0','n'"), true);
        assertEquals(Functions.containsWildcard("v!x','!0','n'"), false);
    }

    @Test
    public void testHard(){
        assertEquals(Functions.containsWildcard("'/*','!0','n'"), false);
    }

    @Test
    public void testExtremelyHard(){
        assertEquals(Functions.containsWildcard("'*','/*','n'"), true);
    }
}
