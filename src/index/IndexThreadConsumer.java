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
import java.util.concurrent.BlockingQueue;
import org.apache.lucene.index.IndexWriter;

/**
 * A Thread that Indexes Files.
 *
 * @author Dallas Fraser
 * @since 2018-02-02
 */
public class IndexThreadConsumer implements Runnable {
  private final BlockingQueue<IndexThreadObject> queue;
  private final IndexWriter writer;
  private final ConvertConfig config;

  /**
   * Constructor.
   *
   * @param queue a queue holds the files to index
   * @param writer the index to write to
   * @param config the config with the features to use
   */
  public IndexThreadConsumer(
      BlockingQueue<IndexThreadObject> queue, IndexWriter writer, ConvertConfig config) {
    this.queue = queue;
    this.writer = writer;
    this.config = config;
  }

  /** The functions called when running the thread. */
  @Override
  public void run() {
    // TODO Auto-generated method stub
    try {
      IndexThreadObject task;
      boolean run = true;
      while (run) {
        task = this.queue.take();
        if (task.complete()) {
          // done
          run = false;
        } else {
          try {
            IndexFiles.indexDoc(
                this.writer, task.getFilePath(), task.getLastModified(), this.config);
          } catch (final IOException ex) {
            System.out.println("Unable to index file:" + task.getFilePath());
            ex.printStackTrace();
          }
        }
      }
    } catch (final InterruptedException ex) {
      System.out.println("Interrupted Thread");
      ex.printStackTrace();
    }
  }
}
