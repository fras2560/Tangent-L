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
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import index.FileStatistics;

public class TestFileStatstics {
    private Path folder;
    @Before
    public void setUp() throws Exception {
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "test_file_statistics");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFile1() {
        try {
            Path testFile = Paths.get(this.folder.toString(), "1.html");
            FileStatistics fs = new FileStatistics(new InputStreamReader(Files.newInputStream(testFile)));
            assertEquals(fs.getWordCount() == 3065.0, true);
            assertEquals(fs.averageFormulaSize() == 9.0, true);
            assertEquals(fs.maxFormulaSize() == 17, true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
    }

    @Test
    public void testFile2() {
        try {
            Path testFile = Paths.get(this.folder.toString(), "2.html");
            FileStatistics fs = new FileStatistics(new InputStreamReader(Files.newInputStream(testFile)));
            assertEquals(fs.getWordCount() == 4385.0, true);
            assertEquals(fs.averageFormulaSize() == 2.0, true);
            assertEquals(fs.maxFormulaSize() == 3.0, true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
    }

    
}
