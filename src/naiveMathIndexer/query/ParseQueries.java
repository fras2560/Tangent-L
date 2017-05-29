package naiveMathIndexer.query;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import naiveMathIndexer.index.ConvertMathML;
import query.MathQuery;

public class ParseQueries{
    private File f;
    private ArrayList<MathQuery> queries;
    public ParseQueries(File f) throws IOException, InterruptedException{
        Path new_file = new ConvertMathML(f.toPath()).convert();
        this.f = new File(new_file.toString());
        this.queries = new ArrayList<MathQuery>();
    }

    public ArrayList<MathQuery> getQueries()
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.f);
        doc.getDocumentElement().normalize();
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "topics/topic";
        NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        System.out.println("NodeList");
        System.out.println(nodeList);
        System.out.println("Length: " + nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            System.out.println("Node");
            System.out.println(node);
            MathQuery q = new MathQuery(node);
            System.out.println(q);
            this.queries.add(q);
        }
        return this.queries;
    }
}
