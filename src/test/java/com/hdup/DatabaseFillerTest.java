package com.hdup;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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

}
