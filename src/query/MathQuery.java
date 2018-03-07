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
package query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import index.ConvertConfig;
import index.MathAnalyzer;
import utilities.Constants;
import utilities.Functions;
import utilities.ProjectLogger;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.w3c.dom.Element;

/**
 * A class to use to create a math query
 * @author Dallas Fraser
 * @since 2017-11-07
 */
public class MathQuery {
    public static float WILDCARD_BOOST = 1.0f;
    private String queryName;
    private List<String> formulas;
    private ArrayList<String> terms;
    private ArrayList<String> analyzedTerms;
    private Logger logger;
    private String fieldName;
    private ConvertConfig config;
    private List<String> phrases;
    private int mathCount;
    /**
     * Class Constructor
     * @param queryName the name of the query
     * 
     */
    public MathQuery(String queryName){
        this(queryName, new ConvertConfig());
    }

    /**
     * Class Constructor
     * @param queryName the name of query
     * @param config the name of the config file
     */
    public MathQuery(String queryName, ConvertConfig config){
        this.terms = new ArrayList<String>();
        this.formulas = new ArrayList<String>();
        this.phrases = new ArrayList<String>();
        this.queryName = queryName;
        this.logger = ProjectLogger.getLogger();
        this.fieldName = Constants.FIELD;
        this.config = config;
        this.mathCount = 0;
    }

    /**
     * Class Constructor
     * @param node the xml node of the query used to parse the query terms and the math formula tuples
     * @param config the config to use
     */
    public MathQuery(Node node, ConvertConfig config){
        this.formulas = new ArrayList<String>();
        this.phrases = new ArrayList<String>();
        this.config = config;
        this.fieldName = Constants.FIELD;
        this.logger = ProjectLogger.getLogger();
        this.terms = new ArrayList<String>();
        this.logger.log(Level.FINEST, "Node Type:" + node.getNodeType());
        Element element = (Element) node;
        this.queryName = element.getElementsByTagName("num").item(0).getTextContent();
        NodeList termList =  element.getElementsByTagName("query").item(0).getChildNodes();
        this.logger.log(Level.FINEST, "Term List: " + termList.getLength());
        MathAnalyzer analyzer = new MathAnalyzer(this.config);
        String phrase;
        for (int j = 0; j < termList.getLength(); j++){
            this.logger.log(Level.FINEST, "Term List Element: " + j);
            Node nNode = termList.item(j);
            if (nNode.getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element) nNode;
                String term = e.getTextContent().replaceAll("\n", "").replaceAll("\t", "").trim();
                this.logger.log(Level.FINEST, "Text Content:" + term);
                phrase = "";
                for (String t : Functions.analyzeTokens(analyzer, Constants.FIELD, term)){
                    phrase = phrase + " " + t;
                    this.terms.add(t);
                    if(this.isTermMath(t)){
                        this.mathCount += 1;
                    }
                }
                phrase = phrase.trim();
                // if a phrase has two or more keywords and is not a math tuple
                if(phrase.length() - phrase.replace(" ", "").length() > 0 && !phrase.startsWith("(")){
                    this.phrases.add(phrase);
                }
                this.formulas.addAll(Functions.parseFormulas(term));
            }else{
                this.logger.log(Level.FINEST, "Node Type:" + nNode.getNodeType() + " " + nNode.getNodeName());
            }
        }
    }

    /**
     * Returns a list of phrases that were listed as keywords
     * @return
     */
    public List<String> getPhrases(){
        return this.phrases;
    }

    /**
     * Checks where a term is a math tuple or not
     * @param term the term the check
     * @return boolean True if term is a math tuple
     */
    public boolean isTermMath(String term){
        boolean mathTerm = false;
        term = term.trim();
        if(term.startsWith("(") && term.endsWith(")")){
            mathTerm = true;
        }
        return mathTerm;
    }

    /**
     * Adds a term to the query parses the term using MathAnalyzer.
     * @param term the term to add
     */
    public void addTerm(String term){
        this.addTerm(term, Constants.FIELD);
        
    }

    /**
     * Adds a term to the query parses the term using MathAnalyzer.
     * @param term the term to add
     * @param field of the term
     */
    public void addTerm(String term, String field){
        for(String t: Functions.analyzeTokens(new MathAnalyzer(this.config), field, term)){
            this.terms.add(t);
            if(this.isTermMath(t)){
                this.mathCount += 1;
            }
        }
    }
    /**
     * Get the field name of the query
     * @return String the field name
     */
    public String getFieldName(){
        return this.fieldName;
    }

    /**
     * Returns a list of terms
     * @return ArrayList the list of terms
     */
    public List<String> getTerms(){
        return this.terms;
    }

    /**
     * Returns the name of the query
     * @return String the name of the query
     */
    public String getQueryName(){
        return this.queryName;
    }

    /**
     * Returns a String representation of the query
     * @return String the query
     */
    public String getQuery(){
        return String.join(" ", this.terms);
    }

    /**
     * Returns the number of math tuples in the query
     * @return int the number of math tuples
     */
    public int getMathCount(){
        return this.mathCount;
    }

    /*
     * Prints a query to a String, with <code>field</code> assumed to be the
     * default field and omitted
     * (non-Javadoc)
     * @see org.apache.lucene.search.Query#toString(java.lang.String)
     * @return the string representation
     */
    public String toString(String field){
        String result;
        result = "Name:" + this.queryName + "\nSearch Terms: \n" + String.join("\n", this.terms);
        return result;
    }

    /*
     * Prints a query to a String, with <code>field</code> assumed to be the
     * default field and omitted
     * (non-Javadoc)
     * @see org.apache.lucene.search.Query#toString(java.lang.String)
     * @return the string representation
     */
    public String toString(){
        String result;
        result = "Name:" + this.queryName + "\nSearch Terms: \n" + String.join("\n", this.terms);
        return result;
    }

    /**
     * Returns a list of uniqueTerms and their counts
     * @param terms the terms to check
     * @return ArrayList the unique terms and their counts
     */
    public List<TermCountPair> uniqueTerms(ArrayList<String> terms){
        ArrayList<TermCountPair> boostedTerms = new ArrayList<TermCountPair>();
        for (String term : terms){
            if (!term.trim().equals("")){
                int pos = 0;
                boolean found = false;
                for (TermCountPair tcp : boostedTerms){
                    if (tcp.equals(term)){
                        found = true;
                        break;
                    }
                    pos += 1;
                }
                if (!found){
                    boostedTerms.add(new TermCountPair(term));
                }else{
                    boostedTerms.get(pos).increment();
                }
            }
        }
        return boostedTerms;
    }

    /**
     * Use the analyzer to parse the query terms
     * @param field the field to query
     * @param config the config to use
     * @throws IOException
     */
    public void setAnalyzedTerms(String field, ConvertConfig config) throws IOException{
        String queryText = String.join(" ", this.terms);
        MathAnalyzer analyzer = new MathAnalyzer(config);
        try (TokenStream source = analyzer.tokenStream(field, queryText)) {
            TermToBytesRefAttribute termAtt = source.getAttribute(TermToBytesRefAttribute.class);
            source.reset();
            while (source.incrementToken()) {
              this.analyzedTerms.add(termAtt.getBytesRef().utf8ToString());
            }
        }
        analyzer.close();
    }

    /**
     * Returns a Lucene Query
     * @param field the field to search
     * @param bq build a boolean query
     * @param synonym whether the index includes synonym or not
     * @param config the config file to use
     * @return Query the to be used for Lucene
     * @throws IOException 
     */
    public Query buildQuery(String field,
                            BooleanQuery.Builder bq,
                            boolean synonym,
                            ConvertConfig config,
                            CollectionStatistics stats) throws IOException{
        List<TermCountPair> uniqueTerms = this.uniqueTerms(this.terms);
        Query tempQuery = null;
        String tfield;
        for (TermCountPair termPair : uniqueTerms){
            if (config.getAttribute(ConvertConfig.SEPERATE_MATH_TEXT)){
                // want our query to seperate math from the texts
                if (termPair.getTerm().startsWith("(")){
                    tfield = Constants.MATHFIELD;
                }else{
                    tfield = Constants.TEXTFIELD;
                }
            }else{
                // want to query on them being in the same field
                tfield = field;
            }
            if(termPair.getTerm().trim().startsWith("(") && config.getMathBM25()){
                tempQuery = new MathTermQuery(new Term(tfield, termPair.getTerm().trim()), (int) termPair.getCount());
            }else{
                tempQuery = new TermQuery(new Term(tfield, termPair.getTerm().trim()));
            }
            if (!synonym && Functions.containsWildcard(termPair.getTerm())){
                // do not have synonyms indexed so use wildcard query
                // this term has a wildcard so need it for match wildcard
                tempQuery = new WildcardQuery(new Term(tfield, termPair.getTerm().trim()));
                // boost the wildcard
                tempQuery = new BoostQuery(tempQuery, MathQuery.WILDCARD_BOOST);
            }
            if (config.getAttribute(ConvertConfig.BOOST_QUERIES)){
                // boost the term by the number of times it appears in the query
                tempQuery =  new BoostQuery(tempQuery, termPair.getCount());
            }
            if (config.getAttribute(ConvertConfig.BOOST_LOCATION)){
                // boost the term if location matches
                tempQuery = new LocationBoostedQuery(tempQuery, termPair, field);
            }
            // add the query
            bq.add(tempQuery, BooleanClause.Occur.SHOULD);
        }
        // try phrase queries
        if(config.getAttribute(ConvertConfig.PROXIMITY)){
            for(String phrase :  this.phrases){
                System.out.println("Added Phrase Query: " + phrase);
                bq.add(new PhraseQuery(5, field, phrase.split(" ")), BooleanClause.Occur.SHOULD);
            }
        }
        if (tempQuery ==  null){
            bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
        }
        Query result = bq.build();
        return (Query) (new MathScoreQuery(result, uniqueTerms, field, config, stats));
    }

    /**
     * Build a query of just text
     * @param field the field to query
     * @param bq the boolean query builder
     * @return Query the text only query
     * @throws IOException
     */
    public Query buildTextQuery(String field,
                                BooleanQuery.Builder bq)throws IOException{
        Query tempQuery = null;
        for (String term : this.terms){
            if(!term.startsWith("(")){
                tempQuery = new TermQuery(new Term(field, term.trim()));
                bq.add(tempQuery, BooleanClause.Occur.SHOULD);
            }
        }
        if (tempQuery ==  null){
            bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
        }
        return bq.build();
    }

    public Query buildMustWeightedQuery(String field,
                                        BooleanQuery.Builder bq,
                                        boolean synonym,
                                        ConvertConfig config,
                                        CollectionStatistics stats,
                                        float mathWeight,
                                        float textWeight) throws IOException{
        BooleanQuery.Builder textBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder mathBuilder = new BooleanQuery.Builder();
        List<TermCountPair> uniqueTerms = this.uniqueTerms(this.terms);
        Query tempQuery = null;
        String tfield;
        int mathCount = 0;
        int textCount = 0;
        for (TermCountPair termPair : uniqueTerms){
            if (config.getAttribute(ConvertConfig.SEPERATE_MATH_TEXT)){
                // want our query to seperate math from the texts
                if (termPair.getTerm().startsWith("(")){
                    tfield = Constants.MATHFIELD;
                }else{
                    tfield = Constants.TEXTFIELD;
                }
            }else{
                // want to query on them being in the same field
                tfield = field;
            }
            if(termPair.getTerm().trim().startsWith("(") && config.getMathBM25()){
                // if a math term and using an adjusted math bm25
                tempQuery = new MathTermQuery(new Term(tfield, termPair.getTerm().trim()), (int) termPair.getCount());
            }else{
                tempQuery = new TermQuery(new Term(tfield, termPair.getTerm().trim()));
            }
            if (!synonym && Functions.containsWildcard(termPair.getTerm())){
                // do not have synonyms indexed so use wildcard query
                // this term has a wildcard so need it for match wildcard
                tempQuery = new WildcardQuery(new Term(tfield, termPair.getTerm().trim()));
                // boost the wildcard
                tempQuery = new BoostQuery(tempQuery, MathQuery.WILDCARD_BOOST);
            }
            if (config.getAttribute(ConvertConfig.BOOST_QUERIES)){
                // boost the term by the number of times it appears in the query
                tempQuery =  new BoostQuery(tempQuery, termPair.getCount());
            }
            if (config.getAttribute(ConvertConfig.BOOST_LOCATION)){
                // boost the term if location matches
                tempQuery = new LocationBoostedQuery(tempQuery, termPair, field);
            }
            if(termPair.getTerm().trim().startsWith("(")){
                tempQuery = new BoostQuery(tempQuery, mathWeight);
                mathBuilder.add(tempQuery, BooleanClause.Occur.SHOULD);
                mathCount += 1;
            }else{
                tempQuery = new BoostQuery(tempQuery, textWeight);
                textBuilder.add(tempQuery, BooleanClause.Occur.SHOULD);
                textCount += 1;
            }
            // add the query
            // bq.add(tempQuery, BooleanClause.Occur.SHOULD);
        }
        // try phrase queries
        if(config.getAttribute(ConvertConfig.PROXIMITY)){
            for(String phrase :  this.phrases){
                bq.add(new PhraseQuery(5, field, phrase.split(" ")), BooleanClause.Occur.SHOULD);
            }
        }
        if (tempQuery ==  null){
            bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
        }else{
            if(textCount > 0 && mathCount > 0){
                mathBuilder.setMinimumNumberShouldMatch((int) Math.ceil(mathCount));
                textBuilder.setMinimumNumberShouldMatch((int) Math.ceil(textCount));
                bq.add(mathBuilder.build(), BooleanClause.Occur.MUST);
                bq.add(textBuilder.build(), BooleanClause.Occur.MUST);
            }else if(textCount == 0){
                // just search for math
                bq.add(mathBuilder.build(), BooleanClause.Occur.SHOULD);
            }else if(mathCount == 0){
                bq.add(textBuilder.build(), BooleanClause.Occur.SHOULD);
            }
        }
        Query result = bq.build();
        return (Query) (new MathScoreQuery(result, uniqueTerms, field, config, stats));
    }

    /**
     * Builds a Lucene Query that has differnet weights for text and math tuples
     * @param field the Lucene field being queries
     * @param bq the booleanquery builder
     * @param synonym whether included synonyms when indexing
     * @param config config file to use
     * @param stats the collection statistics (used for various types of queries)
     * @param alpha the math weight
     * @param beta the text weight
     * @return Query a Lucene Query
     * @throws IOException
     */
    public Query buildWeightedQuery(String field,
                                    BooleanQuery.Builder bq,
                                    boolean synonym,
                                    ConvertConfig config,
                                    CollectionStatistics stats,
                                    float alpha,
                                    float beta) throws IOException{
        List<TermCountPair> uniqueTerms = this.uniqueTerms(this.terms);
        Query tempQuery = null;
        String tfield;
        for (TermCountPair termPair : uniqueTerms){
            if (config.getAttribute(ConvertConfig.SEPERATE_MATH_TEXT)){
                // want our query to seperate math from the texts
                if (termPair.getTerm().startsWith("(")){
                    tfield = Constants.MATHFIELD;
                }else{
                    tfield = Constants.TEXTFIELD;
                }
            }else{
                // want to query on them being in the same field
                tfield = field;
            }
            if(termPair.getTerm().trim().startsWith("(") && config.getMathBM25()){
                // if a math term and using an adjusted math bm25
                tempQuery = new MathTermQuery(new Term(tfield, termPair.getTerm().trim()), (int) termPair.getCount());
            }else{
                tempQuery = new TermQuery(new Term(tfield, termPair.getTerm().trim()));
            }
            if (!synonym && Functions.containsWildcard(termPair.getTerm())){
                // do not have synonyms indexed so use wildcard query
                // this term has a wildcard so need it for match wildcard
                tempQuery = new WildcardQuery(new Term(tfield, termPair.getTerm().trim()));
                // boost the wildcard
                tempQuery = new BoostQuery(tempQuery, MathQuery.WILDCARD_BOOST);
            }
            if (config.getAttribute(ConvertConfig.BOOST_QUERIES)){
                // boost the term by the number of times it appears in the query
                tempQuery =  new BoostQuery(tempQuery, termPair.getCount());
            }
            if (config.getAttribute(ConvertConfig.BOOST_LOCATION)){
                // boost the term if location matches
                tempQuery = new LocationBoostedQuery(tempQuery, termPair, field);
            }
            if(termPair.getTerm().trim().startsWith("(")){
                tempQuery = new BoostQuery(tempQuery, alpha);
            }else{
                tempQuery = new BoostQuery(tempQuery, beta);
            }
            // add the query
            bq.add(tempQuery, BooleanClause.Occur.SHOULD);
        }
        // try phrase queries
        if(config.getAttribute(ConvertConfig.PROXIMITY)){
            for(String phrase :  this.phrases){
                System.out.println("Added Phrase Query: " + phrase);
                bq.add(new PhraseQuery(5, field, phrase.split(" ")), BooleanClause.Occur.SHOULD);
            }
        }
        if (tempQuery ==  null){
            bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
        }
        Query result = bq.build();
        return (Query) (new MathScoreQuery(result, uniqueTerms, field, config, stats));
    }

    /**
     * Returns a list of Query for each formula
     * @param field the field of the query
     * @param synonym whether synonyms were used when indexing
     * @param config the config to used when querying
     * @return List the lst of formulas query
     */
    public List<Query> buildFormulaQuery(String field,
                                         boolean synonym,
                                         ConvertConfig config){
        // build a query for our custom approach for math queries
        List<Query> mathQueries = new ArrayList<Query>();
        for(String formula : this.formulas){
            System.out.println(formula);
            // build a math query
            Query mathQuery = null;
            Query tempQuery = null;
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            for(String tuple : formula.split(" ")){
                tempQuery = new TermQuery(new Term(field, tuple));
                if (!synonym && Functions.containsWildcard(tuple)){
                    // do not have synonyms indexed so use wildcard query
                    // this term has a wildcard so need it for match wildcard
                    tempQuery = new WildcardQuery(new Term(field, tuple.trim()));
                    // boost the wildcard
                    tempQuery = new BoostQuery(tempQuery, MathQuery.WILDCARD_BOOST);
                }
                // add the query
                bq.add(tempQuery, BooleanClause.Occur.SHOULD);
            }
            if (tempQuery ==  null){
                bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
            }
            mathQuery = (Query) bq.build();
            mathQueries.add(mathQuery);
        }
        return mathQueries;
    }
}
