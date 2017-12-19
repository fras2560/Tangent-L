package testing.search;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testing.BaseTest;
import utilities.Functions;

public class TestTompaScoreFormula extends BaseTest{
    private final double TOMPA_B = 0.75f;
    private final double TOMPA_K1 = 1.2f;
    @Test
    public void testDocLength() {
        // plot out doc length
        for(double docLength = 1; docLength < 100; docLength += 10){
            Functions.scoreFormula(1d, docLength, 100d, 50d, 1d, this.TOMPA_K1, this.TOMPA_B);
        }
    }
    
    @Test
    public void testRank() {
        // plot out doc length
        for(double rank = 1; rank < 100; rank += 10){
            Functions.scoreFormula(1d, 50d, 100d, 50d, rank, this.TOMPA_K1, this.TOMPA_B);
        }
    }

}
