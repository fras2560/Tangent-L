package testing.index;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
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
