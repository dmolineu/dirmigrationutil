package com.dlmol.dirmigrationutil.util;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtil {

    private static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String getMd5Checksum(File file) {
        long startMs = System.currentTimeMillis();
        if (file == null) {
            System.out.println("getMd5Checksum(): File is null!");
            return null;
        } if (!file.exists()){
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (file.exists()){
                    break;
                }
            }
            if (!file.exists()) {
                System.out.println("getMd5Checksum(): File '" + file.getAbsolutePath() + "' does NOT exist!");
                return null;
            }
        }
        if (md == null) {
            System.out.println("getMd5Checksum(" + file.getAbsolutePath() + "): MessageDigest is null!");
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
