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
package naiveMathIndexer.query;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.xml.sax.SAXException;
import naiveMathIndexer.index.ConvertConfig;
import naiveMathIndexer.index.ConvertMathML;
import query.MathQuery;
import utilities.ProjectLogger;


public class ParseQueries{
    private File f;
    private ArrayList<MathQuery> queries;
    private Logger logger;
    public ParseQueries(File f) throws IOException, InterruptedException{
        // always uses the optimal config file
        this(f, new ConvertConfig(), ProjectLogger.getLogger());
    }
    public ParseQueries(File f, Logger logger) throws IOException, InterruptedException{
        // always uses the optimal config file
        this(f, new ConvertConfig(), logger);
    }
    public ParseQueries(File f, ConvertConfig config) throws IOException, InterruptedException{
        this(f, config, ProjectLogger.getLogger());
    }
    public ParseQueries(File f, ConvertConfig config, Logger logger) throws IOException, InterruptedException{
        this.logger = logger;
        this.logger = ProjectLogger.getLogger();
        Path new_file = new ConvertMathML(f.toPath()).convert(config);
        this.f = new File(new_file.toString());
        this.queries = new ArrayList<MathQuery>();
    }
    public ArrayList<MathQuery> getQueries() throws ParserConfigurationException,
                                                    SAXException,
                                                    IOException,
                                                    XPathExpressionException{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.f);
        doc.getDocumentElement().normalize();
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "topics/topic";
        NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        this.logger.log(Level.FINEST, "NodeList" + nodeList);
        this.logger.log(Level.FINEST, "Length: " + nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            this.logger.log(Level.FINEST, "Node: " + node);
            MathQuery q = new MathQuery(node);
            this.logger.log(Level.FINEST, "Query:" + q);
            this.queries.add(q);
        }
        return this.queries;
    }

    public void deleteFile(){
        this.f.delete();
    }
}
