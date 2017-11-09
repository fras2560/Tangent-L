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
package testing.utilities;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import index.MathAnalyzer;
import utilities.Functions;

public class TestAnalyzeTokens {

    @Test
    public void test() {
        List<String> result = Functions.analyzeTokens(new MathAnalyzer(), "contents",
                                                  "arthmetic #('m!()1x2','v!x','w')#"+
                                                  " #('v!x','comma','n')# #('v!x','v!σ','e')#");
        assertEquals(result.size(), 4);
        assertEquals(result.get(0), "arthmet");
        assertEquals(result.get(1), "('m!()1x2','v!x','w')");
        assertEquals(result.get(2), "('v!x','comma','n')");
        assertEquals(result.get(3), "('v!x','v!σ','e')");
    }

    @Test
    public void test2() {
        List<String> result = Functions.analyzeTokens(new MathAnalyzer(), "contents",
                                                  "#('n!1','+','n')# #('+','*','n')# " + 
                                                  "Progression Geometric"); 
        assertEquals(result.size(), 4);
        assertEquals(result.get(0), "('n!1','+','n')");
        assertEquals(result.get(1), "('+','*','n')");
        assertEquals(result.get(2), "progress");
        assertEquals(result.get(3), "geometr");
    }

    @Test
    public void test3() {
        String t = "#(start)# #('v!x','+','n')# #('+','*','n')# #(end)# Mean Arithmetic";
        List<String> result = Functions.analyzeTokens(new MathAnalyzer(), "contents", t); 
        assertEquals(result.size(), 4);
        assertEquals(result.get(0), "('v!x','+','n')");
        assertEquals(result.get(1), "('+','*','n')");
        assertEquals(result.get(2), "mean");
        assertEquals(result.get(3), "arithmet");
    }
}
