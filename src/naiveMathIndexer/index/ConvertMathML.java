package naiveMathIndexer.index;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConvertMathML {
    private Path file;
    private Path app;
    public ConvertMathML(Path f){
        this.app = Paths.get(System.getProperty("user.dir"), "src", "tangent", "convert.py");
        this.file = Paths.get(f.toFile().getAbsolutePath());
        System.out.println("Path");
        System.out.println(this.file.toFile().getAbsolutePath());
    }

    public Path convert() throws IOException, InterruptedException{
        // the output file
        
        String[] fn = this.file.getFileName().toString().split("\\.");
        String new_filename = fn[0] + "_temp." + fn[1];
        String new_path = Paths.get(this.file.getParent().toString(), new_filename).toString();
        Path new_file = Paths.get(new_path);
        String[] command = {"python3",
                            this.app.toString(),
                            "-infile",
                            this.file.toString(),
                            "-outfile",
                            new_file.toString()};
        System.out.println("Command");
        for (String s : command){
            System.out.println(s);
        }
        Process proc = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(proc.getInputStream()));

           BufferedReader stdError = new BufferedReader(new 
                InputStreamReader(proc.getErrorStream()));

           // read the output from the command
           System.out.println("Here is the standard output of the command:\n");
           String s = null;
           while ((s = stdInput.readLine()) != null) {
               System.out.println(s);
           }

           // read any errors from the attempted command
           System.out.println("Here is the standard error of the command (if any):\n");
           while ((s = stdError.readLine()) != null) {
               System.out.println(s);
           }
        
        proc.waitFor();
        return new_file;
	}
}
