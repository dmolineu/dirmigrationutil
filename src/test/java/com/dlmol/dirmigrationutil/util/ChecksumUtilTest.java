package com.dlmol.dirmigrationutil.util;

import junit.framework.TestCase;

import java.io.File;

public class ChecksumUtilTest extends TestCase {

    public void testGetMd5Checksum() {
        System.out.println(ChecksumUtil.getMd5Checksum(new File("D:\\DCIM\\112D7100\\_DLM9575.JPG")));
        System.out.println(ChecksumUtil.getMd5Checksum(new File("\\\\192.168.1.4\\Photos\\Zev\\20200926 30-week\\_DLM9575.JPG")));
    }
}