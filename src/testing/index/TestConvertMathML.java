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
package testing.index;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import index.ConvertMathML;

public class TestConvertMathML {
    Path path;
    Path temp_file;
    @Before
    public void setUp(){
        this.path = Paths.get(System.getProperty("user.dir"),
                              "resources",
                              "test",
                              "test_bm25min_1",
                              "documents",
                              "text_1.txt");
        this.temp_file = null;
    }

    @After
    public void tearDown(){
        if (this.temp_file != null){
            // this.temp_file.toFile().delete();
        }
    }

    @Test
    public void test() {
        System.out.println(this.path.toString());
        ConvertMathML math =  new ConvertMathML(this.path);
        try {
            Path result = math.convert();
            this.temp_file = result;
            Path expect = Paths.get(System.getProperty("user.dir"),
                                    "resources",
                                    "test",
                                    "test_bm25min_1",
                                    "documents",
                                    "text_1_temp.txt");
            
            System.out.println("Result: " + result.toString());
            System.out.println("Expect: " + expect.toString());
            assertEquals(result, expect);
            assertEquals(result.toFile().exists(), true);
            assertEquals(result.toFile().delete(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.toString());
            System.out.println(e.getMessage());
            fail("Threw IOException");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            System.out.println(e.toString());
            System.out.println(e.getMessage());
            fail("Threw InterruptException");
        }
    }

}
