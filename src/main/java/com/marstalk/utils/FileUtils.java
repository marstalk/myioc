package com.marstalk.utils;

import java.io.File;
import java.util.Set;

public class FileUtils {

    public static void listAllFiles(File dir, Set<File> files) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                listAllFiles(f, files);
            }
        } else {
            files.add(dir);
        }
    }

    public static String rootPath() {
        String rootPath = System.getProperty("user.dir");
        return new StringBuilder(rootPath).append(File.separator)
                .append("target").append(File.separator).append("classes")
                .append(File.separator).toString();
    }

}