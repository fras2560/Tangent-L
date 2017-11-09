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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import query.MathQuery;
import query.TermCountPair;

public class TestMathQuery {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUniqueTerms() {
        MathQuery mq = new MathQuery("test");
        ArrayList<String> terms = new ArrayList<String>();
        terms.add("hello");
        terms.add("hello");
        terms.add("there");
        List<TermCountPair> result = mq.uniqueTerms(terms);
        assertEquals(result.get(0) , "hello");
        assertEquals(result.get(0).getCount() == 2f, true);
        assertEquals(result.get(1), "there");
        assertEquals(result.get(1).getCount() == 1f, true);
        assertEquals(result.size(), 2);
    }

}
