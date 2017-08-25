package results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import query.MathQuery;


public class ScoreResults {
    Results answers;
    HashMap<String, ArrayList<Result>> results;
    public ScoreResults(Path answers){
        this.answers = new Results(answers.toFile());
        this.results = new HashMap<String, ArrayList<Result>>();
    }
    public void loadResults(Path results){
        try {
          BufferedReader reader = new BufferedReader(new FileReader(results.toFile()));
          String line;
          String queryName;
          MathQuery query;
          String filename;
          Float rank;
          this.results = new HashMap<String, ArrayList<Result>>();
          while ((line = reader.readLine()) != null){
              String[] parts = line.split(" ");
              if (parts.length < 6){
                  throw new ResultException("Parsing error when loading results");
              }
              queryName = parts[0];
              query = new MathQuery(queryName);
              filename = this.parseTitle(parts[2]);
              rank = this.answers.findResult(query, filename);
              this.addResult(queryName, new Result(filename, rank));
          }
        }
        catch (Exception e){
          System.err.format("Exception occurred trying to read '%s'.", results.toString());
          e.printStackTrace();
        }
    }
    private String parseTitle(String title){
        String[] parts = title.split("/");
        String filename = parts[parts.length -1];
        String[] temp = filename.split("\\.");
        String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
        return String.join(".", nameparts);
    }
    private void addResult(String queryName, Result result){
        if (this.results.get(queryName) == null){
            this.results.put(queryName, new ArrayList<Result>());
        }
        this.results.get(queryName).add(result);
    }
    private void calculateBpref(Float lower){
        Float total = (float) 0.0;
        Float count = (float) 0.0;
        Float bpref = (float) 0.0;
        Float n = (float) 0.0;
        Float R = (float) 0.0;
        Float N = (float) 0.0;
        Iterator it = this.results.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            n = (float) 0.0;
            R = (float) 0.0;
            N = (float) 0.0;
            bpref = (float) 0.0;
            // calculate R and N
            for (Result entry : this.results.get(pair.getKey())){
                if (entry.getRank() > lower){
                    R += (float) 1.0;
                }else if(entry.getRank() != (float) -1.0){
                    N += (float) 1.0;
                }
            }
            // find the minimum
            Float denominator;
            if (R > N){
                denominator = N;
            }else{
                denominator = R;
            }
            if (denominator == (float) 0.0){
                denominator = (float) 1.0;
            }
            // loop through and sum min(# n above r, R)
            for (Result entry : this.results.get(pair.getKey())){
                if (entry.getRank() > lower){
                    bpref += ((float) 1.0 - java.lang.Math.min(n, R) / denominator);
                }else if(entry.getRank() != -1.0){
                    n += (float) 1.0;
                }
            }
            if (R > 0){
                bpref = 1 / R * bpref;
            }else{
                bpref = (float) 0.0;
            }
            total += bpref;
            count += 1;
        }
        System.out.println("Bref: " + total / this.results.size());
    }
    private void calculateMAP(Float lower){
        Float total = (float) 0.0;
        Float count = (float) 0.0;
        Float precision = (float) 0.0;
        Float r = (float) 0.0;
        Iterator it = this.results.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            precision = (float) 0.0;
            count = (float) 0.0;
            r = (float) 0.0;
            // calculate R and N
            for (Result entry : this.results.get(pair.getKey())){
                count += (float) 1.0;
                if (entry.getRank() > lower){
                    r += (float) 1.0;
                    precision += (r / count);
                }
            }
            if (r > 0){
                precision = precision / r;
            }
            
            total += precision;
        }
        System.out.println("MAP: " + total / this.results.size());
    }
    private class Result {
        private String filename;
        private Float rank;
        public Result(String filename, Float rank){
            this.setFilename(filename);
            this.setRank(rank);
        }
        public String getFilename() {
            return filename;
        }
        public void setFilename(String filename) {
            this.filename = filename;
        }
        public Float getRank() {
            return rank;
        }
        public void setRank(Float rank) {
            this.rank = rank;
        }
    }
    private class ResultException extends Exception{
        private static final long serialVersionUID = 1L;
        public ResultException(String string) {
            super(string);
        }
    }
    private void crunchNumbers(File[] files, Float lower){
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("----------------------------------------------------------------");
                System.out.println("Directory: " + file.getName());
                System.out.println("----------------------------------------------------------------");
                this.crunchNumbers(file.listFiles(), lower); // Calls same method again.
            } else {
                if(file.getName().contains("queries") == true){
                    this.loadResults(file.toPath());
                    System.out.println("File: " + file.getName());
                    this.calculateMAP(lower);
                    this.calculateBpref(lower);
                    System.out.println();
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        Path answers = Paths.get(System.getProperty("user.dir"), "resources", "results", "simple-results.dat");
        Path results = Paths.get(System.getProperty("user.dir"), "resources", "output", "SolidTesting");
        File[] files = results.toFile().listFiles();
        ScoreResults scorer = new ScoreResults(answers);
        scorer.crunchNumbers(files, (float) 2.0); 
    }
}
