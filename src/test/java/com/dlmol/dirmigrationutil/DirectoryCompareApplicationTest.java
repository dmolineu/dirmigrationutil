package com.dlmol.dirmigrationutil;

import junit.framework.TestCase;

public class DirectoryCompareApplicationTest extends TestCase {

    public void testPrintProgress() {
        assertEquals("Processing file #10 of 10000, ~0%. Elapsed time: 10001 ms. Est. time remaining: 166:31",
            DirectoryCompareApplication.printProgress(10, 10000, System.currentTimeMillis() - 10000));
    }
}