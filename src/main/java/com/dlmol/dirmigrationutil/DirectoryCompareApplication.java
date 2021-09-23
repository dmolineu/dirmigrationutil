package com.dlmol.dirmigrationutil;

import com.dlmol.dirmigrationutil.util.ChecksumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class DirectoryCompareApplication {

    private static MessageDigest md5Digest = null;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        args = new String[]{"D:\\DCIM", "\\\\192.168.1.4\\Photos"};
        SpringApplication.run(DirectoryCompareApplication.class, args);
        findImageFilesMissingInTarget(new File(args[0]), new File(args[1]));
    }

    public static List<File> findImageFilesMissingInTarget(File sourceDir, File targetDir) throws IOException, NoSuchAlgorithmException {
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
        final Map<String, HashedFile> hashedSourceFiles = getHashedFiles(sourceFiles);
        final Set<String> sourceFileNames = hashedSourceFiles.values().stream().map(h -> h.getName()).collect(Collectors.toSet());

        System.out.println("Getting list of files from target...");
        Set<File> targetFiles = DirMigrationUtilApplication.getFilteredFiles(targetDir, DirMigrationUtilApplication.getImageFilter(), sourceFileNames);
        System.out.println("Found " + targetFiles.size() + " files in target.");
        System.out.println("Hashing target files...");
        Map<String, HashedFile> hashedTargetFiles = getHashedFiles(targetFiles, sourceFileNames);

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

    private static Map<String, HashedFile> getHashedFiles(Set<File> files) {
        long ms = System.currentTimeMillis();
        HashMap<String, HashedFile> hashedFiles = new HashMap(files.size());
        List<File> fileList = new ArrayList<>(files);
        for (int i = 0; i < fileList.size(); i++) {
            printProgress(files.size(), ms, i);
            File f = fileList.get(i);
            final String md5checksum = ChecksumUtil.getMd5Checksum(f);
            hashedFiles.put(md5checksum, new HashedFile(f, f.getName(), md5checksum, f.length()));
        }
        System.out.println("getHashedFiles(): Took " + (System.currentTimeMillis() - ms) + " ms.");
        return hashedFiles;
    }

    //Get HashedFile Map, only include files where names match
    private static Map<String, HashedFile> getHashedFiles(Set<File> files, Set<String> fileNames) {
        long ms = System.currentTimeMillis();
        HashMap<String, HashedFile> hashedFiles = new HashMap();
        List<File> fileList = new ArrayList<>(files);
        for (int i = 0; i < fileList.size(); i++) {
            printProgress(files.size(), ms, i);
            File f = fileList.get(i);
            if (fileNames.contains(f.getName())){
                final String md5checksum = ChecksumUtil.getMd5Checksum(f);
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

    private static void printProgress(int size, long ms, int i) {
        if (i % 10 != 0) {
            return;
        }
        long elapsed = System.currentTimeMillis() - ms;
        int pct = i * 100 / size;
        long remainingMins = 100 / pct * elapsed / 1000 / 60;
        long remainingSecs = (100 / pct * elapsed / 1000) % 60;
        System.out.println("Processing file #" + i + " of " + size + ", ~" + pct +
                "%. Elapsed time: " + elapsed + " ms. Est. second remaining: " + remainingMins +
                " minutes and " + remainingSecs + " seconds.");
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