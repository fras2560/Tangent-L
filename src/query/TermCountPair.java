package query;

import index.ConvertConfig;

public class TermCountPair {
    private String term;
    private float count;
    public TermCountPair(String term){
        this.term = term;
        this.count = 1f;
    }

    public void increment(){
        this.count += 1f;
    }

    public String getTerm(){
        return this.term;
    }

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
