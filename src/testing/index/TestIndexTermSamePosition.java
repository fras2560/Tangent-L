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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import index.ConvertConfig;
import index.IndexFiles;
import testing.BaseTest;
import utilities.Constants;

public class TestIndexTermSamePosition extends BaseTest{
    private Path folder;
    private Path index;
    private Path documents;
    
    @Before
    public void setUp() throws Exception{
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "test_index_same_position");
        this.documents = Paths.get(this.folder.toString(), "documents");
        this.index = Paths.get(this.folder.toString(), "index");
        File dir = this.index.toFile();
        // attempt to create the directory here
        boolean successful = dir.mkdir();
        if (!successful){
          // creating the directory failed
          System.out.println("failed trying to create the directory");
          throw new Exception("Failed to create directory");
        }
    }

    @After
    public void tearDown(){
        // remove the index created
        this.deleteDirectory(this.index);
    }

    @Test
    public void testIndex1() {
        IndexFiles indexer = new IndexFiles();
        try {
            ConvertConfig config = new ConvertConfig();
            config.setBooleanAttribute(ConvertConfig.BAGS_OF_WORDS, true);
            indexer.indexDirectory(this.index, this.documents, true, config);
            // make sure some files were created
            String[]entries = this.index.toFile().list();
            assertEquals(entries.length, 6);
            // try opening the index if no errors are raised should be fine
            IndexReader reader = DirectoryReader.open(FSDirectory.open(this.index));
            long result = reader.totalTermFreq(new Term (Constants.FIELD, "('m!()1x1','=','n')"));
            System.out.println(result);
            assertEquals(result == 2l, true);
            System.out.println(result);
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
    }

}
