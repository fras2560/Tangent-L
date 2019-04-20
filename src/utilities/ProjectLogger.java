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

package utilities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * The logger to be used for the project.
 *
 * @author Dallas Fraser
 * @since 2017-11-09
 */
public class ProjectLogger {
  private static Logger logger = Logger.getLogger(ProjectLogger.class.getName());
  private static Level level = Level.INFO;

  /** The class constructor. */
  public ProjectLogger() {}

  /**
   * Set the level of the logger.
   *
   * @param level the level of the logger
   */
  public static void setLevel(Level level) {
    ProjectLogger.level = level;
    ProjectLogger.logger.setLevel(level);
    final Handler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(level);
    ProjectLogger.logger.addHandler(consoleHandler);
    ProjectLogger.logger.setUseParentHandlers(false);
  }

  /**
   * Returns the logger.
   *
   * @return Logger
   */
  public static Logger getLogger() {
    return ProjectLogger.logger;
  }

  /**
   * Set the logger to the given logger.
   *
   * @param logger - the logger to use
   */
  public static void setLogger(Logger logger) {
    ProjectLogger.logger = logger;
  }

  /**
   * Sets the file to log to.
   *
   * @param logFile the path to the file to log to
   * @throws SecurityException - issue with the security level
   * @throws IOException - issue while dealing with a file
   */
  public static void setLogFile(Path logFile) throws SecurityException, IOException {
    final Handler fileHandler = new FileHandler(logFile.toString());
    final SimpleFormatter formatter = new SimpleFormatter();
    fileHandler.setFormatter(formatter);
    fileHandler.setLevel(ProjectLogger.level);
    ProjectLogger.logger.addHandler(fileHandler);
  }
}
