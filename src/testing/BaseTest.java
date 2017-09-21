package testing;

import java.io.File;
import java.nio.file.Path;

public class BaseTest {
    public void deleteDirectory(Path directory){
        File index = directory.toFile();
        String[]entries = index.list();
        for(String s: entries){
            File currentFile = new File(index.getPath(),s);
            currentFile.delete();
        }
        index.delete();
    }
}
