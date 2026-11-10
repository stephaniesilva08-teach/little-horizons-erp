package com.littlehorizons.erp.util;

import java.io.File;

/**
 * File and directory helpers.
 */
public final class FileUtil {

    private FileUtil() {
    }

    public static boolean ensureDirectoryExists(String path) {
        File directory = new File(path);
        return directory.exists() || directory.mkdirs();
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }

    public static String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
}
