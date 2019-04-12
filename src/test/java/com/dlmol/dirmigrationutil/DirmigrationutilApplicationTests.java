package com.dlmol.dirmigrationutil;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DirMigrationUtilApplicationTests {

    @Test
    public void readFromNas() throws IOException {
        File source = new File("/Volumes/SystemStuff/Windows7/en_windows_7_professional_with_sp1_x64_dvd_u_676939.iso"); // 3GB
//        File source = new File("/Volumes/Videos/GOPR0107_1492876862683_high.MP4"); // 179 MB
        File targetDir = new File("/Users/dmolineu/Desktop/");
        testCopyFile(source, targetDir);
    }

    @Test
    public void writeToNas() throws IOException {
//        File source = new File("/Users/dmolineu/Movies/Family 001.avi");
        File source = new File("/Users/dmolineu/Movies/Desktop/en_windows_7_professional_with_sp1_x64_dvd_u_676939.iso");
        File targetDir = new File("/Volumes/Temp/");
        testCopyFile(source, targetDir);
    }

    @Test
    public void toAndFromNas() throws IOException {
        File source = new File("/Volumes/SystemStuff/Windows7/en_windows_7_professional_with_sp1_x64_dvd_u_676939.iso"); // 3GB
//        File source = new File("/Volumes/Videos/GOPR0107_1492876862683_high.MP4"); // 179 MB
        File targetDir = new File("/Users/dmolineu/Desktop/");
        testCopyFile(source, targetDir);

//        File source = new File("/Users/dmolineu/Movies/Family 001.avi");
        source = new File("/Users/dmolineu/Movies/Desktop/en_windows_7_professional_with_sp1_x64_dvd_u_676939.iso");
        targetDir = new File("/Volumes/Temp/");
        testCopyFile(source, targetDir);
    }

    private void testCopyFile(File source, File targetDir) throws IOException {
        assertTrue(source.getAbsolutePath() + " NOT found!", source.isFile());
        assertTrue(targetDir.getAbsolutePath() + " is NOT a valid directory!", targetDir.isDirectory());
        File targetFile = new File (targetDir.getAbsolutePath() + File.separator + source.getName());
        assertFalse(targetFile.getAbsolutePath() + " already exists!", targetFile.exists());
        System.out.println("Starting copy of " + (source.length() / 1000) + " KB file from " + source.getAbsolutePath() +
                "  to " + targetDir.getAbsolutePath());
        long startMs = System.currentTimeMillis();
        FileUtils.copyToDirectory(source, targetDir);
        final long durationMs = System.currentTimeMillis() - startMs;
        System.out.println("Copy done in " + durationMs + " ms, " + (source.length() / durationMs) + " KB/s");
        assertTrue(targetFile.getAbsolutePath() + " does NOT actually exist!", targetFile.exists());
        assertEquals("source size is " + source.length() + ", but target size is " + targetFile.length(),
                source.length(), targetFile.length());
        assertTrue("Unable to delete: " + targetFile.getAbsolutePath(), targetFile.delete());
    }

}
