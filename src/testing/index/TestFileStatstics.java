package testing.index;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import index.FileStatistics;

public class TestFileStatstics {
    private Path folder;
    @Before
    public void setUp() throws Exception {
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "test_file_statistics");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFile1() {
        try {
            Path testFile = Paths.get(this.folder.toString(), "1.html");
            FileStatistics fs = new FileStatistics(new InputStreamReader(Files.newInputStream(testFile)));
            assertEquals(fs.getWordCount() == 3065.0, true);
            assertEquals(fs.averageFormulaSize() == 9.0, true);
            assertEquals(fs.maxFormulaSize() == 17, true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
    }

    @Test
    public void testFile2() {
        try {
            Path testFile = Paths.get(this.folder.toString(), "2.html");
            FileStatistics fs = new FileStatistics(new InputStreamReader(Files.newInputStream(testFile)));
            assertEquals(fs.getWordCount() == 4385.0, true);
            assertEquals(fs.averageFormulaSize() == 2.0, true);
            assertEquals(fs.maxFormulaSize() == 3.0, true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
    }

    
}
