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
package index;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import search.MathSimilarityWrapper;
import utilities.Constants;
import utilities.Functions;
import utilities.ProjectLogger;


/** Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing.
 * Run it with no command-line arguments for usage information.
 * @author Dallas Fraser
 * @since 2017-11-06
 */
public class IndexFiles {
  private Logger logger;
  private boolean multiThreaded;
  /**
   * Class constructor
   * Defaults logger to projectLogger, and is multi-threaded
   */
  public IndexFiles(){
      this(ProjectLogger.getLogger(), true);
  }

  /**
   * Class constructor with a specified logger and is multi-threaded
   * @param logger the logger to use
   */
  public IndexFiles(Logger logger){
      this(logger, true);
  }

  /**
   * Class constructor with specifying if multi-threaded or not
   * @param logger the logger to use
   */
  public IndexFiles(boolean multiThreaded){
      this(ProjectLogger.getLogger(), multiThreaded);
  }

  /**
   * Class constructor with specifying it multi-threaded and logger
   * @param logger the logger to use
   * @param multiThreaded True if want multi-threaded
   */
  public IndexFiles(Logger logger, boolean multiThreaded){
      this.logger = logger;
      this.multiThreaded = multiThreaded;
  }

  /**
   * Index a directory using the default similarity
   * @param indexPath the path to the index
   * @param docsPath the path to the documents
   * @param create whether to create the index versus just appending to one
   * @param config the config file to use when indexing
   * @throws IOException
   */
  public void indexDirectory(Path indexPath,
                             Path docsPath,
                             boolean create,
                             ConvertConfig config) throws IOException{
      this.indexDirectory(indexPath, docsPath, create, config, new MathSimilarityWrapper());
  }

  /**
   * Index all the documents
   * @param documents path to the documents folder
   * @param consumers the number of consumer threads
   * @param queue the queue to add the files to
   */
  public void run(Path documents, int consumers, BlockingQueue<IndexThreadObject> queue) {
      // add all the documents to the thread
      this.indexDocs(documents, queue);
      // add signals to the consumers they are done
      for (int i =0 ; i < consumers; i++){
          queue.add(new IndexThreadObject(IndexThreadObject.COMPLETE, 0l));
      }
  }

  /**
   * indexDocs using multiple threads
   * @param path the path to main folder
   * @param queue the queue to adds for the consumer threads
   */
  public void indexDocs(Path path, BlockingQueue<IndexThreadObject> queue){
      if (Files.isDirectory(path)) {
          try {
              Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                      queue.put(new IndexThreadObject(file.toString(), attrs.lastModifiedTime().toMillis()));
                  } catch (InterruptedException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                      System.out.println("Unable to add file: " + file.toString());
                  }
                    return FileVisitResult.CONTINUE;
                }
              });
          } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
      } else {
          try {
              queue.put(new IndexThreadObject(path.toString(), Files.getLastModifiedTime(path).toMillis()));
          } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              System.out.println("Unable to add file: " + path.toString());
              e.printStackTrace();
          } catch (IOException e) {
              // TODO Auto-generated catch block
              System.out.println("Unable to add file: " + path.toString());
              e.printStackTrace();
          }
      }
  }

  /**
   * Index a directory
   * @param indexPath the path to the index
   * @param docsPath the path to the documents
   * @param create whether to create the index versus just appending to one
   * @param config the config file to use when indexing
   * @param simlarity the similarity to be used when indexing
   * @throws IOException
   */
  public void indexDirectory(Path indexPath,
                             Path docsPath,
                             boolean create,
                             ConvertConfig config,
                             Similarity simlarity) throws IOException{
      if (!Files.isReadable(docsPath)) {
          this.logger.log(Level.SEVERE, docsPath + ": File does not exist");
          throw new IOException("File does not exist");
      }
      Date start = new Date();
      try {
        this.logger.log(Level.FINE, "Indexing to directory: '" + indexPath.toString() + "'...");
        Directory dir = FSDirectory.open(indexPath);
        Analyzer analyzer = new MathAnalyzer(config);
        Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put(Constants.MATHFIELD, new JustMathAnalyzer());
        analyzerPerField.put(Constants.TEXTFIELD, new JustTextAnalyzer());
        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerPerField);
        IndexWriterConfig iwc = new IndexWriterConfig(wrapper);
        iwc.setSimilarity(simlarity);
        if (create) {
          // Create a new index in the directory, removing any
          // previously indexed documents:
          iwc.setOpenMode(OpenMode.CREATE);
        } else {
          // Add new documents to an existing index:
          iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        }
        // Optional: for better indexing performance, if you
        // are indexing many documents, increase the RAM
        // buffer.  But if you do this, increase the max heap
        // size to the JVM (eg add -Xmx512m or -Xmx1g):
        //
        // iwc.setRAMBufferSizeMB(256.0);
        IndexWriter writer = new IndexWriter(dir, iwc);
        int processors = Runtime.getRuntime().availableProcessors();
        if(this.multiThreaded && processors > 1){
            BlockingQueue<IndexThreadObject> bq = new LinkedBlockingQueue<IndexThreadObject>();
            System.out.println("Number of processors:" + processors);
            
            for(int i = 0; i < (processors); i++){
                new Thread(new IndexThreadConsumer(bq, writer, config)).start();
            }
            this.run(docsPath, processors, bq);
            while (!bq.isEmpty()){
                System.out.println("Number of files left: "  + bq.size());
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }else{
            indexDocs(writer, docsPath, config);
        }
        // NOTE: if you want to maximize search performance,
        // you can optionally call forceMerge here.  This can be
        // a terribly costly operation, so generally it's only
        // worth it when your index is relatively static (ie
        // you're done adding documents to it):
        writer.forceMerge(4);
        Date end = new Date();
        this.logger.log(Level.INFO, end.getTime() - start.getTime() + " total milliseconds");
        config.saveConfig(indexPath);
        writer.close();
        dir.close();
      } catch (IOException e) {
        this.logger.log(Level.WARNING, " caught a " + e.getClass() + "\n with message: " + e.getMessage());
      }
        
  }

  /**
   * Indexes the given file using the given writer, or if a directory is given,
   * recurses over files and directories found under the given directory.
   * 
   * NOTE: This method indexes one document per input file.  This is slow.  For good
   * throughput, put multiple documents into your input file(s).  An example of this is
   * in the benchmark module, which can create "line doc" files, one document per line,
   * using the
   * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
   * >WriteLineDocTask</a>.
   *  
   * @param writer Writer to the index where the given file/dir info will be stored
   * @param path The file to index, or the directory to recurse into to find files to index
   * @throws IOException If there is a low-level I/O error
   */
  static void indexDocs(final IndexWriter writer, Path path, ConvertConfig config) throws IOException {
      Logger logger = ProjectLogger.getLogger();
      if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                logger.log(Level.FINE, "Indexing file:" + file.toString());
                indexDoc(writer, file, attrs.lastModifiedTime().toMillis(), config);
            } catch (IOException ignore) {
                ignore.printStackTrace();
                logger.log(Level.WARNING, "Unable to index file");
                // don't index files that can't be read.
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                logger.log(Level.WARNING, "Unable to index file");
                e.printStackTrace();
            }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
        try {
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis(), config);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            logger.log(Level.WARNING, "Unable to index file");
            e.printStackTrace();
        }
    }
  }

  /** Indexes a single document 
 * @throws InterruptedException */
  static void indexDoc(IndexWriter writer,
                       Path file,
                       long lastModified,
                       ConvertConfig config) throws IOException, InterruptedException {
    Path new_file = new ConvertMathML(file).convert(config);
    int docLength = 1;
    String text = "";
    // make a new, empty document
    Document doc = new Document();
    if(config.getAttribute(ConvertConfig.PROXIMITY) || config.getAttribute(ConvertConfig.SEPERATE_MATH_TEXT)){
        text = Functions.parseString(new_file);
        docLength = text.split(" ").length;
        int formulaCount = Functions.countTuples(text);
        // a field to keep track of the doc length and formula length
        doc.add(new StoredField(Constants.FORMULA_COUNT, formulaCount));
        doc.add(new StoredField(Constants.DOCUMENT_LENGTH, docLength));
    }
    try (InputStream stream = Files.newInputStream(new_file)) {
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        // Add the path of the file as a field named "path".  Use a
        Field pathField = new StringField("path", file.toString(), Field.Store.YES);
        doc.add(pathField);
        // Add the last modified date of the file a field named "modified".
        doc.add(new LongPoint("modified", lastModified));
        // Add the contents of the file to a field named "contents".  Specify a Reader,
        // so that the text of the file is tokenized and indexed, but not stored.
        FieldType storeField = new FieldType();
        if(config.getAttribute(ConvertConfig.PROXIMITY)){
            storeField.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            storeField.setTokenized(true);
            storeField.setStoreTermVectors(true);
            storeField.setStoreTermVectorPositions(true);
            storeField.setStoreTermVectorOffsets(true);
            if(config.getAttribute(ConvertConfig.PAYLOADS)){
                storeField.setStoreTermVectorPayloads(true);
            }
        }else{
            storeField.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
            storeField.setTokenized(true);
        }
        
        FieldType freqType = new FieldType();
        freqType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        freqType.setTokenized(true);
        if(config.getAttribute(ConvertConfig.SEPERATE_MATH_TEXT)){
            doc.add(new Field(Constants.TEXTFIELD, text, freqType));
            doc.add(new Field(Constants.MATHFIELD, text, freqType));
        }else{
            doc.add(new Field(Constants.FIELD, reader, storeField));
        }
        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            // New index, so we just add the document (no old document can be there):
            writer.addDocument(doc);
        } else {
            // Existing index (an old copy of this document may have been indexed) so 
            // we use updateDocument instead to replace the old one matching the exact 
            // path, if present:
            writer.updateDocument(new Term("path", file.toString()), doc);
        }
    }
    // remove the file
    new_file.toFile().delete();
  }

  /** Index all text files under a directory. */
  public static void main(String[] args) {
    String usage = "java index.IndexFiles"
                 + " [-index INDEX_PATH] [-docs DOCS_PATH] [-logfile file] [-update]\n\n"
                 + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                 + "in INDEX_PATH that can be searched with SearchFiles";
    Path indexPath = Paths.get(System.getProperty("user.dir"), "resources", "index", "full_arXiv", "current");
    Path docsPath = Paths.get("/home", "d6fraser", "Documents", "Research", "Datasets", "NTCIR12_FullArXiv");
    Path logFile = Paths.get(System.getProperty("user.dir"), "resources", "logs", "NTCIR12_FullArxiv.log");
    boolean create = true;
    for(int i=0;i<args.length;i++) {
      if ("-index".equals(args[i])) {
          indexPath = Paths.get(args[i+1]);
          i++;
      } else if ("-docs".equals(args[i])) {
          docsPath = Paths.get(args[i+1]);
          i++;
      } else if ("-update".equals(args[i])) {
          create = false;
      } else if ("-logfile".equals(args[i])){
          logFile = Paths.get(args[i+1]);
      }
    }
    // make sure the files are legit
    if (docsPath == null) {
         ProjectLogger.getLogger().log(Level.SEVERE, "Usage: " + usage);
         System.exit(1);
    }
    if (!Files.isReadable(docsPath)) {
        ProjectLogger.getLogger().log(Level.SEVERE, "Document directory '" +
                                          docsPath.toAbsolutePath() +
                                          "' does not exist or is not readable, please check the path");
        System.exit(1);
    }
    try {
        ProjectLogger.getLogger().setLevel(Level.FINEST);
        ProjectLogger.setLogFile(logFile);
        ProjectLogger.getLogger().log(Level.FINE, "Hello");
        System.out.println(ProjectLogger.getLogger().getLevel());
        ConvertConfig config = new ConvertConfig();
        // use the best known configuration
        // -!SHORTENED -LOCATION -COMPOUND_SYMBOLS -TERMINAL_SYMBOLS -UNBOUNDED -SYNONYMS -BAG_OF_WORDS
        config.setBooleanAttribute(ConvertConfig.BAGS_OF_WORDS, true);
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
        config.setBooleanAttribute(ConvertConfig.UNBOUNDED, true);
        config.setBooleanAttribute(ConvertConfig.TERMINAL, true);
        config.setBooleanAttribute(ConvertConfig.COMPOUND, true);
        config.setBooleanAttribute(ConvertConfig.EXPAND_LOCATION, true);
        config.setBooleanAttribute(ConvertConfig.SHORTENED, true);
        IndexFiles idf = new IndexFiles();
        idf.indexDirectory(indexPath, docsPath, create, config);
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        ProjectLogger.getLogger().log(Level.SEVERE, "Document directory does not exist");
        System.exit(1);
    } catch(SecurityException e){
        e.printStackTrace();
        ProjectLogger.getLogger().log(Level.SEVERE, "Issue with the logger");
        System.exit(1);
    }
  }
}
