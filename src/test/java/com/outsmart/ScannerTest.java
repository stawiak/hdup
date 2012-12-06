package com.outsmart;

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
}
