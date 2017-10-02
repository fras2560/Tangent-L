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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
import index.IndexFiles;
import search.Search;
import search.Search.SearchConfigException;
import testing.BaseTest;

public class TestSearchCompatible extends BaseTest{
    private Path folder;
    private Path index;
    private Path documents;
    private Search searcher;
    private ConvertConfig config;

    @Before
    public void setUp() throws Exception{
        // uncomment for debugging
        // this.debugLogger();
        this.folder = Paths.get(System.getProperty("user.dir"), "resources", "test", "index_test_1");
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
        // create the index
        IndexFiles indexer = new IndexFiles();
        this.config = new ConvertConfig();
        this.config.flipBit(ConvertConfig.LOCATION);
        indexer.indexDirectory(this.index, this.documents, true, this.config);
        // init the searching object
        this.searcher = new Search(this.index, this.config);
    }

    @After
    public void tearDown(){
        // remove the index created
        try {
            this.searcher.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.deleteDirectory(this.index);
    }

    @Test
    public void testSearchCompatibility(){
        try{
            this.searcher = new Search(this.index, new ConvertConfig());
            assertEquals("Should raise exception",  true, false);
        }catch (SearchConfigException e){
            // should be raised
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertEquals("IO Exception raised",  true, false);
        } catch (ConvertConfigException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertEquals("ConvertConfig not found raised",  true, false);
        }
        // now convert config has location but has something backwards compatible
        try{
            ConvertConfig cf = new ConvertConfig();
            cf.flipBit(ConvertConfig.LOCATION);
            cf.flipBit(ConvertConfig.COMPOUND);
            this.searcher = new Search(this.index, cf);
            assertEquals("Should raise exception",  true, false);
            this.searcher.getConfig();
        }catch (SearchConfigException e){
            // should be raised
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertEquals("IO Exception raised",  true, false);
        } catch (ConvertConfigException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertEquals("ConvertConfig not found raised",  true, false);
        }
    }
}
