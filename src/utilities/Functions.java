package utilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class Functions {

    public static String parseTitle(String title){
        String[] parts = title.split("/|\\\\");
        String filename = parts[parts.length -1];
        String[] temp = filename.split("\\.");
        String[] nameparts = Arrays.copyOfRange(temp, 0, temp.length - 1);
        return String.join(".", nameparts);
    }

    public static Path createtempFile(Path file){
        String title = parseTitle(file.getFileName().toString());
        String[] fn = file.getFileName().toString().split("\\.");
        String filenameExtension = fn[fn.length - 1];
        String new_filename = title + "_temp." + filenameExtension;
        Path new_path = Paths.get(file.getParent().toString(), new_filename);
        return new_path;
        
    }

    public static void printMap(Map<Float, Float> map){
        for (Map.Entry<Float, Float> entry : map.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }
    }

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
    public static String[] analyzeTokens(Analyzer analyzer, String field, String queryText){
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
        return tokens.toArray(new String[tokens.size()]);
    }
}
