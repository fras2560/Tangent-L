package testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import search.Search;
import search.SearchResult;
import utilities.Functions;
import utilities.ProjectLogger;

public class BaseTest {
    public boolean deleteDirectory(Path directory){
        File dir = directory.toFile();
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i].toPath());
                if (!success) {
                    return false;
                }
            }
        }
        // either file or an empty directory
        System.out.println("removing file or directory : " + dir.getName());
        return dir.delete();
    }

    public void setupLogger(Path logger) throws SecurityException, IOException{
        ProjectLogger.setLogFile(logger);
        ProjectLogger.setLevel(Level.FINEST);
    }

    public void debugLogger(){
        ProjectLogger.setLevel(Level.FINEST);
    }

    public boolean compareResults(ArrayList<String> expect,
                                  SearchResult queryResult,
                                  Search searcher) throws IOException{
        ScoreDoc[] hits = queryResult.getResults().scoreDocs;
        Document doc;
        int index = 0;
        boolean same = true;
        for (ScoreDoc hit : hits){
            // loop through every result of the
            doc = searcher.getSearcher().doc(hit.doc);
            // make sure it is in the expected list
            System.out.println(expect.get(index) + " " +  Functions.parseTitle(doc.get("path")));
            if (!expect.get(index).equals(Functions.parseTitle(doc.get("path")))){
                same = false;
                break;
            }
            index += 1;
        }
        return same;
    }
}
