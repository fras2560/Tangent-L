package results;
import static org.junit.Assert.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import query.MathQuery;

public class TestResults {
    @Test
    public void test() {
        Path p = Paths.get(System.getProperty("user.dir"),
                           "resources",
                           "results",
                           "simple-results.dat");
        Results r = new Results(p.toFile());
        assertEquals(r.length(), 4251);
    }
    @Test
    public void testQuery(){
        Path p = Paths.get(System.getProperty("user.dir"),
                "resources",
                "results",
                "simple-results.dat");
        Results r = new Results(p.toFile());
        MathQuery q = new MathQuery("NTCIR12-MathIR-1");
        Float rank = r.findResult(q, "0808.1204_1_258");
        assertEquals(rank, new Float(0.0));
        rank = r.findResult(q, "0809.2335_1_151");
        assertEquals(rank, new Float(1.0));
        rank = r.findResult(q, "NOTFINDINGTHIS");
        assertEquals(rank, new Float(-1.0));
    }
}
