package programs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import index.ConvertConfig;
import search.Search;
import utilities.ProjectLogger;

public class OutputDifferentMathWeights {
    public static BufferedWriter createFile(Path directory, float mathWeight) throws IOException{
        File nf = Paths.get(directory.toString(), String.valueOf(Math.round(mathWeight * 100)) + ".txt").toFile();
        nf.createNewFile();
        FileOutputStream outputIS = new FileOutputStream(nf);
        OutputStreamWriter osw = new OutputStreamWriter(outputIS);
        return new BufferedWriter(osw);
    }
    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava programs.OutputDifferentMathWeights [-index dir] [-queries file] [-logFile file]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        Path index = Paths.get(System.getProperty("user.dir"), "resources", "index", "ntcir-12-wikipedia", "current");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-MathWiki-main.xml");
        String date = new SimpleDateFormat("dd-MM-yyyy:HH:mm").format(new Date());
        Path output = Paths.get(System.getProperty("user.dir"), "resources", "output", "ntcir12-wikipedia", "weights");
        Path logFile = Paths.get(System.getProperty("user.dir"),
                                 "resources",
                                 "output",
                                 "wikipedia",
                                 date + ".log");
        for(int i = 0;i < args.length;i++) {
          if ("-index".equals(args[i])) {
              index = Paths.get(args[i+1]);
              i++;
          }else if ("-queries".equals(args[i])) {
              queries = Paths.get(args[i+1]);
              i++;
          }else if("-logFile".equals(args[i])){
              logFile = Paths.get(args[i+1]);
          }
        }
        logFile.toFile().createNewFile();
        ProjectLogger.setLevel(Level.INFO);
        ProjectLogger.setLogFile(logFile);
        try {
            // setup the config file by loading what it is in the index
            ConvertConfig config = new ConvertConfig();
            config.loadConfig(index);
            // config.setQueryType(ConvertConfig.BM25TP_QUERY);
            config.setQueryType(ConvertConfig.DIFFERENT_WEIGHTED_QUERY);
            // do the actual searching
            Search searcher = new Search(index, config);
            searcher.setBeta(1f);
            BufferedWriter outputWriter;
            // config.setMathBM25(true);
            for (float mathWeight = 0.01f; mathWeight <= 2.00f; mathWeight = mathWeight + 0.01f){
                searcher.setAlpha(mathWeight);
                outputWriter = OutputDifferentMathWeights.createFile(output, mathWeight);
                searcher.recordQueries(queries, outputWriter, 1000);
                outputWriter.close();
                System.out.println(mathWeight);
            }
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
        }
    }
}
