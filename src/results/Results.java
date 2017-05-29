package results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import query.MathQuery;

public class Results {
    private ArrayList<Result> resultsList;
    public Results(File f){
        this.resultsList = this.parseFile(f);
        
    }
    public Float findResult(MathQuery query, String file){
        Float result = new Float(-1.0);
        for (Result entry : this.resultsList){
            if (entry.equals(query) && entry.equals(file)){
                result = entry.getRank();
                break;
            }
        }
        return result;
    }
    public int length(){
        return this.resultsList.size();
    }
    private ArrayList<Result> parseFile(File f){
        ArrayList<Result> records = new ArrayList<Result>();
        try {
          BufferedReader reader = new BufferedReader(new FileReader(f));
          String line;
          while ((line = reader.readLine()) != null){
              records.add(new Result(line));
          }
          reader.close();
          return records;
        }
        catch (Exception e){
          System.err.format("Exception occurred trying to read '%s'.", f);
          e.printStackTrace();
          return null;
        }
    }
    private class Result {
        private String queryName;
        private Float rank;
        private String fileName;
        public Result(String line) throws Exception{
            String[] parts = line.split(" ");
            if (parts.length < 4){
                throw new ResultException("Parsing error when loading results");
            }
            this.queryName = parts[0];
            this.fileName = parts[2];
            this.rank = Float.parseFloat(parts[3]);
        }
        public Float getRank(){
            return this.rank;
        }
        public boolean equals(MathQuery q){
            return this.queryName.equals(q.getQueryName());
        }
        public boolean equals(String f){
            return this.fileName.equals(f);
        }
    }
    private class ResultException extends Exception{
        private static final long serialVersionUID = 1L;
        public ResultException(String string) {
            super(string);
        }
    }
}
