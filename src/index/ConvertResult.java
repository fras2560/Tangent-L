package index;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import utilities.Functions;

public class ConvertResult {
    private String text;
    private StringReader reader;
    private int formulaCount;
    private int wordCount;
    private int docLength;
    public ConvertResult(StringWriter outBuffer){
        this.text = outBuffer.toString();
        this.reader = new StringReader(outBuffer.toString());
        this.formulaCount = Functions.countTuples(outBuffer.toString());
        this.docLength = outBuffer.toString().split(" ").length;
        this.wordCount = this.docLength - this.formulaCount;
    }

    public int getFormulaCount(){
        return this.formulaCount;
    }

    public int getWordCount(){
        return this.wordCount;
    }

    public int getDocLength(){
        return this.docLength;
    }

    public Reader getReader(){
        return this.reader;
    }

    public String getText(){
        return this.text;
    }
}
