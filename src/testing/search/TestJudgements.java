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
package testing.search;
import static org.junit.Assert.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Test;
import query.MathQuery;
import search.Judgments;


public class TestJudgements {
    @Test
    public void test() {
        Path p = Paths.get(System.getProperty("user.dir"),
                           "resources",
                           "test",
                           "index_test_1",
                           "judgements",
                           "judgements.txt");
        Judgments r = new Judgments(p.toFile());
        assertEquals(r.length(), 3);
    }

    @Test
    public void testGetResult(){
        Path p = Paths.get(System.getProperty("user.dir"),
                           "resources",
                           "test",
                           "index_test_1" ,
                           "judgements",
                           "judgements.txt");
        Judgments r = new Judgments(p.toFile());
        MathQuery query = new MathQuery("NTCIR12-MathIR-1");
        assertEquals(r.findResult(query, "1301.6848_1_17"), new Float(3.0));
        assertEquals(r.findResult(query, "math-ph0607065_1_57"), new Float(0.0));
    }

    @Test
    public void testQuery(){
        Path p = Paths.get(System.getProperty("user.dir"),
                           "resources",
                           "test",
                           "index_test_1",
                           "judgements",
                           "NTCIR12-ArXiv-Math.dat");
        Judgments r = new Judgments(p.toFile());
        MathQuery q = new MathQuery("NTCIR12-MathIR-1");
        Float rank = r.findResult(q, "0808.1204_1_258");
        assertEquals(rank, new Float(0.0));
        rank = r.findResult(q, "0809.2335_1_151");
        assertEquals(rank, new Float(1.0));
        rank = r.findResult(q, "NOTFINDINGTHIS");
        assertEquals(rank, new Float(-1.0));
    }

    @Test
    public void testRecallCheck(){
        Path p = Paths.get(System.getProperty("user.dir"),
                           "resources",
                           "test",
                           "index_test_1",
                           "judgements",
                            "NTCIR12-ArXiv-Math.dat");
        Judgments r = new Judgments(p.toFile());
        MathQuery q = new MathQuery("NTCIR12-MathIR-1");
        ArrayList<String> results = new ArrayList<String>(); 
        int[] result = r.recallResult(q, results);
        assertEquals(result[0], 19);
        assertEquals(result[1], 0);
        assertEquals(result[2], 41);
        assertEquals(result[3], 0);
        results.add("0809.2335_1_151");
        results.add("0901.4232_1_15");
        result = r.recallResult(q, results);
        assertEquals(result[0], 19);
        assertEquals(result[1], 1);
        assertEquals(result[2], 41);
        assertEquals(result[3], 2);
    }
}
