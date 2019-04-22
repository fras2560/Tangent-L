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

/**
 * A class to hold the judgments for the expected results.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class Judgments {
  private final ArrayList<Judgment> resultsList;
  private final Logger logger;
  public static final Float relevantLower = new Float(2.0);
  public static final Float partiallyRelevantLower = new Float(0.0);

  /**
   * Class Constructor.
   *
   * @param f the file of the expected results
   */
  public Judgments(File f) {
    this.logger = ProjectLogger.getLogger();
    this.resultsList = this.parseFile(f);
  }

  /**
   * Returns a [num of relevant docs, num of relevant found, num of partially relevant docs, num of
   * partially relevant found].
   *
   * @param query the query
   * @param files the list of files returned from the query
   * @return int[] recall results
   */
  public int[] recallResult(MathQuery query, ArrayList<String> files) {
    int relevantDocs = 0;
    int partiallyRelevantDocs = 0;
    int relevantFound = 0;
    int partiallyRelevantFound = 0;
    for (final Judgment entry : this.resultsList) {
      if (entry.equals(query)) {
        if (entry.getRank() > Judgments.relevantLower) {
          relevantDocs += 1;
          if (this.containsResults(entry, files)) {
            relevantFound += 1;
          } else {
            this.logger.log(Level.FINEST, "Relevant Result not found: " + entry.toString());
          }
        }
        if (entry.getRank() > Judgments.partiallyRelevantLower) {
          partiallyRelevantDocs += 1;
          if (this.containsResults(entry, files)) {
            partiallyRelevantFound += 1;
          } else {
            this.logger.log(
                Level.FINEST, "Partially Relevant Result not found: " + entry.toString());
          }
        }
      }
    }
    return new int[] {relevantDocs, relevantFound, partiallyRelevantDocs, partiallyRelevantFound};
  }

  /**
   * Returns whether the result is in the list of files.
   *
   * @param result the result to check
   * @param files the list of files
   * @return boolean True if files contain result
   */
  public boolean containsResults(Judgment result, ArrayList<String> files) {
    boolean contained = false;
    for (final String file : files) {
      if (result.equals(file)) {
        contained = true;
      }
    }
    this.logger.log(Level.FINEST, "Result - " + result + " was found:" + contained);
    return contained;
  }

  /**
   * Returns the ranking of the query and file.
   *
   * @param query the query used
   * @param file the name of the file
   * @return Float Ranking if found, -1 otherwise
   */
  public Float findResult(MathQuery query, String file) {
    Float result = new Float(-1.0);
    for (final Judgment entry : this.resultsList) {
      if (entry.equals(query) && entry.equals(file)) {
        result = entry.getRank();
        break;
      }
    }
    this.logger.log(Level.FINEST, "Result:" + file.toString() + " Rank:" + result);
    return result;
  }

  /**
   * Returns the number of results.
   *
   * @return int the number of judgments
   */
  public int length() {
    return this.resultsList.size();
  }

  /**
   * Parses a file .
   *
   * @param fileToParse - the file to parse
   * @return an array of judgments
   */
  private ArrayList<Judgment> parseFile(File fileToParse) {
    final ArrayList<Judgment> records = new ArrayList<Judgment>();
    try {
      final BufferedReader reader = new BufferedReader(new FileReader(fileToParse));
      String line;
      while ((line = reader.readLine()) != null) {
        records.add(new Judgment(line));
      }
      reader.close();
      return records;
    } catch (final Exception e) {
      System.err.format("Exception occurred trying to read '%s'.", fileToParse);
      e.printStackTrace();
      return null;
    }
  }

  /**
   * A private class that holds query name, file name and its corresponding rank.
   *
   * @author Dallas Fraser
   */
  private class Judgment {
    private final String queryName;
    private final Float rank;
    private final String fileName;

    /**
     * Class Constructor.
     *
     * @param line a line of the expect results line in a certain format
     * @throws Exception - line was not the expected size
     */
    public Judgment(String line) throws Exception {
      final String[] parts = line.split(" ");
      if (parts.length < 4) {
        throw new JudgmentException("Parsing error when loading results");
      }
      this.queryName = parts[0];
      this.fileName = parts[2];
      this.rank = Float.parseFloat(parts[3]);
    }

    /**
     * Returns the rank of the judgment.
     *
     * @return Float the rank
     */
    public Float getRank() {
      return this.rank;
    }

    /**
     * Checks if Judgment is equal to the query.
     *
     * @param q the query to compare to
     * @return boolean
     */
    public boolean equals(MathQuery q) {
      return this.queryName.equals(q.getQueryName());
    }

    /**
     * Checks if a judgment is equal to a file name.
     *
     * @param f the filename to compare to
     * @return boolean
     */
    public boolean equals(String f) {
      return this.fileName.equals(f);
    }

    /*
     * Prints a query to a String, with <code>field</code> assumed to be the
     * default field and omitted
     * (non-Javadoc)
     * @see org.apache.lucene.search.Query#toString(java.lang.String)
     * @return the string representation
     */
    @Override
    public String toString() {
      return "Query Name: " + this.queryName + " Filename:" + this.fileName;
    }
  }

  /**
   * An exception thrown when parsing a file.
   *
   * @author Dallas Fraser
   */
  private class JudgmentException extends Exception {
    private static final long serialVersionUID = 1L;

    public JudgmentException(String string) {
      super(string);
    }
  }
}
