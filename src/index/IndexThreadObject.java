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

import java.nio.file.Path;
import java.nio.file.Paths;

/** An Object used to store task information that is stored on the queue
 * @author Dallas Fraser
 * @since 2018-02-02
 */
public class IndexThreadObject {
    public static String COMPLETE = "COMPLETE";
    public static String MOVE_ON = "MOVE_ON";
    private String path;
    private long lastModified;
    /**
     * Constructor
     * @param path the path to the file to index
     * @param lastModified the time the file was last modified
     */
    public IndexThreadObject(String path, long lastModified){
        this.path = path;
        this.lastModified = lastModified;
    }

    /**
     * Returns true if the tasks is signal the queue is complete
     * @return boolean True if it is down
     */
    public boolean complete(){
        boolean done = false;
        if (this.path.equals(IndexThreadObject.COMPLETE)){
            done = true;
        }
        return done;
    }

    /**
     * Returns the time the file was last modified
     * @return long the time last modified
     */
    public long getLastModified(){
        return this.lastModified;
    }

    /**
     * Returns the path to the file to index
     * @return Path the path to the file
     */
    public Path getFilePath(){
        return Paths.get(this.path);
    }

}
