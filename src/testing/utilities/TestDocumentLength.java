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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import index.MathAnalyzer;
import utilities.Constants;
import utilities.Functions;

public class TestDocumentLength {

    @Test
    public void test() {
        Path file  = Paths.get(System.getProperty("user.dir"),
                               "resources",
                               "test",
                               "test_bm25min_1",
                               "documents",
                               "text_1.txt");
        try (InputStream stream = Files.newInputStream(file)){
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            assertEquals(Functions.documentLength(new MathAnalyzer(), Constants.FIELD, reader), 4);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            fail("Unable to read file");
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        Path file  = Paths.get(System.getProperty("user.dir"),
                               "resources",
                               "test",
                               "test_bm25min_1",
                               "documents",
                               "text_2.txt");
        try (InputStream stream = Files.newInputStream(file)){
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            assertEquals(Functions.documentLength(new MathAnalyzer(), Constants.FIELD, reader), 4);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            fail("Unable to read file");
            e.printStackTrace();
        }
    }
}
