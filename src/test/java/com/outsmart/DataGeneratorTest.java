package com.outsmart;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * @author Vadim Bobrov
 */
public class DataGeneratorTest {

    private DataGenerator dataGenerator = new DataGenerator();

    @Test
    public void testGetRandomCustomer() throws Exception {
        assertTrue(dataGenerator.getRandomCustomer().startsWith("customer"));
    }

    @Test
    public void testGetCustomer() throws Exception {
        for(int i = 0; i < 20; i++)
            assertEquals(dataGenerator.getCustomer(i), "customer" + i);
    }

    @Test
    public void testGetRandomLocation() throws Exception {
        assertTrue(dataGenerator.getRandomLocation().startsWith("location"));
    }

    @Test
    public void testGetLocation() throws Exception {
        for(int i = 0; i < 2; i++)
            assertEquals(dataGenerator.getLocation(i), "location" + i);
    }

    @Test
    public void testGetRandomWireId() throws Exception {
        assertTrue(dataGenerator.getRandomWireId().startsWith("wire"));
    }

    @Test
    public void testGetWireId() throws Exception {
        for(int i = 0; i < 300; i++)
            assertEquals(dataGenerator.getWireId(i), "wire" + i);
    }

    @Test
    public void testGetRandomMeasurement() throws Exception {
        assertTrue(dataGenerator.getRandomMeasurement() > 0);
    }
}
