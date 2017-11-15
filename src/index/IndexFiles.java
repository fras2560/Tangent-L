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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import query.MathSimilarity;
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
  /**
   * Class constructor
   */
  public IndexFiles(){
      this.logger = ProjectLogger.getLogger();
  }

  /**
   * Class constructor with a specified logger
   * @param logger the logger to use
   */
  public IndexFiles(Logger logger){
      this.logger = logger;
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
      this.indexDirectory(indexPath, docsPath, create, config, MathSimilarity.getSimilarity());
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
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
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
        indexDocs(writer, docsPath, config);
        // NOTE: if you want to maximize search performance,
        // you can optionally call forceMerge here.  This can be
        // a terribly costly operation, so generally it's only
        // worth it when your index is relatively static (ie
        // you're done adding documents to it):
        //
        // writer.forceMerge(1);
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
    Logger logger = ProjectLogger.getLogger();
    Path new_file = new ConvertMathML(file).convert(config);
    int docLength = 1;
    try (InputStream stream = Files.newInputStream(new_file)){
        // get the statistics of the file
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        docLength = Functions.documentLength(writer.getAnalyzer(), Constants.FIELD, reader);
    }
    try (InputStream stream = Files.newInputStream(new_file)) {
        
        // make a new, empty document
        Document doc = new Document();
        // Add the path of the file as a field named "path".  Use a
        // field that is indexed (i.e. searchable), but don't tokenize 
        // the field into separate words and don't index term frequency
        // or positional information:
        logger.log(Level.INFO, "Working on file " + file.toString());
        Field pathField = new StringField("path", file.toString(), Field.Store.YES);
        doc.add(pathField);
        // a field to keep track of the doc length
        doc.add(new NumericDocValuesField(Constants.DOCUMENT_LENGTH, (long) docLength));
        // Add the last modified date of the file a field named "modified".
        // Use a LongPoint that is indexed (i.e. efficiently filterable with
        // PointRangeQuery).  This indexes to milli-second resolution, which
        // is often too fine.  You could instead create a number based on
        // year/month/day/hour/minutes/seconds, down the resolution you require.
        // For example the long value 2011021714 would mean
        // February 17, 2011, 2-3 PM.
        doc.add(new LongPoint("modified", lastModified));
        // Add the contents of the file to a field named "contents".  Specify a Reader,
        // so that the text of the file is tokenized and indexed, but not stored.
        // Note that FileReader expects the file to be in UTF-8 encoding.
        // If that's not the case searching for special characters will fail.;
        FieldType fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fieldType.setTokenized(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStoreTermVectorPayloads(true);
        fieldType.setStoreTermVectorOffsets(true);
        doc.add(new Field(Constants.FIELD,
                          new InputStreamReader(stream, StandardCharsets.UTF_8),
                          fieldType
                          ));
        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            // New index, so we just add the document (no old document can be there):
            logger.log(Level.FINE, "Adding file: " + file.toString());
            writer.addDocument(doc);
        } else {
            // Existing index (an old copy of this document may have been indexed) so 
            // we use updateDocument instead to replace the old one matching the exact 
            // path, if present:
            logger.log(Level.FINE, "Updating file: " + file.toString());
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
    Path indexPath = Paths.get(System.getProperty("user.dir"), "resources", "index", "arXiv", "current");
    Path docsPath = Paths.get(System.getProperty("user.dir"), "resources", "document", "arXiv");
    Path logFile = Paths.get(System.getProperty("user.dir"), "resources", "logs", "index.log");
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
        config.setBooleanAttribute(ConvertConfig.SYNONYMS, true);
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
