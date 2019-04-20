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

package utilities;

import index.JustMathAnalyzer;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import query.TermCountPair;
import utilities.Payload.PayloadException;

/**
 * Holds some common functions for the project.
 *
 * @author Dallas Fraser
 * @since 2017-11-09
 */
public class Functions {
  /**
   * Returns the name of the document.
   *
   * @param title the title which may include the filepath
   * @return String the name of Document (formula id removed)
   */
  public static String parseDocumentName(String title) {
    final String[] parts = title.split("/|\\\\");
    final String filename = parts[parts.length - 1];
    String[] temp = filename.split("\\.");
    String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
    // remove formula part
    temp = String.join(".", nameparts).split("-");
    if (temp.length > 1) {
      nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
    } else {
      nameparts = Arrays.copyOfRange(temp, 0, temp.length);
    }

    return String.join(".", nameparts);
  }

  /**
   * Parses a title.
   *
   * @param title the title which may include the file path
   * @return String the name of the document
   */
  public static String parseTitle(String title) {
    final String[] parts = title.split("/|\\\\");
    final String filename = parts[parts.length - 1];
    final String[] temp = filename.split("\\.");
    final String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
    return String.join(".", nameparts);
  }

  /**
   * Creates a temp file path used when converting the MathML with Tangent.
   *
   * @param file the file path
   * @return Path a path to the temp file
   */
  public static Path createtempFile(Path file) {
    final String title = parseTitle(file.getFileName().toString());
    final String[] fn = file.getFileName().toString().split("\\.");
    final String filenameExtension = fn[fn.length - 1];
    final String new_filename = title + Constants.TEMP_EXT + "." + filenameExtension;
    final Path new_path = Paths.get(file.getParent().toString(), new_filename);
    return new_path;
  }

  /**
   * Prints the map.
   *
   * @param map the map to print
   */
  public static void printMap(Map<Float, Float> map) {
    for (final Map.Entry<Float, Float> entry : map.entrySet()) {
      System.out.println(entry.getKey() + " : " + entry.getValue());
    }
  }

  /**
   * Check whether the term contains a wildcard.
   *
   * @param term the term to check
   * @return boolean True if term contains a wildcard, False otherwise
   */
  public static boolean containsWildcard(String term) {
    boolean wildcard = false;
    if (term.contains("'" + Constants.WILDCARD + "'")) {
      // need to add quotes ' around Wildcard to make sure not to consider /*
      // e.g. ('/*', '*', 'n') should be a wildcard
      // but ('/*', 'v!x', 'n') should not be a wildcard
      wildcard = true;
    }
    return wildcard;
  }

  /**
   * Returns a list of tokens using a the given Analyzer.
   *
   * @param analyzer the analyzer to use
   * @param field the field used by analyzer when parsing the tokens
   * @param queryText the query to analyze
   * @return List the list of tokens
   */
  public static List<String> analyzeTokens(Analyzer analyzer, String field, String queryText) {
    // Use the analyzer to get all the tokens, and then build an appropriate
    // query based on the analysis chain.
    final List<String> tokens = new ArrayList<String>();
    char[] token;
    try (TokenStream source = analyzer.tokenStream(field, queryText)) {
      final CharTermAttribute charAtt = source.getAttribute(CharTermAttribute.class);
      source.reset();
      while (source.incrementToken()) {
        token = Arrays.copyOfRange(charAtt.buffer(), 0, charAtt.length());
        tokens.add(new String(token));
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error analyzing query text", e);
    }
    return tokens;
  }

  /**
   * Returns the count of math tuples.
   *
   * @param text the document text to count the tuples of
   * @return int the number of tuples
   */
  public static int countTuples(String text) {
    int count = 0;
    for (final String term : text.split(" ")) {
      if ((term.startsWith("#") && term.endsWith("#"))
          || (term.startsWith("(") && term.endsWith(")"))) {
        if (!term.contains(Constants.FORMULA_START_TAG)
            && !term.contains(Constants.FORMULA_END_TAG)) {
          count += 1;
        }
      }
    }
    return count;
  }

  /**
   * Parses the formulas of a query text.
   *
   * @param queryText the query text String
   * @return List a list of String
   */
  public static List<String> parseFormulas(String queryText) {
    final List<String> formulas = new ArrayList<String>();
    // seperate the different math formulas
    final Analyzer analyzer = new JustMathAnalyzer();
    int pos = 0;
    String tempFormula;
    while (queryText.indexOf(Constants.FORMULA_END_TAG) != -1) {
      // need to add two because #
      if (queryText.indexOf("#" + Constants.FORMULA_END_TAG + "#") == -1) {
        pos = queryText.indexOf(Constants.FORMULA_END_TAG) + Constants.FORMULA_END_TAG.length();
      } else {
        pos = queryText.indexOf(Constants.FORMULA_END_TAG) + Constants.FORMULA_END_TAG.length() + 1;
      }
      tempFormula = queryText.substring(0, pos);
      queryText = queryText.substring(pos);
      formulas.add(String.join(" ", analyzeTokens(analyzer, Constants.MATHFIELD, tempFormula)));
    }
    return formulas;
  }

  /**
   * Returns a string by parsing the file.
   *
   * @param path - the file name path to to get the String for
   * @return String the String of the file
   * @throws IOException - issue while dealing with a dile
   */
  public static String parseString(Path path) throws IOException {
    final String contents = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    return contents;
  }

  /**
   * Returns the length of the document.
   *
   * @param analyzer the analyzer to use
   * @param field the field used for indexing
   * @param reader the file reader to calculate length of
   * @return int - the document length
   */
  public static int documentLength(Analyzer analyzer, String field, Reader reader) {
    int length = 0;
    try (TokenStream source = analyzer.tokenStream(field, reader)) {
      source.reset();
      while (source.incrementToken()) {
        length += 1;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error analyzing file", e);
    }
    return length;
  }

  /**
   * Returns a list of TermCountPair based upon analyzing the query string with the given analyzer .
   *
   * @param analyzer the analyzer to use
   * @param field the field used by analyzer when parsing the tokens
   * @param queryText the query to analyze
   * @return List a list of TermCountPair
   * @throws IOException - issue while dealing with file
   */
  public static List<TermCountPair> getTermCountPair(
      Analyzer analyzer, String field, String queryText) throws IOException {
    final List<TermCountPair> tokens = new ArrayList<TermCountPair>();
    String token;
    int index;
    try (TokenStream source = analyzer.tokenStream(field, queryText)) {
      final CharTermAttribute charAtt = source.getAttribute(CharTermAttribute.class);
      final PayloadAttribute payloadAtt = source.getAttribute(PayloadAttribute.class);
      source.reset();
      while (source.incrementToken()) {
        token = String.valueOf(Arrays.copyOfRange(charAtt.buffer(), 0, charAtt.length()));
        index = tokens.indexOf(new TermCountPair(token));
        // in not found then need to add it other just increment it
        if (index != -1) {
          tokens.get(index).increment();
        } else {
          index = tokens.size();
          tokens.add(new TermCountPair(token.toString()));
        }
        // check for the payload
        try {
          if (payloadAtt.getPayload() != null) {
            tokens.get(index).addPayload(new Payload(payloadAtt.getPayload()));
          }
        } catch (final PayloadException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return tokens;
  }

  /**
   * Scores a formula using Tompa approach (BM25 where TF=1 and n(qi) = rank(f)).
   *
   * @param docLength the doc length
   * @param totalDoc the total number of documents
   * @param avgDocLength average document length
   * @param rank the rank of the formula in the query
   * @param valueK1 - the value of the K1
   * @param valueB - the value of B
   * @param docScore - the score of the document
   * @return Double the formula score
   * @throws NumberFormatException - issue when parsing with file
   * @throws IOException - issue while dealing with file
   */
  public static Double scoreFormula(
      double docLength,
      double totalDoc,
      double avgDocLength,
      double rank,
      double valueK1,
      double valueB,
      double docScore) {
    final double idf = Math.log((totalDoc - rank + 0.5) / (rank + 0.5));
    final double tf =
        ((docScore * (valueK1 + 1))
            / (docScore + valueK1 * (1 - valueB + valueB * (docLength / avgDocLength))));
    System.out.println(
        "r(rank="
            + rank
            + ", docLength="
            + docLength
            + ", totalDoc="
            + totalDoc
            + ", avgDL="
            + avgDocLength
            + ", docScore="
            + docScore
            + ") "
            + idf
            + " X "
            + tf
            + " = "
            + (idf * tf));
    return (idf * tf);
  }
}
