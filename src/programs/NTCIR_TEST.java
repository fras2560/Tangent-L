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

public class NTCIR_TEST {
    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava programs.NTCIR_TEST [-index dir] [-queries file] [-judgements file] [-resultsOutput file] [-queriesOutput file] [-logFile file]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        Path index = Paths.get(System.getProperty("user.dir"), "resources", "index", "arXiv", "current");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-ArXiv.xml");
        Path results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR12-ArXiv-Math.dat");
        String date = new SimpleDateFormat("dd-MM-yyyy:HH:mm").format(new Date());
        Path logFile = Paths.get(System.getProperty("user.dir"),
                                 "resources",
                                 "output",
                                 "arXiv",
                                 date + ".log");
        Path queryOutput = Paths.get(System.getProperty("user.dir"),
                                     "resources",
                                     "output",
                                     "arXiv",
                                     date + "-queries.txt");
        Path resultOutput = Paths.get(System.getProperty("user.dir"),
                                      "resources",
                                      "output",
                                      "arXiv",
                                      date + "-results.txt");
        for(int i = 0;i < args.length;i++) {
          if ("-index".equals(args[i])) {
              index = Paths.get(args[i+1]);
              i++;
          } else if ("-results".equals(args[i])) {
              results = Paths.get(args[i+1]);
              i++;
          } else if ("-queries".equals(args[i])) {
              queries = Paths.get(args[i+1]);
              i++;
          } else if("-resultsOutput".equals(args[i])){
              resultOutput = Paths.get(args[i+1]);
              i++;
          }else if("-queriesOutput".equals(args[i])){
              queryOutput = Paths.get(args[i+1]);
              i++;
          }else if("-logFile".equals(args[i])){
              logFile = Paths.get(args[i+1]);
          }
        }
        logFile.toFile().createNewFile();
        ProjectLogger.setLevel(Level.FINEST);
        ProjectLogger.setLogFile(logFile);
        BufferedWriter queryWriter = null;
        BufferedWriter resultsWriter = null;
        try {
            // write out the queries
            File queryText = queryOutput.toFile();
            queryText.createNewFile();
            FileOutputStream qis = new FileOutputStream(queryText);
            OutputStreamWriter qosw = new OutputStreamWriter(qis);
            queryWriter = new BufferedWriter(qosw);
            // write out the precisions results of each file
            File resultsText = resultOutput.toFile();
            resultsText.createNewFile();
            FileOutputStream ris = new FileOutputStream(resultsText);
            OutputStreamWriter rosw = new OutputStreamWriter(ris);
            resultsWriter = new BufferedWriter(rosw);
            // setup the config file for searching
            ConvertConfig config = new ConvertConfig();
            config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
            // do the actual searching
            Search searcher = new Search(index, config);
            searcher.ntcirTest(queries, results, resultsWriter);
            searcher.recordQueries(queries, queryWriter, 100);;
            // close the files
            resultsWriter.close();
            queryWriter.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
            if(queryWriter != null){
                queryWriter.close();
            }
            if (resultsWriter != null){
                resultsWriter.close();
            }
        }
    }
}
