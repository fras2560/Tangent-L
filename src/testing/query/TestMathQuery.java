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
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import index.ConvertConfig;
import query.MathQuery;
import query.TermCountPair;
import utilities.Constants;

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

    @Test
    public void testBuildQuery(){
        MathQuery mq = new MathQuery("test");
        ConvertConfig config =  new ConvertConfig();
        CollectionStatistics stats = new CollectionStatistics(Constants.FIELD, 0l, 0l, 0l, 0l);
        mq.addTerm(" #('m!()1x1','n!1','n')# #('v!t','*','b')#", Constants.FIELD);
        try {
            Query q = mq.buildQuery(Constants.FIELD, new BooleanQuery.Builder(), false, config, stats);
            assertEquals(q.toString(), "custom(contents:('m!()1x1','n!1','n') (contents:('v!t','*','b'))^"
                                       + MathQuery.WILDCARD_BOOST
                                       + ")");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
        config.setBooleanAttribute(ConvertConfig.BOOST_QUERIES, true);
        try {
            Query q = mq.buildQuery(Constants.FIELD, new BooleanQuery.Builder(), true, config, stats);
            assertEquals(q.toString(), "custom((contents:('m!()1x1','n!1','n'))^1.0 (contents:('v!t','*','b'))^1.0)");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
        config.setBooleanAttribute(ConvertConfig.BOOST_LOCATION, true);
        try {
            Query q = mq.buildQuery(Constants.FIELD, new BooleanQuery.Builder(), true, config, stats);
            assertEquals(q.toString(),
                         "custom(custom((contents:('m!()1x1','n!1','n'))^1.0) custom((contents:('v!t','*','b'))^1.0))");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
    }

    @Test
    public void testBuildEmptyQuery(){
        MathQuery mq =  new MathQuery("test");
        ConvertConfig config =  new ConvertConfig();
        CollectionStatistics stats = new CollectionStatistics(Constants.FIELD, 0l, 0l, 0l, 0l);
        try {
            Query q = mq.buildQuery(Constants.FIELD, new BooleanQuery.Builder(), false, config, stats);
            assertEquals(q.toString(), "custom(contents:)");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
