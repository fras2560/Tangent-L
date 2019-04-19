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

package programs;

import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.xml.sax.SAXException;
import query.MathQuery;
import query.ParseQueries;
import search.Search;
import search.Search.SearchConfigException;
import utilities.ProjectLogger;

/**
 * A program to time the amount time used for search.
 *
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class TimeSearch {
  /**
   * Class Constructor.
   *
   * @param index the path to the index
   * @param queries the path to the queries
   * @param size the number of documents to return for each query
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public TimeSearch(Path index, Path queries, int size)
      throws IOException, XPathExpressionException, ParserConfigurationException, SAXException,
          InterruptedException, ParseException, SearchConfigException, ConvertConfigException {
    this(index, queries, new ConvertConfig(), size, ProjectLogger.getLogger());
  }

  /**
   * Class Constructor.
   *
   * @param index the path to the index
   * @param queries the path to the queries
   * @param config the convert config to use
   * @param size the number of documents to return for each query
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public TimeSearch(Path index, Path queries, ConvertConfig config, int size)
      throws IOException, XPathExpressionException, ParserConfigurationException, SAXException,
          InterruptedException, ParseException, SearchConfigException, ConvertConfigException {
    this(index, queries, config, size, ProjectLogger.getLogger());
  }

  /**
   * Class Constructor.
   *
   * @param index the path to the index
   * @param queries the path to the queries
   * @param config the convert config to use
   * @param size the number of documents to return for each query
   * @param logger the logger to use
   * @throws IOException - issue with parsing file
   * @throws XPathExpressionException - issue parsing math ml
   * @throws ConvertConfigException - exception when converting mathml
   * @throws InterruptedException - issue when python process
   * @throws ParserConfigurationException - parsing issue
   * @throws SAXException - parsing issue
   * @throws SearchConfigException - search config not compatible with index
   */
  public TimeSearch(Path index, Path queries, ConvertConfig config, int size, Logger logger)
      throws IOException, XPathExpressionException, ParserConfigurationException, SAXException,
          InterruptedException, ParseException, SearchConfigException, ConvertConfigException {
    final long queryLoadStart = System.currentTimeMillis();
    final ParseQueries queryLoader = new ParseQueries(queries.toFile(), config);
    final ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
    queryLoader.deleteFile();
    long start;
    long end;
    final ArrayList<Long> times = new ArrayList<Long>();
    final Search searcher = new Search(index, config);
    final long queryLoadEnd = System.currentTimeMillis();
    System.out.println(new Double(queryLoadEnd - queryLoadStart));
    for (int i = 0; i < 10; i++) {
      for (final MathQuery mq : mathQueries) {
        start = System.currentTimeMillis();
        searcher.searchQuery(mq, size);
        end = System.currentTimeMillis();
        times.add(new Long(end - start));
      }
      System.out.println(i);
    }
    Long min = new Long(0);
    Long max = new Long(0);
    Long total = new Long(0);
    Long variance = new Long(0);
    Long std = new Long(0);
    Long mean = new Long(0);
    Long temp;
    if (times.size() > 0) {
      min = times.get(0);
      max = times.get(0);
      for (final Long time : times) {
        if (time < min) {
          min = time;
        }
        if (time > max) {
          max = time;
        }
        total += time;
      }
      mean = total / times.size();
      for (final Long time : times) {
        temp = (time - mean);
        variance += powerN(temp.longValue(), 2);
      }
      std = (long) Math.sqrt((variance / times.size()));
    }
    System.out.println(
        " Total time:"
            + total
            + " Mean:"
            + mean
            + " Min:"
            + min
            + " Max:"
            + max
            + " Std:"
            + std
            + " Variance:"
            + variance);
  }

  /**
   * Return a number to the given power.
   *
   * @param number - the number
   * @param power - the power to apply to the number
   * @return long - the result
   */
  public static long powerN(long number, int power) {
    if (power == 0) {
      return 1;
    }
    long result = number;
    while (power > 1) {
      result *= number;
      power--;
    }
    return result;
  }

  /**
   * The main entry to the program.
   *
   * @param args - see usage
   */
  public static void main(String[] args) {
    final String usage =
        "Usage:\tjava naiveMathIndexer.TimeQueries [-index dir]"
            + " [-queries file] [-precision precision] [-log logFile]";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }
    final ConvertConfig config = new ConvertConfig();
    // default values
    int precision = 100;
    Path index =
        Paths.get(
            System.getProperty("user.dir"),
            "resources",
            "index",
            "wikipedia_formula",
            "findOptimal",
            "-LOCATION-COMPOUND_SYMBOLS-TERMINAL_SYMBOLS-EDGE_PAIRS-SYNONYMS");
    Path queries =
        Paths.get(
            System.getProperty("user.dir"), "resources", "query", "NTCIR11-Math-Wikipedia.xml");
    Path logFile =
        Paths.get(
            System.getProperty("user.dir"),
            "resources",
            "output",
            "wikipedia_formula",
            "recallChceck.log");
    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        index = Paths.get(args[i + 1]);
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = Paths.get(args[i + 1]);
        i++;
      } else if ("-log".equals(args[i])) {
        logFile = Paths.get(args[i + 1]);
        i++;
      } else if ("-precision".equals(args[i])) {
        precision = Integer.parseInt(args[i + 1]);
        i++;
      }
    }
    try {
      // setup the logger
      config.loadConfig(index);
      ProjectLogger.setLevel(Level.OFF);
      ProjectLogger.setLogFile(logFile);
      // time all the different queries
      new TimeSearch(index, queries, config, precision);

    } catch (final IOException e) {
      System.err.println("Problem writing to the file statsTest.txt");
      e.printStackTrace();
    } catch (final XPathExpressionException e) {
      e.printStackTrace();
    } catch (final ParserConfigurationException e) {
      e.printStackTrace();
    } catch (final SAXException e) {
      e.printStackTrace();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    } catch (final ParseException e) {
      e.printStackTrace();
    } catch (final SearchConfigException e) {
      System.err.println("Config files did not match");
      e.printStackTrace();
    } catch (final ConvertConfigException e) {
      System.err.println("ConvertConfig not found raised");
      e.printStackTrace();
    }
  }
}
