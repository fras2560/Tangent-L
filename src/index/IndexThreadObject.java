package index;

import java.nio.file.Path;
import java.nio.file.Paths;

public class IndexThreadObject {
    public static String COMPLETE;
    public static String MOVE_ON;
    private String path;
    private long lastModified;
    public IndexThreadObject(String path, long lastModified){
        this.path = path;
        this.lastModified = lastModified;
    }

    public boolean complete(){
        boolean done = false;
        if (this.path.equals(IndexThreadObject.COMPLETE)){
            done = true;
        }
        return done;
    }

    public long getLastModified(){
        return this.lastModified;
    }

    public Path getFilePath(){
        return Paths.get(this.path);
    }

}
