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
 * Holds some common functions for the project
 * @author Dallas Fraser
 * @since 2017-11-09
 */
public class Functions {
    /**
     * Parses a title
     * @param title the title which may include the filepath
     * @return String the name of the document
     */
    public static String parseTitle(String title){
        String[] parts = title.split("/|\\\\");
        String filename = parts[parts.length -1];
        String[] temp = filename.split("\\.");
        String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
        return String.join(".", nameparts);
    }

    /**
     * Creates a temp file path used when converting the MathML with Tangent 
     * @param file the file path
     * @return Path a path to the temp file
     */
    public static Path createtempFile(Path file){
        String title = parseTitle(file.getFileName().toString());
        String[] fn = file.getFileName().toString().split("\\.");
        String filenameExtension = fn[fn.length - 1];
        String new_filename = title + "_temp." + filenameExtension;
        Path new_path = Paths.get(file.getParent().toString(), new_filename);
        return new_path;
        
    }

    /**
     * Prints the map
     * @param map the map to print
     */
    public static void printMap(Map<Float, Float> map){
        for (Map.Entry<Float, Float> entry : map.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
    }

    /**
     * Check whether the term contains a wildcard
     * @param term the term to check
     * @return boolean True if term contains a wildcard, False otherwise
     */
    public static boolean containsWildcard(String term){
        boolean wildcard = false;
        if (term.contains("'" + Constants.WILDCARD + "'")){
            // need to add quotes ' around Wildcard to make sure not to consider /*
            // e.g. ('/*', '*', 'n') should be a wildcard
            // but ('/*', 'v!x', 'n') should not be a wildcard
            wildcard = true;
        }
        return wildcard;
    }

    /**
     * Returns a list of tokens using a the given Analyzer
     * @param analyzer the analyzer to use
     * @param field the field used by analyzer when parsing the tokens
     * @param queryText the query to analyze
     * @return List the list of tokens
     */
    public static List<String> analyzeTokens(Analyzer analyzer, String field, String queryText){
        // Use the analyzer to get all the tokens, and then build an appropriate
        // query based on the analysis chain.
        List<String> tokens = new ArrayList<String>();
        char[]  token;
        try (TokenStream source = analyzer.tokenStream(field, queryText)) {
            CharTermAttribute charAtt = source.getAttribute(CharTermAttribute.class);
            source.reset();
            while (source.incrementToken()) {
                token = Arrays.copyOfRange(charAtt.buffer(), 0, charAtt.length());
                tokens.add(new String(token));
            }
        } catch (IOException e) {
          throw new RuntimeException("Error analyzing query text", e);
        }
        return tokens;
    }

    /**
     * Returns a string by parsing the file using the analyzer
     * @param analyzer the analyzer to use
     * @param reader the reader of the file
     * @return String the String of the file
     * @throws IOException 
     */
    public static String parseString(Path path) throws IOException{
        String contents = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        return contents;
    }

    /**
     * Returns the length of the document
     * @param analyzer the analyzer to use
     * @param field the field used for indexing
     * @param reader the file reader to calculate length of
     * @return
     */
    public static int documentLength(Analyzer analyzer, String field, Reader reader){
        int length = 0;
        try(TokenStream source = analyzer.tokenStream(field, reader)){
            source.reset();
            while (source.incrementToken()){
                length += 1;
            }
        }catch (IOException e){
            throw new RuntimeException("Error analyzing file", e);
        }
        return length;
    }

    /**
     * Returns a list of TermCountPair based upon analyzing the query string with the given analyzer 
     * @param analyzer the analyzer to use
     * @param field the field used by analyzer when parsing the tokens
     * @param queryText the query to analyze
     * @return List a list of TermCountPair
     * @throws IOException
     */
    public static List<TermCountPair> getTermCountPair(Analyzer analyzer, String field, String queryText) throws IOException{
        List<TermCountPair> tokens = new ArrayList<TermCountPair>();
        String token;
        int index;
        try (TokenStream source = analyzer.tokenStream(field, queryText)){
            CharTermAttribute charAtt = source.getAttribute(CharTermAttribute.class);
            PayloadAttribute payloadAtt = source.getAttribute(PayloadAttribute.class);
            source.reset();
            while(source.incrementToken()){
                token = String.valueOf(Arrays.copyOfRange(charAtt.buffer(), 0, charAtt.length()));
                index = tokens.indexOf(new TermCountPair(token));
                // in not found then need to add it other just increment it
                if(index != -1){
                    tokens.get(index).increment();
                }else{
                    index = tokens.size();
                    tokens.add(new TermCountPair(token.toString()));
                }
                // check for the payload
                try {
                    if(payloadAtt.getPayload() != null){
                        tokens.get(index).addPayload(new Payload(payloadAtt.getPayload()));
                    }
                } catch (PayloadException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return tokens;
    }
}
