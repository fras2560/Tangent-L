package index;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;

public class IndexThreadProducer implements Runnable{
    private BlockingQueue <IndexThreadObject> queue;
    private Path documents;
    private int consumers;
    public IndexThreadProducer(BlockingQueue<IndexThreadObject> queue, Path documents, int consumers){
        this.queue = queue;
        this.documents = documents;
        this.consumers = consumers;
    }

    @Override
    public void run() {
        // add all the documents to the thread
        this.indexDocs(this.documents);
        // add signals to the consumers they are done
        for (int i =0 ; i < this.consumers; i++){
            this.queue.add(new IndexThreadObject(IndexThreadObject.COMPLETE, 0l));
        }
        this.queue.add(new IndexThreadObject(IndexThreadObject.MOVE_ON, 0l));
    }

    public void indexDocs(Path path){
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
}
