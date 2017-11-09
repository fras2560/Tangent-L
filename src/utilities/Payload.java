package utilities;

import org.apache.lucene.util.BytesRef;

public class Payload {
    private int formulaSize;
    private String location;

    public Payload(int formulaSize, String location){
        this.formulaSize = formulaSize;
        this.location = location;
    }

    public Payload(BytesRef bytes) throws PayloadException{
        String[] parts = bytes.utf8ToString().split(Constants.PAYLOAD_SEPARATOR);
        if(parts.length != 2){
            throw new PayloadException("Unrecognized Payload");
        }
        
        this.formulaSize = Integer.parseInt(parts[1]);
        this.location = parts[0];
    }

    public int getFormulaSize(){
        return this.formulaSize;
    }

    public String getLocation(){
        return this.location;
    }

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
