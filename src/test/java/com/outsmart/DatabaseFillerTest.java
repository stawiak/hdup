package com.outsmart;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vadim Bobrov
 */
public class DatabaseFillerTest {

    private DatabaseFiller databaseFiller;
    private DataGenerator dataGenerator;


    @Before
    public void setUp() throws Exception {
        databaseFiller = new DatabaseFiller();
        dataGenerator = new DataGenerator();
    }

    @Test
    public void testFillRandom() throws Exception {
        databaseFiller.fillRandom(100000);
    }

    @Test
    public void testFill() throws Exception {
        databaseFiller.fill("customer2", "location2", "wire1", 1000000);
    }

    @Test
    public void testFillEven() throws Exception {
        //for(int i = 1; i < 31; i++)
            databaseFiller.fillEven(new DateTime("2012-01-02"), new DateTime("2012-01-03"), 2);
    }

}
