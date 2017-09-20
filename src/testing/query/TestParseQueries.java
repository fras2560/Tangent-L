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
package testing.query;
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
import query.ParseQueries;


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
