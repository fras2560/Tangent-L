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

import index.ConvertConfig;
import index.ConvertMathMl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utilities.ProjectLogger;

/**
 * A class used to parse queries from a file.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class ParseQueries {
  private final File tempFile;
  private final ArrayList<MathQuery> queries;
  private Logger logger;
  private final ConvertConfig config;

  /**
   * Class Constructor.
   *
   * @param f the file (NTCIR - MathIR file format)
   * @throws IOException - issue with reading/writing from file
   * @throws InterruptedException - issue with converting to math tuples
   */
  public ParseQueries(File f) throws IOException, InterruptedException {
    // always uses the optimal config file
    this(f, new ConvertConfig(), ProjectLogger.getLogger());
  }

  /**
   * Class Constructor.
   *
   * @param f the file (NTCIR - MathIR file format)
   * @param logger the logger to use
   * @throws IOException - issue with reading/writing from file
   * @throws InterruptedException - issue with converting to math tuples
   */
  public ParseQueries(File f, Logger logger) throws IOException, InterruptedException {
    // always uses the optimal config file
    this(f, new ConvertConfig(), logger);
  }

  /**
   * A Constructor.
   *
   * @param f the file (NTCIR - MathIR file format)
   * @param config the config to use to convert the math formula
   * @throws IOException - issue with reading/writing from file
   * @throws InterruptedException - issue with converting to math tuples
   */
  public ParseQueries(File f, ConvertConfig config) throws IOException, InterruptedException {
    this(f, config, ProjectLogger.getLogger());
  }

  /**
   * A Constructor.
   *
   * @param f the file (NTCIR - MathIR file format)
   * @param config the config to use to convert the math formula
   * @param logger the loger to use
   * @throws IOException - issue with reading/writing from file
   * @throws InterruptedException - issue with converting to math tuples
   */
  public ParseQueries(File f, ConvertConfig config, Logger logger)
      throws IOException, InterruptedException {
    this.logger = logger;
    this.logger = ProjectLogger.getLogger();
    final Path new_file = new ConvertMathMl(f.toPath()).convertPath(config.getSearchConfig());
    this.tempFile = new File(new_file.toString());
    this.queries = new ArrayList<MathQuery>();
    this.config = config;
  }

  /**
   * Returns a list of queries parsed from the file.
   *
   * @return ArrayList a list of math queries parsed
   * @throws ParserConfigurationException - issue parsing configuration
   * @throws SAXException - issue parsing
   * @throws IOException - issue with reading from file
   * @throws XPathExpressionException - issue with html expression
   */
  public ArrayList<MathQuery> getQueries()
      throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder;
    docBuilder = dbFactory.newDocumentBuilder();
    final Document doc = docBuilder.parse(this.tempFile);
    doc.getDocumentElement().normalize();
    final XPath xpath = XPathFactory.newInstance().newXPath();
    final String expression = "topics/topic";
    final NodeList nodeList =
        (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
    this.logger.log(Level.FINEST, "NodeList" + nodeList);
    this.logger.log(Level.FINEST, "Length: " + nodeList.getLength());
    for (int i = 0; i < nodeList.getLength(); i++) {
      final Node node = nodeList.item(i);
      this.logger.log(Level.FINEST, "Node: " + node);
      final MathQuery q = new MathQuery(node, this.config);
      this.logger.log(Level.FINEST, "Query:" + q);
      this.queries.add(q);
    }
    return this.queries;
  }

  /** Deletes the temporary file used when converting the MathML. */
  public void deleteFile() {
    this.tempFile.delete();
  }
}
