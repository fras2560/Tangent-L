package naiveMathIndexer.query;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Test;
import org.xml.sax.SAXException;
import query.MathQuery;

public class TestParseQueries {
    @Test
    public void test() {
        Path p = Paths.get(System.getProperty("user.dir"),
                           "resources",
                           "query",
                           "simple-queries.xml");
        try {
            ParseQueries pq = new ParseQueries(p.toFile());
            ArrayList<MathQuery> queries = pq.getQueries();
            System.out.println("\nQueries:");
            for (MathQuery q : queries){
                System.out.println("----------------------------------");
                System.out.println(q);
            }
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Threw InterruptException | IOException");
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Threw XPathExpressionException");
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Threw ParserConfigurationException");
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Threw SAXException");
        }
    }
}
