package programs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

import index.ConvertConfig;
import search.Search;
import utilities.ProjectLogger;

public class FindOptimalWeight {
    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava programs.FindOptimalWeight [-index dir] [-queries file] [-judgements file] [-logFile file]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        Path index = Paths.get(System.getProperty("user.dir"), "resources", "index", "ntcir-12-wikipedia", "current");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-MathWiki-main.xml");
        Path results = Paths.get(System.getProperty("user.dir"), "resources", "results", "NTCIR12-MathWiki-main.dat");
        String date = new SimpleDateFormat("dd-MM-yyyy:HH:mm").format(new Date());
        Path output = Paths.get(System.getProperty("user.dir"), "resources", "output", "arXiv", "weights", date + ".csv");
        Path logFile = Paths.get(System.getProperty("user.dir"),
                                 "resources",
                                 "output",
                                 "arXiv",
                                 date + ".log");
        for(int i = 0;i < args.length;i++) {
          if ("-index".equals(args[i])) {
              index = Paths.get(args[i+1]);
              i++;
          }else if ("-results".equals(args[i])) {
              results = Paths.get(args[i+1]);
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
        BufferedWriter outputWriter = null;
        try {
            //  write out the queries
            File outputText = output.toFile();
            outputText.createNewFile();
            FileOutputStream outputIS = new FileOutputStream(outputText);
            OutputStreamWriter osw = new OutputStreamWriter(outputIS);
            outputWriter = new BufferedWriter(osw);
            // setup the config file by loading what it is in the index
            ConvertConfig config = new ConvertConfig();
            config.loadConfig(index);
            // config.setQueryType(ConvertConfig.BM25TP_QUERY);
            config.setQueryType(ConvertConfig.DIFFERENT_WEIGHTED_QUERY);
            // do the actual searching
            Search searcher = new Search(index, config);
            float bestMAPR = 0f;
            float bestMAPPR = 0f;
            float bestMathWeight = 0f;
            float bestMathWeightPR = 0f;
            float tempMAPR, tempMAPPR;
            float[] result;
            searcher.setBeta(1f);
            // config.setMathBM25(true);
            for (float mathWeight = 0.01f; mathWeight <= 3f; mathWeight = mathWeight + 0.01f){
                searcher.setAlpha(mathWeight);
                result = searcher.runNtcirTest(queries, results);
                System.out.println(Arrays.toString(result));
                tempMAPR = (result[0] / 5 + result[1] / 10 + result[2] / 15 + result[3] / 20) / 4;
                tempMAPPR = (result[4] / 5 + result[5] / 10 + result[6] / 15 + result[7] / 20) / 4;
                outputWriter.write(mathWeight + "," + tempMAPR + "," + tempMAPPR);
                outputWriter.newLine();
                if(tempMAPR > bestMAPR){
                    bestMathWeightPR = mathWeight;
                    bestMAPR = tempMAPR;
                }
                if(tempMAPPR > bestMAPPR){
                    bestMathWeight = mathWeight; 
                    bestMAPPR = tempMAPPR;
                }
            }
            System.out.println("Best alpha for Relevant Results: " + bestMAPR + " @ mathWeight = " + bestMathWeight);
            System.out.println("Best alpha for Partially Relevant Results: " + bestMAPPR +  " @ mathWeight = " + bestMathWeightPR);
            outputWriter.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
            if(outputWriter != null){
                outputWriter.close();
            }
        }
    }
}
