package com.outsmart;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vadim Bobrov
 */
public class ScannerTest {

    private Scanner scanner;

    @Before
    public void setUp() throws Exception {
        scanner = new Scanner();
    }

    @Test
    public void testScan() throws Exception {
        scanner.scan("customer2", "location2", "wire1", 2000L, 2500L);
    }

    @Test
    public void testScanDates() throws Exception {
        scanner.scan("customer1", "location1", "wire1", new DateTime("2012-01-01"), new DateTime("2012-01-03"));
    }

}
