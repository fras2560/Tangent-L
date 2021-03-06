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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Analyzer that only uses text tokens.
 *
 * <p>{@link Analyzer} for English.
 */
public final class JustTextAnalyzer extends StopwordAnalyzerBase {
  private final CharArraySet stemExclusionSet;
  private ConvertConfig config;

  /**
   * Constructor.
   *
   * @param config - the config to use when converting
   */
  public JustTextAnalyzer(ConvertConfig config) {
    this(DefaultSetHolder.DEFAULT_STOP_SET);
    this.config = config;
  }

  /**
   * Returns an unmodifiable instance of the default stop words set.
   *
   * @return default stop words set.
   */
  public static CharArraySet getDefaultStopSet() {
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }

  /**
   * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer class accesses the
   * static final set the first time.;
   */
  private static class DefaultSetHolder {
    static final CharArraySet DEFAULT_STOP_SET = StandardAnalyzer.STOP_WORDS_SET;
  }

  /**
   * Builds an analyzer with the default stop words.
   *
   * <p>{@link #getDefaultStopSet}.
   */
  public JustTextAnalyzer() {
    this(DefaultSetHolder.DEFAULT_STOP_SET);
    this.config = new ConvertConfig();
  }

  /**
   * Builds an analyzer with the given stop words.
   *
   * @param stopwords a stopword set
   */
  public JustTextAnalyzer(CharArraySet stopwords) {
    this(stopwords, CharArraySet.EMPTY_SET);
    this.config = new ConvertConfig();
  }

  /**
   * Builds an analyzer with the given stop words. If a non-empty stem exclusion set is provided
   * this analyzer will add a {@link SetKeywordMarkerFilter} before stemming.
   *
   * @param stopwords a stopword set
   * @param stemExclusionSet a set of terms not to be stemmed
   */
  public JustTextAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet) {
    super(stopwords);
    this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
    this.config = new ConvertConfig();
  }

  /**
   * Creates a {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} which tokenizes all
   * the text in the provided {@link Reader}.
   *
   * @return A {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} built from an
   *     {@link StandardTokenizer} filtered with {@link StandardFilter}, {@link
   *     EnglishPossessiveFilter}, {@link LowerCaseFilter}, {@link StopFilter} , {@link
   *     SetKeywordMarkerFilter} if a stem exclusion set is provided and {@link PorterStemFilter}.
   */
  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    final Tokenizer source = new MathTokenizer();
    TokenStream result = new ClassicFilter(source);
    result = new StandardFilter(result);
    result = new EnglishPossessiveFilter(result);
    result = new LowerCaseFilter(result);
    result = new StopFilter(result, this.stopwords);
    if (!this.stemExclusionSet.isEmpty()) {
      result = new SetKeywordMarkerFilter(result, this.stemExclusionSet);
    }
    if (this.config.getAttribute(ConvertConfig.BAGS_OF_WORDS)) {
      result = new MathBagsFilter(result);
    }
    result = new MathRemoveTagsFilter(result);
    if (this.config.getAttribute(ConvertConfig.SYNONYMS)) {
      result = new MathSynonymFilter(result);
    }
    // parse out any payloads that are added
    result = new PayloadFilter(result);
    result = new PorterStemFilter(result);
    // remove math tuples
    result = new RemoveMathFilter(result);
    return new TokenStreamComponents(source, result);
  }

  @Override
  protected TokenStream normalize(String fieldName, TokenStream in) {
    TokenStream result = new StandardFilter(in);
    result = new LowerCaseFilter(result);
    return result;
  }
}
