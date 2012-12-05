package com.hdup;

import org.junit.Test;

/**
 * @author Vadim Bobrov
 */
public class ScannerTest {

    private Scanner scanner = new Scanner();

    @Test
    public void testScan() throws Exception {
        scanner.scan("a", "b", "c", 2L, 32L);
    }
}
