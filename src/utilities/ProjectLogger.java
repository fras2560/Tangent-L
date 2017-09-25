package utilities;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ProjectLogger {
    private static Logger logger = Logger.getLogger(ProjectLogger.class.getName());
    private static Level level = Level.INFO;
    public ProjectLogger(){
        
    }

    public static void setLevel(Level level){
        ProjectLogger.level = level;
        ProjectLogger.logger.setLevel(level);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        ProjectLogger.logger.addHandler(consoleHandler);
        ProjectLogger.logger.setUseParentHandlers( false );
    }

    public static Logger getLogger(){
        return ProjectLogger.logger;
    }

    public static void setLogger(Logger logger){
        ProjectLogger.logger = logger;
    }

    public static void setLogFile(Path logFile) throws SecurityException, IOException{
        Handler fileHandler  = new FileHandler(logFile.toString());
        SimpleFormatter formatter = new SimpleFormatter(); 
        fileHandler.setFormatter(formatter);
        fileHandler.setLevel(ProjectLogger.level);
        ProjectLogger.logger.addHandler(fileHandler);
    }
}
