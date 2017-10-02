package testing.utilities;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import utilities.Functions;

public class TestParseTitle {

    @Test
    public void testParseTitle() {
        String test = "/home/d6fraser/git/UWResearch/resources/test/index_test_1/documents/1301.6848_1_17.xhtml";
        String expect = "1301.6848_1_17";
        assertEquals(Functions.parseTitle(test), expect);
        test = "/home/d6fraser/git/UWResearch/resources/test/index_test_1/documents/math-ph0607065_1_57.xhtml";
        expect = "math-ph0607065_1_57";
        assertEquals(Functions.parseTitle(test), expect);
    }

    @Test
    public void testParseTitleWindows() {
        String test = "C:\\Users\\Dallas\\git\\UWResearch\\resources\\test\\index_test_1\\documents\\1301.6848_1_17.xhtml";
        String expect = "1301.6848_1_17";
        assertEquals(Functions.parseTitle(test), expect);
    }

    @Test
    public void testCreateTempName(){
        Path test = Paths.get("/home/d6fraser/git/UWResearch/resources/test/index_test_1/documents/1301.6848_1_17.xhtml");
        Path expect = Paths.get("/home/d6fraser/git/UWResearch/resources/test/index_test_1/documents/1301.6848_1_17_temp.xhtml");
        Path result = Functions.createtempFile(test);
        assertEquals(expect.toString(), result.toString());
    }
}
