package testing.query;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import query.MathScoreQueryProvider;

public class TestMinSpanCalc {
    private MathScoreQueryProvider mq;
    @Before
    public void setUp() throws Exception {
        mq = new MathScoreQueryProvider(null, null, null, null, 0, 0);
    }

    @After
    public void tearDown() throws Exception {
    }

    public List<List<Integer>> testData(int dataSet){

        List<List<Integer>> positions = new ArrayList<List<Integer>>();
        if (dataSet <= 1){
            List<Integer> t1 = new ArrayList<Integer>();
            t1.add(new Integer(0));
            t1.add(new Integer(2));
            t1.add(new Integer(4));
            t1.add(new Integer(6));
            List<Integer> t2 = new ArrayList<Integer>();
            t2.add(new Integer(1));
            t2.add(new Integer(4));
            t2.add(new Integer(7));
            List<Integer> t3 = new ArrayList<Integer>();
            t3.add(new Integer(2));
            t3.add(new Integer(4));
            t3.add(new Integer(6));
            positions.add(t1);
            positions.add(t2);
            positions.add(t3);
        }else if (dataSet == 2){
            List<Integer> t1 = new ArrayList<Integer>();
            t1.add(new Integer(1));
            t1.add(new Integer(7));
            List<Integer> t2 = new ArrayList<Integer>();
            t2.add(new Integer(2));
            t2.add(new Integer(6));
            List<Integer> t3 = new ArrayList<Integer>();
            t3.add(new Integer(5));
            positions.add(t1);
            positions.add(t2);
            positions.add(t3);
        }else if (dataSet == 3){
            List<Integer> t1 = new ArrayList<Integer>();
            t1.add(new Integer(1));
            t1.add(new Integer(7));
            List<Integer> t2 = new ArrayList<Integer>();
            t2.add(new Integer(2));
            t2.add(new Integer(6));
            List<Integer> t3 = new ArrayList<Integer>();
            t3.add(new Integer(3));
            t3.add(new Integer(4));
            t3.add(new Integer(5));
            positions.add(t1);
            positions.add(t2);
            positions.add(t3);
        }
        return positions;
    }
    @Test
    public void testMaxValue(){
        assertEquals(this.mq.maxValue(this.testData(1)), 7);
        assertEquals(this.mq.maxValue(this.testData(2)), 7);
        assertEquals(this.mq.maxValue(this.testData(3)), 7);
    }

    @Test
    public void testminMaxValue(){
        assertEquals(this.mq.maxMinValue(this.testData(1)), 2);
        assertEquals(this.mq.maxMinValue(this.testData(2)), 5);
        assertEquals(this.mq.maxMinValue(this.testData(3)), 3);
    }

    @Test
    public void testminMinValue(){
        assertEquals(this.mq.minMinValue(this.testData(1)), 0);
        assertEquals(this.mq.minMinValue(this.testData(2)), 1);
        assertEquals(this.mq.minMinValue(this.testData(3)), 1);
    }

    private int totalSize(List<List<Integer>> positions){
        int size = 0;
        for (List<Integer> values : positions){
            size += values.size();
        }
        return size;
    }

    @Test
    public void testRemoveLowerPositions(){
        List<List<Integer>> temp = this.testData(1);
        this.mq.removeLowerPositions(temp, 3);
        assertEquals(this.totalSize(temp), 6);
        // second test
        temp = this.testData(2);
        this.mq.removeLowerPositions(temp, 3);
        assertEquals(this.totalSize(temp), 3);
        // third test
        temp = this.testData(3);
        this.mq.removeLowerPositions(temp, 3);
        assertEquals(this.totalSize(temp), 5);
    }

    @Test
    public void testMinSpanCalc(){
        assertEquals(this.mq.minSpanCalc(this.testData(1)), 0);
        assertEquals(this.mq.minSpanCalc(this.testData(2)), 2);
        assertEquals(this.mq.minSpanCalc(this.testData(3)), 2);
    }

}
