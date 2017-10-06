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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utilities.Constants;
import utilities.ProjectLogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.w3c.dom.Element;


public class MathQuery {
    private String queryName;
    private ArrayList<String> terms;
    private Logger logger;
    private String fieldName;
    private static final String BOOSTSYMBOL = "^";
    
    public MathQuery(String queryName){
        this.terms = new ArrayList<String>();
        this.queryName = queryName;
        this.logger = ProjectLogger.getLogger();
        this.fieldName = Constants.FIELD;
    }

    public MathQuery(Node node){
        this.fieldName = Constants.FIELD;
        this.logger = ProjectLogger.getLogger();
        this.terms = new ArrayList<String>();
        this.logger.log(Level.FINEST, "Node Type:" + node.getNodeType());
        Element element = (Element) node;
        this.queryName = element.getElementsByTagName("num").item(0).getTextContent();
        NodeList termList =  element.getElementsByTagName("query").item(0).getChildNodes();
        this.logger.log(Level.FINEST, "Term List: " + termList.getLength());
        for (int j = 0; j < termList.getLength(); j++){
            this.logger.log(Level.FINEST, "Term List Element: " + j);
            Node nNode = termList.item(j);
            if (nNode.getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element) nNode;
                String term = e.getTextContent().replaceAll("\n", "").replaceAll("\t", "").trim();
                this.logger.log(Level.FINEST, "Text Content:" + term);
                this.terms.add(term);
            }else{
                this.logger.log(Level.FINEST, "Node Type:" + nNode.getNodeType() + " " + nNode.getNodeName());
            }
        }
    }

    public void addTerm(String term){
        this.terms.add(term);
    }

    public String getFieldName(){
        return this.fieldName;
    }

    public ArrayList<String> getTerms(){
        return this.terms;
    }

    public String getQueryName(){
        return this.queryName;
    }

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
        return "Name:" + this.queryName + "\nSearch Terms: \n" + String.join("\n", this.terms);
    }

    public ArrayList<String> uniqueTerms(String[] terms){
        String termsString = String.join(" ", terms);
        ArrayList<String> boostedTerms = new ArrayList<String>();
        for (String term : terms){
            if (!boostedTerms.contains(term)){
                boostedTerms.add(term);
            }
        }
        return boostedTerms;
    }

    public Query buildQuery(String[] terms, String field, BooleanQuery.Builder bq, boolean synonym){
        return this.buildQuery(terms, field, bq, synonym, false);
    }
    
    
    
    public Query buildQuery(String[] terms, String field, BooleanQuery.Builder bq, boolean synonym, boolean dice){
        // check if synonyms were indexed or not
        float boost;
        BoostQuery booster;
        String termsString = String.join(" ", terms);
        ArrayList<String> uniqueTerms = this.uniqueTerms(terms);
        if (!synonym){
            WildcardQuery tempQuery = null;
            for (String term : uniqueTerms){
                term = term.trim(); 
                if (!term.equals("")){
                    tempQuery = new WildcardQuery(new Term(field, term));
                    if (dice){
                        bq.add(new DiceQuery(tempQuery, uniqueTerms), BooleanClause.Occur.SHOULD);
                    }else{
                        boost = StringUtils.countMatches(termsString, term);
                        booster = new BoostQuery(tempQuery, boost);
                        bq.add(booster, BooleanClause.Occur.SHOULD);
                    }
                    
                }
            }
            if (tempQuery == null){
                bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
            }
        }else{
            TermQuery tempQuery = null;
            for(String term : uniqueTerms){
                term = term.trim();
                if(!term.equals("")){
                    tempQuery = new TermQuery(new Term(field, term));
                    if (dice){
                        bq.add(new DiceQuery(tempQuery, uniqueTerms), BooleanClause.Occur.SHOULD);
                    }else{
                        boost = StringUtils.countMatches(termsString, term);
                        booster = new BoostQuery(tempQuery, boost);
                        bq.add(booster, BooleanClause.Occur.SHOULD);
                    }
                }
            }
            if (tempQuery == null){
                bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
            }
        }
        return bq.build();
    }
}
