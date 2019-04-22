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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Test;

import index.ConvertConfig;
import index.ConvertMathMl;
import utilities.Functions;

public class TestParseString {
    private Path temp_file = null;
    @Test
    public void testParseStringOne() {
        Path file  = Paths.get(System.getProperty("user.dir"),
                "resources",
                "test",
                "test_bm25min_1",
                "documents",
                "text_1.txt");
        String text;
        try {
            text = Functions.parseString(file);
            System.out.println(text);
            assertEquals(text, "dallas lies lies fraser");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("error raised");
        }
        
    }

    @After
    public void tearDown(){
        // remove the index created
        if (this.temp_file != null){
            this.temp_file.toFile().delete();
        }
    }
    
    @Test
    public void testParseStringTwo() {
        Path file  = Paths.get(System.getProperty("user.dir"),
                               "resources",
                               "test",
                               "test_index_same_position",
                               "documents",
                               "27419.html");
       try {
           ConvertConfig config = new ConvertConfig();
           config.optimalConfig();
           this.temp_file = new ConvertMathMl(file).convertPath(config);
           String text = Functions.parseString(this.temp_file);
           System.out.println(text);
           System.out.println(text.length());
           assertEquals(text.length(), 8396);
       } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
           fail("Error raised");
       } catch (InterruptedException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
           fail("Error raised");
       }
    }
}
