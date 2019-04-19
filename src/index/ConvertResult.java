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

package index;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import utilities.Functions;

/** Holds the result after converting MathML into Tangent Tuples. */
public class ConvertResult {
  private final String text;
  private final StringReader reader;
  private final int formulaCount;
  private final int wordCount;
  private final int docLength;

  /**
   * Constructor.
   *
   * @param outBuffer the buffer the holds the result of the conversion.
   */
  public ConvertResult(StringWriter outBuffer) {
    this.text = outBuffer.toString();
    this.reader = new StringReader(outBuffer.toString());
    this.formulaCount = Functions.countTuples(outBuffer.toString());
    this.docLength = outBuffer.toString().split(" ").length;
    this.wordCount = this.docLength - this.formulaCount;
  }

  /**
   * Getter for the number of formulas in the result.
   *
   * @return int - the number of formulas
   */
  public int getFormulaCount() {
    return this.formulaCount;
  }

  /**
   * Getter for the number of words in the result.
   *
   * @return int - the word count
   */
  public int getWordCount() {
    return this.wordCount;
  }

  /**
   * Getter for the length of the document.
   *
   * @return int - the length of the document
   */
  public int getDocLength() {
    return this.docLength;
  }

  /**
   * Getter for the reader of the text.
   *
   * @return Reader - a reader that contains the converted file
   */
  public Reader getReader() {
    return this.reader;
  }

  /**
   * Getter for the text.
   *
   * @return String - the converted text of the file
   */
  public String getText() {
    return this.text;
  }
}
