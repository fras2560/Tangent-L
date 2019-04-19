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
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import utilities.Functions;
import utilities.ProjectLogger;

/**
 * This class converts a File's MathML to Tangent Tuples.
 *
 * @author Dallas Fraser
 * @see ConvertConfig
 * @since 2017-09-06
 */
public class ConvertMathMl {
  /** file holds the path to the file to convert app holds the path to the tangent app. */
  private final Path file;

  private final Path app;
  private final Logger logger;

  /**
   * Class constructor.
   *
   * @param f the path to the file to convert
   */
  public ConvertMathMl(Path f) {
    this(f, ProjectLogger.getLogger());
  }

  /**
   * Class constructor with a specified logger.
   *
   * @param f the path to the file to convert
   * @param logger the
   */
  public ConvertMathMl(Path f, Logger logger) {
    this.app = Paths.get(System.getProperty("user.dir"), "src", "tangent", "convert.py");
    this.file = Paths.get(f.toFile().getAbsolutePath());
    this.logger = logger;
    this.logger.log(Level.FINEST, "Path:" + this.file.toFile().getAbsolutePath());
  }

  /**
   * Converts a file and returns a reader to the converted file.
   *
   * @exception IOException - issue when reading an file
   * @exception InterruptedException - raised when the process has an interruption
   * @return path to the file that was converted
   */
  public ConvertResult convert() throws IOException, InterruptedException {
    final ConvertConfig config = new ConvertConfig();
    config.optimalConfig();
    return this.convert(config);
  }

  /**
   * Converts a file using the specified config and ConvertResult which contains a reader.
   *
   * @param config the configuration of features to be used when converting
   * @return ConvertResult contains a reader and some stats about the document
   * @throws IOException - issue when reading a file
   * @throws InterruptedException - raised when the process has an interruption
   * @see ConvertConfig
   */
  public ConvertResult convert(ConvertConfig config) throws IOException, InterruptedException {
    // the output file
    final String[] attributes = config.toCommands();
    final String[] program = {"python3", this.app.toString(), "-infile", this.file.toString()};
    String[] command = ArrayUtils.addAll(program, attributes);
    this.logger.log(Level.FINEST, "Command");
    for (final String s : command) {
      this.logger.log(Level.FINEST, s);
    }
    Process proc;
    try {
      proc = Runtime.getRuntime().exec(command);
    } catch (final IOException e) {
      this.logger.log(Level.WARNING, "Unable to find python3 using python command");
      program[0] = "python";
      command = ArrayUtils.addAll(program, attributes);
      proc = Runtime.getRuntime().exec(command);
    }
    final BufferedReader stdInput =
        new BufferedReader(new InputStreamReader(proc.getInputStream()));
    final BufferedReader stdError =
        new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    // read the output from the command
    this.logger.log(Level.FINEST, "Here is the standard output of the command:\n");
    String s = null;
    final StringWriter outBuffer = new StringWriter();
    while (proc.isAlive() || stdInput.ready() || stdError.ready()) {
      if (stdInput.ready()) {
        if ((s = stdInput.readLine()) != null) {
          outBuffer.write(s);
          outBuffer.write("\n");
        }
      }
      if (stdError.ready()) {
        if ((s = stdError.readLine()) != null) {
          this.logger.log(Level.SEVERE, this.file.toString() + ":" + s);
        }
      }
    }
    proc.waitFor();
    final ConvertResult cr = new ConvertResult(outBuffer);
    return cr;
  }

  /**
   * Converts the MathML file to Tuples using Tangent Returns a temporary file.
   *
   * @param config the config file with the features to use
   * @return Path the path to the temporary file
   * @throws IOException - issue when reading a file
   * @throws InterruptedException - raised when the process has an interruption
   */
  public Path convertPath(ConvertConfig config) throws IOException, InterruptedException {
    // the output file
    final Path new_file = Functions.createtempFile(this.file);
    final String[] attributes = config.toCommands();
    final String[] program = {
      "python3",
      this.app.toString(),
      "-infile",
      this.file.toString(),
      "-outfile",
      new_file.toString()
    };
    String[] command = ArrayUtils.addAll(program, attributes);
    this.logger.log(Level.FINEST, "Command");
    for (final String s : command) {
      this.logger.log(Level.FINEST, s);
    }
    Process proc;
    try {
      proc = Runtime.getRuntime().exec(command);
    } catch (final IOException e) {
      this.logger.log(Level.WARNING, "Unable to find python3 using python command");
      program[0] = "python";
      command = ArrayUtils.addAll(program, attributes);
      proc = Runtime.getRuntime().exec(command);
    }
    final BufferedReader stdInput =
        new BufferedReader(new InputStreamReader(proc.getInputStream()));
    final BufferedReader stdError =
        new BufferedReader(new InputStreamReader(proc.getErrorStream()));

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
