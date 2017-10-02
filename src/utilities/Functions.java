package utilities;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Functions {

    public static String parseTitle(String title){
        String[] parts = title.split("/|\\\\");
        String filename = parts[parts.length -1];
        String[] temp = filename.split("\\.");
        String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
        return String.join(".", nameparts);
    }

    public static Path createtempFile(Path file){
        String title = parseTitle(file.getFileName().toString());
        String[] fn = file.getFileName().toString().split("\\.");
        String filenameExtension = fn[fn.length - 1];
        String new_filename = title + "_temp." + filenameExtension;
        Path new_path = Paths.get(file.getParent().toString(), new_filename);
        return new_path;
        
    }
}
