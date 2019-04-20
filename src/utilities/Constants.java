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

/**
 * Holds some constants for the project.
 *
 * @author Dallas Fraser
 * @since 2017-11-09
 */
public class Constants {
  public static final String WILDCARD = "*";
  public static final String ESCAPED_WILDCARD = "/*";
  public static final String FIELD = "contents";
  public static final String MATHFIELD = "math-contents";
  public static final String TEXTFIELD = "text-contents";
  public static final String DOCUMENT_LENGTH = "documentLength";
  public static final String FORMULA_COUNT = "numberOfFormulas";
  public static final String WORD_COUNT = "numberOfWords";
  public static final String MAX_FORMULA_SIZE = "maxFormulaSize";
  public static final String AVERAGE_FORMULA_SIZE = "averageFormulaSize";
  public static final String FORMULA_PREFIX = "(";
  public static final String FORMULA_SUFFIX = ")";
  public static final String FORMULA_START_TAG = "(start)";
  public static final String FORMULA_END_TAG = "(end)";
  public static final String PAYLOAD_DELIMITER = "\\|__\\|";
  public static final String PAYLOAD_SEPARATOR = ":";
  public static final String DOCUMENT_SIZE = "DOCUMENT SIZE";
  public static final String TEMP_EXT = "_temp";
}
