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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;

import utilities.Constants;
import utilities.Functions;

/**
 * A Thread that waslks a document folder and adds file to index to a queue
 * @author Dallas Fraser
 * @since 2018-02-02
 */
public class IndexThreadProducer implements Runnable{
    private BlockingQueue <IndexThreadObject> queue;
    private Path documents;
    private int consumers;
    /**
     * Constructor
     * @param queue the queue to add files to
     * @param documents the directory that holds the documents
     * @param consumers the number of threads that are indexing
     */
    public IndexThreadProducer(BlockingQueue<IndexThreadObject> queue, Path documents, int consumers){
        this.queue = queue;
        this.documents = documents;
        this.consumers = consumers;
    }

    /**
     * The functions called when running the thread
     * Walks the documents directory and then adds a stop signal for each consumer thread
     */
    @Override
    public void run() {
        // add all the documents to the thread
        this.indexDocs(this.documents);
        // add signals to the consumers they are done
        for (int i =0 ; i < this.consumers; i++){
            this.queue.add(new IndexThreadObject(IndexThreadObject.COMPLETE, 0l));
        }
        // System.out.println("Producer exiting");
    }

    /**
     * Walks the file path and adds files to a queue to be indexed
     * @param path the documents directory
     */
    public void indexDocs(Path path){
        if (Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                  @Override
                  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                      try {
                          if(Functions.parseTitle(file.toString()).endsWith(Constants.TEMP_EXT)){
                              System.out.println("Did not add: " + file.toString() + " since it appears to be a temp file");
                          }else{
                              queue.put(new IndexThreadObject(file.toString(), attrs.lastModifiedTime().toMillis()));
                          }
                              
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
