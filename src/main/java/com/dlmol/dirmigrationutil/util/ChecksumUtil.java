package com.dlmol.dirmigrationutil.util;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtil {

    private static final String MD5_EXTENSION = ".md5";

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

    public static String getMd5Checksum(File f, boolean usePersistentChecksumFiles) {
        String md5checksum;
        if (usePersistentChecksumFiles) {
            if (doesChecksumFileExist(f)) {
                md5checksum = getMd5ChecksumFromChecksumFile(f);
                if (md5checksum == null) {
                    md5checksum = ChecksumUtil.getMd5Checksum(f);
                }
            } else {
                md5checksum = ChecksumUtil.getMd5Checksum(f);
                writeChecksumFile(f.getAbsolutePath(), md5checksum);
            }
        } else {
            md5checksum = ChecksumUtil.getMd5Checksum(f);
        }
        return md5checksum;
    }

    private static boolean doesChecksumFileExist(File rootFile) {
        File checksumFile = new File(getMd5FilePath(rootFile));
        return checksumFile.exists() && checksumFile.length() > 0;
    }

    private static String getMd5ChecksumFromChecksumFile(File rootFile) {
        try {
            return Files.readAllLines(Paths.get(getMd5FilePath(rootFile))).get(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writeChecksumFile(String rootFileAbsolutePath, String md5checksum) {
        if (StringUtils.isBlank(rootFileAbsolutePath)) {
            System.out.println("writeChecksumFile(): rootFileAbsolutePath is blank!");
            return;
        }
        if (StringUtils.isBlank(md5checksum)) {
            System.out.println("writeChecksumFile(): md5checksum is blank!");
            return;
        }
        final String md5FilePath = getMd5FilePath(rootFileAbsolutePath);
        try {
            Path md5Path = Paths.get(md5FilePath);
            if (md5FilePath.endsWith(MD5_EXTENSION)) {
                Files.write(md5Path, md5checksum.getBytes());
            } else {
                System.out.println("ERROR: MD5 File Path does not end with expected extension!  " + md5Path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getMd5FilePath(String rootFileAbsolutePath) {
        return rootFileAbsolutePath.concat(MD5_EXTENSION);
    }

    private static String getMd5FilePath(File rootFile) {
        return getMd5FilePath(rootFile.getAbsolutePath());
    }
}
