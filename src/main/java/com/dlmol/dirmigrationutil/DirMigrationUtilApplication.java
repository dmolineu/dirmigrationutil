package com.dlmol.dirmigrationutil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SpringBootApplication
public class DirMigrationUtilApplication {

    public static void main(String[] args) throws IOException {
        args = new String[]{"/Users/dmolineu/Pictures/", "/Volumes/dls_docs/My Pictures/", "/Volumes/Photos/"};
        SpringApplication.run(DirMigrationUtilApplication.class, args);
        System.out.println("args: " + Arrays.asList(args).stream().collect(Collectors.joining(", ")));
        if (args == null || args.length < 2) {
            System.out.println("Invalid args!");
            return;
        }

        boolean copy = false;
        if (copy)
            smartCopy(new File("/Volumes/NIKON D7100/DCIM/"), new File("/Volumes/Temp/D7100/"));
        else {
            File source = new File(args[0]);
            File target = new File(args[1]);
            File target2 = new File(args[2]);
            compareAndClean(source, target, target2);
        }
    }

    private static void smartCopy(File source, File target) throws IOException {
        long startMs = System.currentTimeMillis();
        if (!source.isDirectory()) {
            System.out.println(source.getAbsolutePath() + " is NOT a valid directory!");
            return;
        }
        if (!target.isDirectory()) {
            System.out.println(target.getAbsolutePath() + " is NOT a valid directory!");
            return;
        }
        Set<File> sourceFiles = Files.walk(source.toPath())
                .map(Path::toFile)
                .filter(f -> f.isFile())
                .collect(Collectors.toSet());
        System.out.println("Found " + sourceFiles.size() + " files in source (" + source.getAbsolutePath() + ") in " +
                (System.currentTimeMillis() - startMs) / 1000 + " seconds.");

        Set<File> targetFiles = Files.walk(target.toPath())
                .map(Path::toFile)
                .filter(f -> f.isFile())
                .collect(Collectors.toSet());
        System.out.println("Found " + targetFiles.size() + " files in target (" + target.getAbsolutePath() + ") in " +
                (System.currentTimeMillis() - startMs) / 1000 + " total seconds.");

        sourceFiles.forEach(file -> {
            File testTarget = new File(target.getAbsolutePath() + File.separator + file.getName());
            boolean copy = false;
            if (testTarget.exists()) {
//                System.out.println("\tTarget file exists: " + testTarget.getAbsolutePath());
                if (testTarget.length() == 0) {
                    System.out.println("\tTarget file is empty: " + testTarget.getAbsolutePath());
                    copy = true;
                }
            } else {
                copy = true;
            }
            if (copy) {
                System.out.println("\tCopying " + file.getAbsolutePath() + " to " + testTarget.getAbsolutePath());
                try {
                    FileUtils.copyToDirectory(file, target);
                } catch (IOException e) {
                    System.out.println("Unable to copy file '" + file.getAbsolutePath() + "' to '" + target.getAbsolutePath() +
                            "'. Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        });

        System.out.println("smartCopy(): Done after " + (System.currentTimeMillis() - startMs) / 1000 + " seconds.");
    }

    private static void compareAndClean(File sourceDir, File targetDir1) throws IOException {
        compareAndClean(sourceDir, targetDir1, null);
    }

    private static void compareAndClean(File sourceDir, File targetDir1, File targetDir2) throws IOException {
        long startMs = System.currentTimeMillis();
        if (!sourceDir.isDirectory()) {
            System.out.println(sourceDir.getAbsolutePath() + " is NOT a valid directory!");
            return;
        }
        if (!targetDir1.isDirectory()) {
            System.out.println(targetDir1.getAbsolutePath() + " is NOT a valid directory!");
            return;
        }
        System.out.println("\nSource dir: " + sourceDir.getAbsolutePath() +
                "\nTarget dir: " + targetDir1.getAbsolutePath());
        if (targetDir2 != null)
            System.out.println("Target dir #2: " + targetDir2.getAbsolutePath() + "\n");

        final Predicate<File> imageFilter = getImageFilter();

        long ms;

        boolean error = false;
        Set<File> targetFiles = new HashSet<>();
        int i = 1;
        do {
            System.out.println("Attempt #" + (i++) + " for " + targetDir1.getAbsolutePath());
            ms = System.currentTimeMillis();
            try {
                targetFiles = getFilteredFiles(targetDir1, imageFilter);
                System.out.println("Found " + targetFiles.size() + " files in: " + targetDir1.getAbsolutePath() + ", " +
                        (System.currentTimeMillis() - ms) + " ms.");
                error = false;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                error = true;
            }
        } while (error);

        if (targetDir2 != null) {
            ms = System.currentTimeMillis();
            final Set<File> target2Files = getFilteredFiles(targetDir2, imageFilter);
            System.out.println("Found " + target2Files.size() + " files in: " + targetDir2.getAbsolutePath() + ", " +
                    (System.currentTimeMillis() - ms) + " ms.");
            targetFiles.addAll(target2Files);
            System.out.println("Total target file count: " + targetFiles.size());
        }

        Set<String> targetFileNames = targetFiles.stream().map(File::getName).collect(Collectors.toSet());

        /*
        Set<File> filesToRemove = sourceFiles.stream()
                .filter(file -> targetFileNames.contains(file.getName()))
                .collect(Collectors.toSet());
        System.out.println(filesToRemove.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")) +
                "\n\n" + filesToRemove.size() + " above files to be removed from source.");
        System.out.println((sourceFiles.size() - filesToRemove.size()) + " remaining files in source.");
*/

        ms = System.currentTimeMillis();
        Set<File> sourceFiles = getFilteredFiles(sourceDir, imageFilter);
        System.out.println("Found " + sourceFiles.size() + " files in source (" + sourceDir.getAbsolutePath() + "), " +
                (System.currentTimeMillis() - ms) + " ms.");

        Set<File> filesToReview = sourceFiles.stream()
                .filter(file -> !targetFileNames.contains(file.getName()))
                .collect(Collectors.toSet());
        System.out.println(filesToReview.stream()
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.joining("\n")) +
                "\n" + filesToReview.size() + " above files to be reviewed/moved into collections.");

        /*
        File reviewDir = new File(sourceDir.getParentFile().getAbsolutePath() + File.separator + "review");
        if (!reviewDir.isDirectory())
            reviewDir.mkdir();
        System.out.println("Copying " + filesToReview.size() + " files to: " + reviewDir.getAbsolutePath());
        filesToReview.forEach(f -> {
            try {
                System.out.print("\tCopying " + f.getAbsolutePath() + " to " + reviewDir.getAbsolutePath());
                long ms = System.currentTimeMillis();
                FileUtils.copyToDirectory(f, reviewDir);
                final long durationMs = System.currentTimeMillis() - ms;
                System.out.println("\tDone after " + durationMs + " ms, KBs: " +
                        f.length() / durationMs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
*/
        System.out.println("Done after: " + ((System.currentTimeMillis() - startMs) / 1000) + " seconds.");
    }

    public static Set<File> getFilteredFiles(File targetDir, Predicate<File> fileFilter) throws IOException {
        return Files.walk(targetDir.toPath())
                .map(Path::toFile)
                .filter(fileFilter)
                .collect(Collectors.toSet());
    }

    public static Predicate<File> getImageFilter() {
        final Predicate<File> imageFilter = f -> f.isFile() &&
                (FilenameUtils.getExtension(f.getAbsolutePath()).toLowerCase().contains("jpg") ||
                        FilenameUtils.getExtension(f.getAbsolutePath()).toLowerCase().contains("nef"));
        return imageFilter;
    }

}
