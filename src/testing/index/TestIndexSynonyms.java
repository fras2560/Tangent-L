package testing.index;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import index.ConvertConfig;
import index.IndexFiles;
import testing.BaseTest;
import utilities.ProjectLogger;

public class TestIndexSynonyms extends BaseTest{
    private Path folder;
    private Path index;
    private Path documents;

    @Before
    public void setUp() throws Exception{
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
    }

    @After
    public void tearDown(){
        // remove the index created
        this.deleteDirectory(this.index);
    }

    @Test
    public void testIndex1() {
        ProjectLogger.setLevel(Level.FINEST);
        IndexFiles indexer = new IndexFiles();
        try {
            ConvertConfig config = new ConvertConfig();
            config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
            indexer.indexDirectory(this.index, this.documents, true, config);
            // make sure some files were created
            String[]entries = this.index.toFile().list();
            assertEquals(entries.length, 5);
            // try opening the index if no errors are raised should be fine
            IndexReader reader = DirectoryReader.open(FSDirectory.open(this.index));
            new IndexSearcher(reader);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("IOException raised");
        }
    }
}
