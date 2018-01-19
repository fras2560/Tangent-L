package index;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import utilities.Constants;


public class IndexThreadConsumer implements Runnable{
    private BlockingQueue <IndexThreadObject>queue;
    private IndexWriter writer;
    private ConvertConfig config;
    public IndexThreadConsumer(BlockingQueue <IndexThreadObject>queue, IndexWriter writer, ConvertConfig config){
        this.queue = queue;
        this.writer =  writer;
        this.config = config;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try{
            IndexThreadObject task;
            boolean run = true;
            while(run){
                task = this.queue.take();
                if(task.complete()){
                    // done
                    run = false;
                }else{
                    
                }
            }
        } catch (InterruptedException ex){
            System.out.println("Interrupted Thread");
            ex.printStackTrace();
        }
    }

    public void indexFile(Path file, long lastModified){
        Path new_file;
        try {
            new_file = new ConvertMathML(file).convert(this.config);
            try (InputStream stream = Files.newInputStream(new_file)) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                // make a new, empty document
                Document doc = new Document();
                // Add the path of the file as a field named "path".  Use a
                Field pathField = new StringField("path", file.toString(), Field.Store.YES);
                doc.add(pathField);
                // Add the last modified date of the file a field named "modified".
                doc.add(new LongPoint("modified", lastModified));
                // Add the contents of the file to a field named "contents".  Specify a Reader,
                FieldType storeField = new FieldType();
                storeField.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
                storeField.setTokenized(true);
                doc.add(new Field(Constants.FIELD, reader, storeField));
                if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                    // New index, so we just add the document (no old document can be there):
                    writer.addDocument(doc);
                } else {
                    // Existing index (an old copy of this document may have been indexed) so 
                    // we use updateDocument instead to replace the old one matching the exact 
                    // path, if present:
                    writer.updateDocument(new Term("path", file.toString()), doc);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("Unable to index file: " + file.toString());
                e.printStackTrace();
            }
            // remove the file
            new_file.toFile().delete();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            System.out.println("Unable to index file: " + file.toString());
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            System.out.println("Unable to index file: " + file.toString());
            e1.printStackTrace();
        }
      }
}
