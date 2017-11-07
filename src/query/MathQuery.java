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
    private String queryName;
    private ArrayList<String> terms;
    private ArrayList<String> analyzedTerms;
    private Logger logger;
    private String fieldName;
    private ConvertConfig config;
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
        this.queryName = queryName;
        this.logger = ProjectLogger.getLogger();
        this.fieldName = Constants.FIELD;
        this.config = config;
    }

    /**
     * Class Constructor
     * @param node the xml node of the query used to parse the query terms and the math formula tuples
     * @param config the config to use
     */
    public MathQuery(Node node, ConvertConfig config){
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
        for (int j = 0; j < termList.getLength(); j++){
            this.logger.log(Level.FINEST, "Term List Element: " + j);
            Node nNode = termList.item(j);
            if (nNode.getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element) nNode;
                String term = e.getTextContent().replaceAll("\n", "").replaceAll("\t", "").trim();
                this.logger.log(Level.FINEST, "Text Content:" + term);
                for (String t : Functions.analyzeTokens(analyzer, Constants.FIELD, term)){
                    this.terms.add(t);
                }
            }else{
                this.logger.log(Level.FINEST, "Node Type:" + nNode.getNodeType() + " " + nNode.getNodeName());
            }
        }
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
     * @param the field of the term
     */
    public void addTerm(String term, String field){
        for(String t: Functions.analyzeTokens(new MathAnalyzer(this.config), field, term)){
            this.terms.add(t);
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
     * @return ArrayList<String> the list of terms
     */
    public ArrayList<String> getTerms(){
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

    /**
     * Returns a list of uniqueTerms and their counts
     * @param terms the terms to check
     * @return ArrayList<TermCountPair> the unique terms and their counts
     */
    public ArrayList<TermCountPair> uniqueTerms(ArrayList<String> terms){
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
     * @param terms the terms of the query
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
                            ConvertConfig config) throws IOException{
        BoostQuery booster;
        ArrayList<TermCountPair> uniqueTerms = this.uniqueTerms(this.terms);
        TermQuery tempQuery = null;
        WildcardQuery wTempQuery = null;
        if (!synonym){
            wTempQuery = null;
            for (TermCountPair termPair : uniqueTerms){
                if (!termPair.getTerm().trim().equals("")){
                    wTempQuery = new WildcardQuery(new Term(field, termPair.getTerm().trim()));
                    if (!config.getAttribute(ConvertConfig.BOOST_QUERIES)){
                        bq.add(wTempQuery, BooleanClause.Occur.SHOULD);
                    }else{
                        booster = new BoostQuery(wTempQuery, termPair.getCount());
                        bq.add(booster, BooleanClause.Occur.SHOULD);
                    }
                }
            }
        }else{
            tempQuery = null;
            for(TermCountPair termPair : uniqueTerms){
                if(!termPair.getTerm().trim().equals("")){
                    tempQuery = new TermQuery(new Term(field, termPair.getTerm().trim()));
                    if (!config.getAttribute(ConvertConfig.BOOST_QUERIES)){
                        bq.add(tempQuery, BooleanClause.Occur.SHOULD);
                    }else{
                        booster = new BoostQuery(tempQuery, termPair.getCount());
                        bq.add(booster, BooleanClause.Occur.SHOULD);
                    }
                }
            }
        }
        if (tempQuery ==  null || wTempQuery == null){
            bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
        }
        Query result = bq.build();
        return (Query) (new MathScoreQuery(result, uniqueTerms, field, config));
    }
}
