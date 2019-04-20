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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import query.MathQuery;

/**
 * A class that used to score results.
 *
 * @author Dallas
 */
public class ScoreResults {
  Judgments answers;
  HashMap<String, ArrayList<Result>> results;

  /**
   * Constructor.
   *
   * @param answers filepath to the list of judged relevant files for each query
   */
  public ScoreResults(Path answers) {
    this.answers = new Judgments(answers.toFile());
    this.results = new HashMap<String, ArrayList<Result>>();
  }

  /**
   * Loads the results to be socred.
   *
   * @param results filepath to the results.
   * @throws IOException - issue with reading/writing to a file
   */
  @SuppressWarnings("resource")
  public void loadResults(Path results) throws IOException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(results.toFile()));
      String line;
      String queryName;
      MathQuery query;
      String filename;
      Float rank;
      this.results = new HashMap<String, ArrayList<Result>>();
      while ((line = reader.readLine()) != null) {
        final String[] parts = line.split(" ");
        if (parts.length < 6) {
          throw new ResultException("Parsing error when loading results");
        }
        queryName = parts[0];
        query = new MathQuery(queryName);
        filename = this.parseTitle(parts[2]);
        rank = this.answers.findResult(query, filename);
        this.addResult(queryName, new Result(filename, rank));
      }
      reader.close();
    } catch (final Exception e) {
      if (reader != null) {
        reader.close();
      }
      System.err.format("Exception occurred trying to read '%s'.", results.toString());
      e.printStackTrace();
    }
  }

  /**
   * Parses the title from the filepath.
   *
   * @param title - the given filepath or filename
   * @return just the title of the document (no ext)
   */
  private String parseTitle(String title) {
    final String[] parts = title.split("/");
    final String filename = parts[parts.length - 1];
    final String[] temp = filename.split("\\.");
    final String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
    return String.join(".", nameparts);
  }

  /**
   * Adds the result for the query.
   *
   * @param queryName - the name of the query
   * @param result - the result of the query
   */
  private void addResult(String queryName, Result result) {
    if (this.results.get(queryName) == null) {
      this.results.put(queryName, new ArrayList<Result>());
    }
    this.results.get(queryName).add(result);
  }

  /**
   * Calculate bpref and print it out.
   *
   * @param lower - the lower bound of whether something is relevant or not
   */
  private void calculateBpref(Float lower) {
    Float total = (float) 0.0;
    Float count = (float) 0.0;
    Float bpref = (float) 0.0;
    Float n = (float) 0.0;
    Float valueR = (float) 0.0;
    Float valueN = (float) 0.0;
    final Iterator<Entry<String, ArrayList<Result>>> it = this.results.entrySet().iterator();
    while (it.hasNext()) {
      @SuppressWarnings("rawtypes")
      final Map.Entry pair = it.next();
      n = (float) 0.0;
      valueR = (float) 0.0;
      valueN = (float) 0.0;
      bpref = (float) 0.0;
      // calculate R and N
      for (final Result entry : this.results.get(pair.getKey())) {
        if (entry.getRank() > lower) {
          valueR += (float) 1.0;
        } else if (entry.getRank() != (float) -1.0) {
          valueN += (float) 1.0;
        }
      }
      // find the minimum
      Float denominator;
      if (valueR > valueN) {
        denominator = valueN;
      } else {
        denominator = valueR;
      }
      if (denominator == (float) 0.0) {
        denominator = (float) 1.0;
      }
      // loop through and sum min(# n above r, R)
      for (final Result entry : this.results.get(pair.getKey())) {
        if (entry.getRank() > lower) {
          bpref += ((float) 1.0 - java.lang.Math.min(n, valueR) / denominator);
        } else if (entry.getRank() != -1.0) {
          n += (float) 1.0;
        }
      }
      if (valueR > 0) {
        bpref = 1 / valueR * bpref;
      } else {
        bpref = (float) 0.0;
      }
      total += bpref;
      count += 1;
    }
    System.out.println("Bref: " + total / this.results.size());
  }

  /**
   * Calculates Map and prints it out.
   *
   * @param lower - the lower bound of whether something is deemed relevant
   */
  private void calculateMap(Float lower) {
    Float total = (float) 0.0;
    Float count = (float) 0.0;
    Float precision = (float) 0.0;
    Float r = (float) 0.0;
    final Iterator<Entry<String, ArrayList<Result>>> it = this.results.entrySet().iterator();
    while (it.hasNext()) {
      @SuppressWarnings("rawtypes")
      final Map.Entry pair = it.next();
      precision = (float) 0.0;
      count = (float) 0.0;
      r = (float) 0.0;
      // calculate R and N
      for (final Result entry : this.results.get(pair.getKey())) {
        count += (float) 1.0;
        if (entry.getRank() > lower) {
          r += (float) 1.0;
          precision += (r / count);
        }
      }
      if (r > 0) {
        precision = precision / r;
      }

      total += precision;
    }
    System.out.println("MAP: " + total / this.results.size());
  }

  /**
   * A class that holds the result rank.
   *
   * @author Dallas
   */
  private class Result {
    private Float rank;

    /**
     * Constructor.
     *
     * @param filename - the result's filename
     * @param rank - the rank of the result
     */
    public Result(String filename, Float rank) {
      this.setFilename(filename);
      this.setRank(rank);
    }

    public void setFilename(String filename) {}

    public Float getRank() {
      return this.rank;
    }

    public void setRank(Float rank) {
      this.rank = rank;
    }
  }

  /**
   * An exception when dealing with results.
   *
   * @author Dallas
   */
  private class ResultException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param string - the exception message
     */
    public ResultException(String string) {
      super(string);
    }
  }

  /**
   * Mainly a testing method the will loop through files and if they are a queries file.
   *
   * @param files - a list of files to crunch numbers for
   * @param lower - the lower bound for whether something is relevant or not.
   * @throws IOException - issue when reading/writing to a file.
   */
  private void crunchNumbers(File[] files, Float lower) throws IOException {
    for (final File file : files) {
      if (file.isDirectory()) {
        System.out.println("----------------------------------------------------------------");
        System.out.println("Directory: " + file.getName());
        System.out.println("----------------------------------------------------------------");
        this.crunchNumbers(file.listFiles(), lower); // Calls same method again.
      } else {
        if (file.getName().contains("queries") == true) {
          this.loadResults(file.toPath());
          System.out.println("File: " + file.getName());
          this.calculateMap(lower);
          this.calculateBpref(lower);
          System.out.println();
        }
      }
    }
  }

  /**
   * Main for testing some of the methods.
   *
   * @param args - the arguments
   * @throws Exception - issue with reading and writing to files
   */
  public static void main(String[] args) throws Exception {
    final Path answers =
        Paths.get(System.getProperty("user.dir"), "resources", "results", "simple-results.dat");
    final Path results =
        Paths.get(System.getProperty("user.dir"), "resources", "output", "SolidTesting");
    final File[] files = results.toFile().listFiles();
    final ScoreResults scorer = new ScoreResults(answers);
    scorer.crunchNumbers(files, (float) 2.0);
  }
}
