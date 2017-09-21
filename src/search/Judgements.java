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
package search;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import query.MathQuery;
import utilities.ProjectLogger;


public class Judgements {
    private ArrayList<Judgement> resultsList;
    private Logger logger;
    public static final Float rLower = new Float(2.0);
    public static final Float pLower = new Float(0.0);
    public Judgements(File f){
        this.logger = ProjectLogger.getLogger();
        this.resultsList = this.parseFile(f);
    }

    public int[] recallResult(MathQuery query, ArrayList<String> files){
        int r_docs = 0;
        int pr_docs = 0;
        int r_found = 0;
        int pr_found = 0;
        for(Judgement entry: this.resultsList){
            if (entry.equals(query)){
                if(entry.getRank() > Judgements.rLower){
                    r_docs += 1;
                    if (this.containsResults(entry, files)){
                        r_found += 1;
                    }else{
                        this.logger.log(Level.FINEST, "Relevant Result not found: " + entry.toString());
                    }
                }
                if (entry.getRank() > Judgements.pLower){
                    pr_docs += 1;
                    if (this.containsResults(entry, files)){
                        pr_found += 1;
                    }else{
                        this.logger.log(Level.FINEST, "Partially Relevant Result not found: " + entry.toString());
                    }
                }
            }
        }
        return new int[]{r_docs,r_found, pr_docs, pr_found};
    }

    public boolean containsResults(Judgement result, ArrayList<String> files){
        boolean contained = false;
        for (String file: files){
            if (result.equals(file)){
                contained = true;
            }
        }
        this.logger.log(Level.FINEST, "Result - " + result + " was found:" + contained);
        return contained;
    }

    public Float findResult(MathQuery query, String file){
        Float result = new Float(-1.0);
        for (Judgement entry : this.resultsList){
            if (entry.equals(query) && entry.equals(file)){
                result = entry.getRank();
                break;
            }
        }
        this.logger.log(Level.FINEST, "Result:" + file.toString() + " Rank:" + result);
        return result;
    }

    public int length(){
        return this.resultsList.size();
    }

    private ArrayList<Judgement> parseFile(File f){
        ArrayList<Judgement> records = new ArrayList<Judgement>();
        try {
          BufferedReader reader = new BufferedReader(new FileReader(f));
          String line;
          while ((line = reader.readLine()) != null){
              records.add(new Judgement(line));
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

    private class Judgement {
        private String queryName;
        private Float rank;
        private String fileName;

        public Judgement(String line) throws Exception{
            String[] parts = line.split(" ");
            if (parts.length < 4){
                throw new JudgementException("Parsing error when loading results");
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

    private class JudgementException extends Exception{
        private static final long serialVersionUID = 1L;
        public JudgementException(String string) {
            super(string);
        }
    }
}
