package programs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.xml.sax.SAXException;
import index.ConvertConfig;
import index.ConvertConfig.ConvertConfigException;
import query.ParseQueries;
import search.Search;
import search.Search.SearchConfigException;
import query.MathQuery;
import utilities.ProjectLogger;

public class TimeSearch{
    public TimeSearch(Path index, Path queries, int size) throws IOException,
                                                                  XPathExpressionException,
                                                                  ParserConfigurationException,
                                                                  SAXException,
                                                                  InterruptedException,
                                                                  ParseException,
                                                                  SearchConfigException,
                                                                  ConvertConfigException{
        this(index, queries, new ConvertConfig(), size, ProjectLogger.getLogger());
    }

    public TimeSearch(Path index,
                       Path queries,
                       ConvertConfig config,
                       int size) throws IOException,
                                        XPathExpressionException,
                                        ParserConfigurationException,
                                        SAXException,
                                        InterruptedException,
                                        ParseException,
                                        SearchConfigException,
                                        ConvertConfigException{
        this(index, queries, config, size, ProjectLogger.getLogger());
    }

    public TimeSearch(Path index,
                      Path queries,
                      ConvertConfig config,
                      int size,
                      Logger logger) throws IOException,
                                             XPathExpressionException,
                                             ParserConfigurationException,
                                             SAXException,
                                             InterruptedException,
                                             ParseException,
                                             SearchConfigException,
                                             ConvertConfigException{
        ParseQueries queryLoader = new ParseQueries(queries.toFile(), config);
        ArrayList<MathQuery> mathQueries = queryLoader.getQueries();
        queryLoader.deleteFile();
        Date start, end;
        ArrayList<Double> times = new ArrayList<Double>();
        Search searcher = new Search(index, config);
        for (MathQuery mq: mathQueries){
            start = new Date();
            searcher.searchQuery(mq, size);
            end = new Date();
            times.add(new Double(end.getTime() - start.getTime()));
        }
        Double min = new Double(0);
        Double max = new Double(0);
        Double total = new Double(0);
        Double variance = new Double(0);
        Double std = new Double(0);
        Double mean = new Double(0);
        if (times.size() > 0){
            min = times.get(0);
            max = times.get(0);
            for (Double time: times){
                if (time < min){
                    min = time;
                }
                if(time > max){
                    max = time;
                }
                total += time;
            }
            mean = total / times.size();
            for (Double time: times){
                variance += Math.pow((time - mean), 2);
            }
            std = Math.sqrt((variance / times.size()));
        }
        System.out.println(" Total time:" + total  +
                           " Mean:" + mean +
                           " Min:" + min + 
                           " Max:" + max +
                           " Std:" + std
                           );
    }

    public static void main(String[] args) {
        String usage = "Usage:\tjava naiveMathIndexer.TimeQueries [-index dir] [-queries file] [-precision precision] [-log logFile]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);  
            System.exit(0);
        }
        ConvertConfig config = new ConvertConfig();
        // default values
        int precision = 1000;
        Path index = Paths.get(System.getProperty("user.dir"), "resources", "index", "arXiv", "config", "compound-unbounded-edge_pairs");
        Path queries = Paths.get(System.getProperty("user.dir"), "resources", "query", "NTCIR12-ArXiv.xml");
        Path logFile = Paths.get(System.getProperty("user.dir"), "resources", "output", "arXiv", "recallChceck.log");
        for(int i = 0;i < args.length;i++) {
          if ("-index".equals(args[i])) {
            index = Paths.get(args[i+1]);
            i++;
          } else if ("-queries".equals(args[i])) {
            queries = Paths.get(args[i+1]);
            i++;
          }else if ("-log".equals(args[i])){
            logFile = Paths.get(args[i+1]);
            i++;
          }else if("-precision".equals(args[i])){
            precision = Integer.parseInt(args[i+1]);
            i++;
          }
        }
        try {
            // setup the logger
            config.loadConfig(index);
            ProjectLogger.setLevel(Level.INFO);
            ProjectLogger.setLogFile(logFile);
            // time all the different queries
            new TimeSearch(index, queries, config, precision);
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
            e.printStackTrace();
        }catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SearchConfigException e) {
            // TODO Auto-generated catch block
            System.err.println("Config files did not match");
            e.printStackTrace();
        } catch (ConvertConfigException e) {
            // TODO Auto-generated catch block
            System.err.println("ConvertConfig not found raised");
            e.printStackTrace();
        }
    }
}
