package testing.utilities;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utilities.CommandLineArguments;
import utilities.CommandLineException;

public class TestCommandLineArguments {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorRequired(){
        String[] args = {"-" + CommandLineArguments.DOCUMENTSDIRECTORY , "test"};
        List<String> required = new ArrayList<String>();
        required.add(CommandLineArguments.DOCUMENTSDIRECTORY);
        required.add(CommandLineArguments.INDEXDIRECTORY);
        
        try {
            CommandLineArguments cla = new CommandLineArguments(args, required);
            assertEquals(cla.getPath(CommandLineArguments.DOCUMENTSDIRECTORY), Paths.get("test"));
            assertEquals(cla.getPath(CommandLineArguments.INDEXDIRECTORY), Paths.get("/home/d6fraser/git/UWResearch/resources/index/wikipedia_formula"));
            
        } catch (CommandLineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Command Line Exception");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Unable to load config");
        }
    }

    @Test
    public void testLoadConfig(){
        Map<String, Path> result = null;
        try {
            result = CommandLineArguments.loadConfig();
            // just make sure equal to something
            String expect = "{judgements=/home/d6fraser/git/UWResearch/resources/results/NTCIR11-wikipedia-formula-11.txt, resultsFile=/home/d6fraser/git/UWResearch/resources/output, queriesFile=/home/d6fraser/git/UWResearch/resources/query/NTCIR12-MathWiki-formula.xml, logFile=/home/d6fraser/git/UWResearch/resources/logs, indexDirectory=/home/d6fraser/git/UWResearch/resources/index/wikipedia_formula, queriesOutputFile=/home/d6fraser/git/UWResearch/resources/output, documentsDirectory=/home/d6fraser/Documents/Research/Datasets/NTCIR11_wikipedia_formula}";
            assertEquals(result.toString(), expect);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Unable to load config");
        }
    }

    @Test
    public void testConstructor(){
        String[] args = {"-" + CommandLineArguments.DOCUMENTSDIRECTORY , "test"};
        try {
            CommandLineArguments cla = new CommandLineArguments(args);
            assertEquals(cla.getPath(CommandLineArguments.DOCUMENTSDIRECTORY), Paths.get("test"));
            assertEquals(cla.getPath(CommandLineArguments.INDEXDIRECTORY), null);
        } catch (CommandLineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Command Line Exception");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Unable to load config");
        }
    }
}
