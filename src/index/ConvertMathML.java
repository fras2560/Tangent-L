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
package index;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import utilities.ProjectLogger;


/*
 * This class converts a File's MathML to Tangent Tuples
 * 
 * @author Dallas Fraser
 * @see ConvertConfig
 * @since 2017-09-06
 */
public class ConvertMathML {
    /*
     * file holds the path to the file to convert
     * app holds the path to the tangent app
     */
    private Path file;
    private Path app;
    private Logger logger;
    /*
     * Class constructor
     * @param f the path to the file to convert
     */
    public ConvertMathML(Path f){
        this(f, ProjectLogger.getLogger());
    }
    /*
     * Class constructor with a specified logger
     * @param f the path to the file to convert
     * @param logger the 
     */
    public ConvertMathML(Path f, Logger logger){
        this.app = Paths.get(System.getProperty("user.dir"), "src", "tangent", "convert.py");
        this.file = Paths.get(f.toFile().getAbsolutePath());
        this.logger = logger;
        this.logger.log(Level.FINEST, "Path:" + this.file.toFile().getAbsolutePath());
        
    }
    /*
     * Converts a file and returns a path to the converted file path
     * @exception IOException
     * @exception InterruptedException
     * @return path to the file that was converted
     */
    public Path convert() throws IOException, InterruptedException{
        ConvertConfig config = new ConvertConfig();
        config.optimalConfig();
        return this.convert(config);
    }
    /*
     * Converts a file using the specified config and returns a path to the converted file path
     * @see ConvertConfig
     * @param config the configuration of features to be used when converting
     * @exception IOException
     * @exception InterruptedException
     * @return path to the file that was converted
     */
    public Path convert(ConvertConfig config) throws IOException, InterruptedException{
        // the output file
        String[] fn = this.file.getFileName().toString().split("\\.");
        String new_filename = fn[0] + "_temp." + fn[1];
        String new_path = Paths.get(this.file.getParent().toString(), new_filename).toString();
        Path new_file = Paths.get(new_path);
        String[] attributes = config.toCommands();
        String[] program = {"python3",
                            this.app.toString(),
                            "-infile",
                            this.file.toString(),
                            "-outfile",
                            new_file.toString()};
        String[] command = ArrayUtils.addAll(program,attributes);
        this.logger.log(Level.FINEST, "Command");
        for (String s : command){
            this.logger.log(Level.FINEST, s);
            
        }
        Process proc = Runtime.getRuntime().exec(command);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        // read the output from the command
        this.logger.log(Level.FINEST, "Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            this.logger.log(Level.FINEST, s);
        }
        // read any errors from the attempted command
        this.logger.log(Level.FINEST, "Here is the standard error of the command:\n");
        while ((s = stdError.readLine()) != null) {
            this.logger.log(Level.FINEST, s);
        }
        proc.waitFor();
        return new_file;
	}
}