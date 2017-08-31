package query;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.w3c.dom.Element;

public class MathQuery {
    private String queryName;
    private ArrayList<String> terms;
    public MathQuery(String queryName){
        this.terms = new ArrayList<String>();
        this.queryName = queryName;
    }
    public MathQuery(Node node){
        this.terms = new ArrayList<String>();
        System.out.println("Node Type:" + node.getNodeType());
        Element element = (Element) node;
        this.queryName = element.getElementsByTagName("num").item(0).getTextContent();
        NodeList termList =  element.getElementsByTagName("query").item(0).getChildNodes();
        System.out.println("Term List: " + termList.getLength());
        for (int j = 0; j < termList.getLength(); j++){
            System.out.println("Term List Element: " + j);
            Node nNode = termList.item(j);
            if (nNode.getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element) nNode;
                String term = e.getTextContent().replaceAll("\n", "").replaceAll("\t", "").trim();
                System.out.println("Text Content:" + term);
                this.terms.add(term);
            }else{
                System.out.println("Node Type:" + nNode.getNodeType() + " " + nNode.getNodeName());
            }
        }
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
    public String toString(){
        return "Name:" + this.queryName + "\nSearch Terms: \n" + String.join("\n", this.terms);
    }

    public Query buildQuery(String[] terms, String field, BooleanQuery.Builder bq){
        WildcardQuery tempQuery = null;
        for (String term : terms){
            term = term.trim(); 
            if (!term.equals("")){
                tempQuery = new WildcardQuery(new Term(field, term));
                bq.add(tempQuery, BooleanClause.Occur.SHOULD);
            }
        }
        if (tempQuery == null){
            bq.add(new TermQuery(new Term(field, "")), BooleanClause.Occur.SHOULD);
        }
        return bq.build();
    }
}
