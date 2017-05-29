package query;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
                String term = e.getTextContent().replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "");
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
}
