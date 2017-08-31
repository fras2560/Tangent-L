package results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import query.MathQuery;

public class Results {
    private ArrayList<Result> resultsList;
    private static final Float rLower = new Float(2.0);
    private static final Float pLower = new Float(0.0);
    private boolean showResults;
    private boolean debugResults;
    public Results(File f){
        this.resultsList = this.parseFile(f);
        this.showResults = false;
        this.debugResults = false;
    }
    public Results(File f, boolean showResults, boolean debugResults){
        this.resultsList = this.parseFile(f);
        this.showResults = showResults;
        this.debugResults = debugResults;
    }
    public void output(String s){
        if (this.showResults){
            System.out.println(s);
        }
    }
    public void debug(String s){
        if(this.debugResults){
            System.out.println(s);
        }
    }
    public float resultsContainAnswers(MathQuery query, ArrayList<String> files){
        int count = 0;
        int total = 0;
        for (Result entry: this.resultsList){
            if (entry.equals(query)){
                total += 1;
                if (entry.getRank() > Results.pLower){
                    count += 1;
                }
            }
        }
        return (float) count / (float) total;
    }
    public int[] recallResult(MathQuery query, ArrayList<String> files){
        int r_docs = 0;
        int pr_docs = 0;
        int r_found = 0;
        int pr_found = 0;
        for(Result entry: this.resultsList){
            if (entry.equals(query)){
                if(entry.getRank() > Results.rLower){
                    r_docs += 1;
                    if (this.containsResults(entry, files)){
                        r_found += 1;
                    }else{
                        this.debug(entry.toString());
                    }
                }
                if (entry.getRank() > Results.pLower){
                    pr_docs += 1;
                    if (this.containsResults(entry, files)){
                        pr_found += 1;
                    }else{
                        this.debug(entry.toString());
                    }
                }
            }
        }
//        this.output("Query Name: " + query.getQueryName() +
//                    " Relevant: " +r_docs + " Relevant Found: " +
//                    r_found + " PR:" + pr_docs + " PR Found:" + pr_found);
//        this.output(((float) r_found / (float) r_docs) + " " + ((float) pr_found / (float) pr_docs));
        return new int[]{r_docs,r_found, pr_docs, pr_found};
    }
    public boolean containsResults(Result result, ArrayList<String> files){
        boolean contained = false;
        for (String file: files){
            if (result.equals(file)){
                contained = true;
            }
        }
        return contained;
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
        public String toString(){
            return "Query Name: " + this.queryName + " Filename:" + this.fileName;
        }
    }
    private class ResultException extends Exception{
        private static final long serialVersionUID = 1L;
        public ResultException(String string) {
            super(string);
        }
    }
}
