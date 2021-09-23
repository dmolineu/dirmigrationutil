package com.dlmol.dirmigrationutil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dlmol.dirmigrationutil.util.ChecksumUtil.getMd5Checksum;

@SpringBootApplication
public class DirectoryCompareApplication {

    private static MessageDigest md5Digest = null;

    public static void main(String[] args) throws IOException {
        args = new String[]{"D:\\DCIM", "\\\\192.168.1.4\\Photos"};
        SpringApplication.run(DirectoryCompareApplication.class, args);
        findImageFilesMissingInTarget(new File(args[0]), new File(args[1]));
    }

    public static List<File> findImageFilesMissingInTarget(File sourceDir, File targetDir) throws IOException {
        long startMs = System.currentTimeMillis();
        List<File> filesNotInTarget = new ArrayList<>();
        if (!sourceDir.isDirectory()) {
            System.out.println(sourceDir.getAbsolutePath() + " is NOT a valid directory!");
            return filesNotInTarget;
        }
        if (!targetDir.isDirectory()) {
            System.out.println(targetDir.getAbsolutePath() + " is NOT a valid directory!");
            return filesNotInTarget;
        }
        System.out.println("\nSource dir: " + sourceDir.getAbsolutePath() +
                "\nTarget dir: " + targetDir.getAbsolutePath());

        System.out.println("Getting list of files from source...");
        Set<File> sourceFiles = DirMigrationUtilApplication.getFilteredFiles(sourceDir, DirMigrationUtilApplication.getImageFilter());
        System.out.println("Found " + sourceFiles.size() + " files in source.");
        System.out.println("Hashing source files...");
        final Map<String, HashedFile> hashedSourceFiles = getHashedFiles(sourceFiles, true);
        final Set<String> sourceFileNames = hashedSourceFiles.values().stream().map(h -> h.getName()).collect(Collectors.toSet());

        System.out.println("Getting list of files from target...");
        Set<File> targetFiles = DirMigrationUtilApplication.getFilteredFiles(targetDir, DirMigrationUtilApplication.getImageFilter(), sourceFileNames);
        System.out.println("Found " + targetFiles.size() + " files in target.");
        System.out.println("Hashing target files...");
        Map<String, HashedFile> hashedTargetFiles = getHashedFiles(targetFiles, sourceFileNames, true);

        System.out.println("Looking for files in source missing in target...");
        List<HashedFile> sourceFilesMissingInTarget = hashedSourceFiles.keySet().stream()
                        .filter(checksum -> !hashedTargetFiles.containsKey(checksum)) //File already exists in target if checksum matches a file
                        .map(checksum -> hashedSourceFiles.get(checksum))
                                .collect(Collectors.toList());
        System.out.println("Found " + sourceFilesMissingInTarget.size() + " files in source missing in target:");
        sourceFilesMissingInTarget.stream()
                .map(h -> h.getFile().getAbsolutePath() + " (" + h.getChecksum() + ")")
                .sorted()
                .forEach(System.out::println);

        System.out.println("Done after " + (System.currentTimeMillis() - startMs) + " ms.");
        return filesNotInTarget;
    }

    private static Map<String, HashedFile> getHashedFiles(Set<File> files, boolean usePersistentChecksumFiles) {
        long ms = System.currentTimeMillis();
        HashMap<String, HashedFile> hashedFiles = new HashMap(files.size());
        List<File> fileList = new ArrayList<>(files);
        for (int i = 0; i < fileList.size(); i++) {
            printProgress(i, files.size(), ms);
            File f = fileList.get(i);
            final String md5checksum = getMd5Checksum(f, usePersistentChecksumFiles);
            hashedFiles.put(md5checksum, new HashedFile(f, f.getName(), md5checksum, f.length()));
        }
        System.out.println("getHashedFiles(): Took " + (System.currentTimeMillis() - ms) + " ms.");
        return hashedFiles;
    }

    //Get HashedFile Map, only include files where names match
    private static Map<String, HashedFile> getHashedFiles(Set<File> files, Set<String> fileNames, boolean usePersistentChecksumFiles) {
        long ms = System.currentTimeMillis();
        HashMap<String, HashedFile> hashedFiles = new HashMap();
        List<File> fileList = new ArrayList<>(files);
        for (int i = 0; i < fileList.size(); i++) {
            printProgress(i, files.size(), ms);
            File f = fileList.get(i);
            if (fileNames.contains(f.getName())){
                final String md5checksum = getMd5Checksum(f, usePersistentChecksumFiles);
                hashedFiles.put(md5checksum, new HashedFile(f, f.getName(), md5checksum, f.length()));
            } else {
                System.out.println("Skipping " + f.getAbsolutePath() + " because its name is not in the list of names from source files.");
            }
        }
        System.out.println("Hashed " + hashedFiles.size() + " (names matched those from source) of " + files.size() +
                " total files found in target.");
        System.out.println("getHashedFiles(): Took " + (System.currentTimeMillis() - ms) + " ms.");
        return hashedFiles;
    }

    protected static String printProgress(int currentIndex, int size, long ms) {
        if (currentIndex == 0 || currentIndex % 10 != 0) {
            return null;
        }
        final long elapsed = System.currentTimeMillis() - ms;
        final BigDecimal msPerItem = new BigDecimal (((double) elapsed) / currentIndex);
        final BigDecimal secsRemaining = msPerItem.multiply(BigDecimal.valueOf(size - currentIndex)).divide(BigDecimal.valueOf(1000));
        final BigDecimal pct = new BigDecimal (((double) currentIndex) * 100 / size);
        final long remainingMins = secsRemaining.intValue() / 60;
        final long remainingSecs = secsRemaining.intValue() % 60;
        final String progressString = "Processing file #" + currentIndex + " of " + size + ", ~" + pct.intValue() + "%. Elapsed time: " +
                elapsed + " ms. Estimated time remaining: " + remainingMins + ":" + remainingSecs;
        System.out.println(progressString);
        return progressString;
    }
}

@AllArgsConstructor
@Getter
@Setter
class HashedFile  {
    private File file;
    private String name;
    private String checksum;
    private Long length;
}