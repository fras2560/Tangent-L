package naiveMathIndexer.index;
import static org.junit.Assert.*;
import org.junit.Test;

public class TestConfig {
    @Test 
    public void testCopy(){
        ConvertConfig config = new ConvertConfig();
        config.setWindowSize(2);
        config.flipBit(ConvertConfig.COMPOUND);
        ConvertConfig result1 = config.copy();
        assertEquals(result1, config);
        config.flipBit(ConvertConfig.TERMINAL);
        ConvertConfig result2 = config.copy();
        assertEquals(result2, config);
        assertNotEquals(result1, config);
    }

    @Test
    public void testToString() {
        ConvertConfig config = new ConvertConfig();
        assertEquals(config.toString(), "");
        config.flipBit(ConvertConfig.COMPOUND);
        assertEquals(config.toString(), " -compound_symbols");
        config.flipBit(ConvertConfig.COMPOUND);
        config.flipBit(ConvertConfig.EDGE);
        assertEquals(config.toString(), " -edge_pairs");
        config.flipBit(ConvertConfig.EDGE);
        config.flipBit(ConvertConfig.TERMINAL);
        assertEquals(config.toString(), " -terminal_symbols");
        config.flipBit(ConvertConfig.EOL);
        config.flipBit(ConvertConfig.TERMINAL);
        assertEquals(config.toString(), " -eol");
        config.flipBit(ConvertConfig.EOL);
        config.flipBit(ConvertConfig.SHORTENED);
        assertEquals(config.toString(), " -shortened");
        config.flipBit(ConvertConfig.SHORTENED);
        config.flipBit(ConvertConfig.UNBOUNDED);
        assertEquals(config.toString(), " -unbounded");
        config.flipBit(ConvertConfig.UNBOUNDED);
        config.flipBit(ConvertConfig.LOCATION);
        assertEquals(config.toString(), " -location");
        config.flipBit(ConvertConfig.LOCATION);
        config.setWindowSize(2);
        assertEquals(config.toString(), " -window_size 2");
        
    }

    @Test
    public void testToCommands() {
        ConvertConfig config = new ConvertConfig();
        String[] expect = {};
        String[] expect1 = {"-compound_symbols"};
        String[] expect2 = {"-edge_pairs"};
        String[] expect3 = {"-terminal_symbols"};
        String[] expect4 = {"-eol"};
        String[] expect5 = {"-shortened"};
        String[] expect6 = {"-unbounded"};
        String[] expect7 = {"-location"};
        String[] expect8 = {"-window_size", "2"};
        assertArrayEquals(config.toCommands(), expect);
        config.flipBit(ConvertConfig.COMPOUND);
        assertArrayEquals(config.toCommands(), expect1);
        config.flipBit(ConvertConfig.COMPOUND);
        config.flipBit(ConvertConfig.EDGE);
        assertArrayEquals(config.toCommands(), expect2);
        config.flipBit(ConvertConfig.EDGE);
        config.flipBit(ConvertConfig.TERMINAL);
        assertArrayEquals(config.toCommands(), expect3);
        config.flipBit(ConvertConfig.EOL);
        config.flipBit(ConvertConfig.TERMINAL);
        assertArrayEquals(config.toCommands(), expect4);
        config.flipBit(ConvertConfig.EOL);
        config.flipBit(ConvertConfig.SHORTENED);
        assertArrayEquals(config.toCommands(), expect5);
        config.flipBit(ConvertConfig.SHORTENED);
        config.flipBit(ConvertConfig.UNBOUNDED);
        assertArrayEquals(config.toCommands(), expect6);
        config.flipBit(ConvertConfig.UNBOUNDED);
        config.flipBit(ConvertConfig.LOCATION);
        assertArrayEquals(config.toCommands(), expect7);
        config.flipBit(ConvertConfig.LOCATION);
        config.setWindowSize(2);
        assertArrayEquals(config.toCommands(), expect8);
    }
}
