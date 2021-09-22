package com.dlmol.dirmigrationutil.util;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtil {
    public static String getMd5Checksum(File file) {
        long startMs = System.currentTimeMillis();
        if (file == null) {
            System.out.println("getMd5Checksum(): File is null!");
            return null;
        } else if (file.exists() == false){
            System.out.println("getMd5Checksum(): File '" + file.getAbsolutePath() + "' does NOT exist!");
            return null;
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("getMd5Checksum(): Unable to call 'MessageDigest.getInstance(\"MD5\")'. " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        try {
            md.update(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            System.out.println("getMd5Checksum(): Unable to call 'md.update(Files.readAllBytes(file.toPath()));' " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        byte[] digest = md.digest();
        String checksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
        System.out.println("getMd5Checksum(): For file '" + file.getAbsolutePath() + "' returning checksum: " + checksum +
                ". Done in: " + (System.currentTimeMillis() - startMs) + " ms.");
        return checksum;
    }
}
