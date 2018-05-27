package vkclient.vkclient.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchiver {

    private static final int BUFFER_SIZE = 1024;

    public static void zip(String[] filePaths, String zipFilePath) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFilePath)))) {
            byte data[] = new byte[BUFFER_SIZE];
            for (String filePath : filePaths) {
                FileInputStream fi = new FileInputStream(filePath);
                try (BufferedInputStream origin = new BufferedInputStream(fi, BUFFER_SIZE)) {
                    ZipEntry entry = new ZipEntry(filePath.substring(filePath.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                }
            }
        }
    }

}
