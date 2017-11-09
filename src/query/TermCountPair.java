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
package query;

import java.util.ArrayList;
import java.util.List;

import utilities.Payload;

/**
 * A Class that holds a term and a count
 * @author Dallas Fraser
 * @since 2017-11-06
 *
 */
public class TermCountPair {
    private String term;
    private float count;
    private List<Payload> payloads;
    /**
     * Class Constructor
     * @param term the term
     */
    public TermCountPair(String term){
        this.term = term;
        this.payloads = new ArrayList<Payload>();
        this.count = 1f;
    }

    /**
     * Increases the count
     */
    public void increment(){
        this.count += 1f;
    }

    /**
     * Returns the term
     * @return String the term
     */
    public String getTerm(){
        return this.term;
    }


    /**
     * Adds a payload to the term 
     * @param payload
     */
    public void addPayload(Payload payload){
        this.payloads.add(payload);
    }

    public int[] payloadFormulaSizes(){
        int[] fs = new int[this.payloads.size()];
        int i = 0;
        for (Payload pl : this.payloads){
            fs[i] = pl.getFormulaSize();
        }
        return fs;
    }

    public List<String> payloadLocations(){
        List<String> locations = new ArrayList<String>();
        for (Payload pl : this.payloads){
            locations.add(pl.getLocation());
        }
        return locations;
    }

    /**
     * Returns the number of count
     * @return float the count
     */
    public float getCount(){
        return this.count;
    }

    /*
     * Returns a String representation of the object
     * @return a String representation
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return this.term + ":" + this.count;
    }

    @Override
    /*
     * Returns True if the two objects are equal False otherwise
     * @param o the object to check if it equal with
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        // If the object is compared with itself then return true  
        if (o == this) {
            return true;
        }
        if (o instanceof String){
            return o.equals(this.term);
        }
        /* Check if o is an instance of TermCountPair or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof TermCountPair)) {
            return false;
        }
        
        // typecast o to TermCountPair so that we can compare data members 
        TermCountPair c = (TermCountPair) o;
        // Compare the data members and return accordingly
        return this.count == c.count && this.term.equals(c.getTerm());
    }
}
