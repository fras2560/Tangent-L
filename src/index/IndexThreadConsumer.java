package index;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import org.apache.lucene.index.IndexWriter;


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
                    System.out.println("Shutting down");
                }else{
                    System.out.println("Indexing: " + task.getFilePath());
                    try{
                        IndexFiles.indexDoc(writer, task.getFilePath(), task.getLastModified(), this.config);
                    } catch (IOException ex){
                        System.out.println("Unable to index file:" + task.getFilePath());
                        ex.printStackTrace();
                    }
                    
                }
            }
        } catch (InterruptedException ex){
            System.out.println("Interrupted Thread");
            ex.printStackTrace();
        }
    }
}
