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
package utilities;

import org.apache.lucene.util.BytesRef;

/**
 * A class that deals with the payload indexed and search with Lucene
 * @author Dallas Fraser
 * @since 2017-11-09
 */
public class Payload {
    private int formulaSize;
    private String location;

    /**
     * A class constructor
     * @param formulaSize the size of the formula
     * @param location the location of the math tuple in the formula
     */
    public Payload(int formulaSize, String location){
        this.formulaSize = formulaSize;
        this.location = location;
    }

    /**
     * A class constructor
     * @param bytes how Lucene stores the payload
     * @throws PayloadException is raised when the payload has a different number of parts 
     */
    public Payload(BytesRef bytes) throws PayloadException{
        String[] parts = bytes.utf8ToString().split(Constants.PAYLOAD_SEPARATOR);
        if(parts.length != 2){
            throw new PayloadException("Unrecognized Payload");
        }
        this.formulaSize = Integer.parseInt(parts[1]);
        this.location = parts[0];
    }

    /**
     * Returns the formula size of the payload
     * @return int the size of the formula
     */
    public int getFormulaSize(){
        return this.formulaSize;
    }

    /**
     * Return the location of the tuple
     * @return String the location of the tuple
     */
    public String getLocation(){
        return this.location;
    }

    /*
     * Prints a Payload to a String, with <code>field</code> assumed to be the
     * default field and omitted
     * (non-Javadoc)
     * @see org.apache.lucene.search.Query#toString(java.lang.String)
     * @return the string representation
     */
    public String toString(){
        String result;
        result = "Payload " + this.location + ":" + this.formulaSize;
        return result;
    }

    /**
     * An exception raised by the payload when parsing the BytesRef
     * @author Dallas Fraser
     * @since 2017-11-09
     */
    public class PayloadException extends Exception{
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        public PayloadException(String message){
            super(message);
        }
    }
}
