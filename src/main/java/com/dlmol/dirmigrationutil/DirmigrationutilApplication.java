package com.dlmol.dirmigrationutil;

import ch.qos.logback.core.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class DirmigrationutilApplication {

    public static void main(String[] args) throws IOException {
        args = new String[]{"/Volumes/Temp/D7100/", "/Volumes/Photos/"};
        SpringApplication.run(DirmigrationutilApplication.class, args);
        System.out.println("args: " + Arrays.asList(args).stream().collect(Collectors.joining(", ")));
        if (args == null || args.length < 2){
            System.out.println("Invalid args!");
            return;
        }
        File source = new File(args[0]);
        File target = new File(args[1]);
        compareAndClean(source, target);
    }

    private static void compareAndClean(File sourceDir, File targetDir) throws IOException {
       long startMs = System.currentTimeMillis();
        if (!sourceDir.isDirectory()){
            System.out.println(sourceDir.getAbsolutePath() + " is NOT a valid directory!");
            return;
        }
        if (!targetDir.isDirectory()){
            System.out.println(targetDir.getAbsolutePath() + " is NOT a valid directory!");
            return;
        }
        System.out.println("Source dir: " + sourceDir.getAbsolutePath() +
                "\nTarget dir: " + targetDir.getAbsolutePath());
        Set<File> sourceFiles = Files.walk(sourceDir.toPath())
                .map(Path::toFile)
                .filter(f -> f.isFile())
                .collect(Collectors.toSet());
        System.out.println("Found " + sourceFiles.size() + " files in source (" + sourceDir.getAbsolutePath() + ")");

        Set<File> targetFiles = Files.walk(targetDir.toPath())
                .map(Path::toFile)
                .filter(f -> f.isFile())
                .collect(Collectors.toSet());
        Set<String> targetFileNames = targetFiles.stream().map(File::getName).collect(Collectors.toSet());

        System.out.println("Found " + targetFiles.size() + " files in target (" + targetDir.getAbsolutePath() + ")");
        Set<File> filesToRemove = sourceFiles.stream()
                .filter(file -> targetFileNames.contains(file.getName()))
                .collect(Collectors.toSet());
        System.out.println(filesToRemove.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")) +
                "\n" + filesToRemove.size() + " above files to be removed from source.");
        System.out.println((sourceFiles.size() - filesToRemove.size()) + " remaining files in source.");

        Set<File> filesToReview = sourceFiles.stream()
                .filter(file -> !targetFileNames.contains(file.getName()))
                .collect(Collectors.toSet());
        System.out.println(filesToReview.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")) +
                "\n" + filesToReview.size() + " above files to be reviewed/moved into collections.");
        File reviewDir = new File(sourceDir.getAbsolutePath() + File.separator + "review");
        if (!reviewDir.isDirectory())
            reviewDir.mkdir();
        System.out.println("Copying " + filesToReview.size() + " files to: " + reviewDir.getAbsolutePath());
        filesToReview.forEach(f -> {
            try {
                System.out.println("\tCopying " + f.getAbsolutePath() + " to " + reviewDir.getAbsolutePath());
                FileUtils.copyToDirectory(f, reviewDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Done after: " + (System.currentTimeMillis() - startMs) + " ms.");
    }

}
