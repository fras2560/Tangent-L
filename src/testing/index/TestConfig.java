/*
 * Copyright 2017 Dallas Fraser
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testing.index;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
import testing.BaseTest;


public class TestConfig extends BaseTest {
    private Path tempDirectory;

    @Before
    public void setUp() throws Exception{
        this.tempDirectory = Paths.get(System.getProperty("user.dir"), "resources", "test", "temp");
        File dir = this.tempDirectory.toFile();
        // attempt to create the directory here
        boolean successful = dir.mkdir();
        if (!successful){
          // creating the directory failed
          System.out.println("failed trying to create the directory");
          throw new Exception("Failed to create directory");
        }
    }

    @After
    public void tearDown(){
        // remove the index created
        this.deleteDirectory(this.tempDirectory);
    }

    @Test
    public void testSaveLoad(){
        ConvertConfig config = new ConvertConfig();
        config.setWindowSize(3);
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
        // save it
        try {
            config.saveConfig(this.tempDirectory);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertEquals("unable to save config", false, true);
        }
        // make sure it is not equal to the above changes
        ConvertConfig loaded_config = new ConvertConfig();
        assertEquals(loaded_config.getAttribute(ConvertConfig.SYNONYMS), false);
        assertEquals(loaded_config.getWindowsSize(), 1);
        // load it and make sure attribute changed
        try {
            loaded_config.loadConfig(this.tempDirectory);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertEquals("unable to load config", false, true);
        } catch (ConvertConfigException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertEquals("ConvertConfig not found raised",  true, false);
        }
        assertNotEquals(loaded_config.getAttribute(ConvertConfig.SYNONYMS), false);
        assertNotEquals(loaded_config.getWindowsSize(), 1);
        assertEquals(loaded_config.getAttribute(ConvertConfig.SYNONYMS), true);
        assertEquals(loaded_config.getWindowsSize(), 3);
    }

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
        assertEquals(config.toString(), "base");
        config.flipBit(ConvertConfig.COMPOUND);
        assertEquals(config.toString(), " -compound_symbols".toUpperCase());
        config.flipBit(ConvertConfig.COMPOUND);
        config.flipBit(ConvertConfig.EDGE);
        assertEquals(config.toString(), " -edge_pairs".toUpperCase());
        config.flipBit(ConvertConfig.EDGE);
        config.flipBit(ConvertConfig.TERMINAL);
        assertEquals(config.toString(), " -terminal_symbols".toUpperCase());
        config.flipBit(ConvertConfig.EOL);
        config.flipBit(ConvertConfig.TERMINAL);
        assertEquals(config.toString(), " -eol".toUpperCase());
        config.flipBit(ConvertConfig.EOL);
        config.flipBit(ConvertConfig.SHORTENED);
        assertEquals(config.toString(), " -shortened".toUpperCase());
        config.flipBit(ConvertConfig.SHORTENED);
        config.flipBit(ConvertConfig.UNBOUNDED);
        assertEquals(config.toString(), " -unbounded".toUpperCase());
        config.flipBit(ConvertConfig.UNBOUNDED);
        config.flipBit(ConvertConfig.LOCATION);
        assertEquals(config.toString(), " -location".toUpperCase());
        config.flipBit(ConvertConfig.LOCATION);
        config.setWindowSize(2);
        assertEquals(config.toString(), " -window_size:2".toUpperCase());
        config.setWindowSize(1);
        config.flipBit(ConvertConfig.SYMBOLS);
        assertEquals(config.toString(), " -!symbol_pairs".toUpperCase());
    }

    @Test
    public void testCompatible(){
        ConvertConfig config = new ConvertConfig();
        ConvertConfig compare = new ConvertConfig();
        // default they are both compatible
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), true);
        // check shortened not backwards compatible
        config.setBooleanAttribute(ConvertConfig.SHORTENED, false);
        assertEquals(config.compatible(compare), false);
        assertEquals(compare.compatible(config), false);
        config.setBooleanAttribute(ConvertConfig.SHORTENED, true);
        // check location not backwards compatible
        config.setBooleanAttribute(ConvertConfig.LOCATION, true);
        assertEquals(config.compatible(compare), false);
        assertEquals(compare.compatible(config), false);
        config.setBooleanAttribute(ConvertConfig.LOCATION, false);
        // window size is backwards compatible
        config.setWindowSize(3);
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), false);
        config.setWindowSize(1);
        // eol is backwards compatible
        config.setBooleanAttribute(ConvertConfig.EOL, true);
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), false);
        config.setBooleanAttribute(ConvertConfig.EOL, false);
        // compound is backwards compatible
        config.setBooleanAttribute(ConvertConfig.COMPOUND, true);
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), false);
        config.setBooleanAttribute(ConvertConfig.COMPOUND, false);
        // terminal is backwards compatible
        config.setBooleanAttribute(ConvertConfig.TERMINAL, true);
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), false);
        config.setBooleanAttribute(ConvertConfig.TERMINAL, false);
        // edge is backwards compatible
        config.setBooleanAttribute(ConvertConfig.EDGE, true);
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), false);
        config.setBooleanAttribute(ConvertConfig.EDGE, false);
        // synonyms is backwards compatible
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), false);
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, false);
        // unbounded is backwards compatible
        config.setBooleanAttribute(ConvertConfig.UNBOUNDED, true);
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), false);
        config.setBooleanAttribute(ConvertConfig.UNBOUNDED, false);
        // symbol pairs is backwards compatible
        config.setBooleanAttribute(ConvertConfig.SYMBOLS, true);
        assertEquals(config.compatible(compare), true);
        assertEquals(compare.compatible(config), true);
        compare.setBooleanAttribute(ConvertConfig.SYMBOLS, false);
        assertEquals(compare.compatible(config), false);
        
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
