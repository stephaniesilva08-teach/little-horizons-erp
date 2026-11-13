package com.littlehorizons.erp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple file logger used by the whole application.
 */
public final class LoggerUtil {

    private static final String LOG_FILE = "error_log.txt";
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LoggerUtil() {
    }

    public static void info(String message) {
        log("INFO", sanitize(message));
    }

    public static void warn(String message) {
        log("WARN", sanitize(message));
    }

    public static void debug(String message) {
        log("DEBUG", sanitize(message));
    }

    public static void error(String message, Throwable throwable) {
        String detail = throwable == null ? ""
                : " - Exception type: " + throwable.getClass().getName();
        log("ERROR", sanitize(message + detail));
        if (throwable != null) {
            StringBuilder trace = new StringBuilder("Stack frames:");
            for (StackTraceElement frame : throwable.getStackTrace()) {
                trace.append(" at ").append(frame);
            }
            Throwable cause = throwable.getCause();
            if (cause != null && cause != throwable) {
                trace.append(" | Cause type: ").append(cause.getClass().getName());
                for (StackTraceElement frame : cause.getStackTrace()) {
                    trace.append(" at ").append(frame);
                }
            }
            log("ERROR", sanitize(trace.toString()));
        }
    }

    public static String getLogFile() {
        return LOG_FILE;
    }

    private static synchronized void log(String level, String message) {
        String entry = "[" + LocalDateTime.now().format(FORMAT) + "] ["
                + level + "] " + message + System.lineSeparator();
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(entry);
        } catch (IOException e) {
            System.err.println("Could not write to log file: " + e.getMessage());
            return;
        }
        restrictPermissions();
    }

    /** Prevents log forging through embedded line breaks or tabs. */
    private static String sanitize(String message) {
        if (message == null) {
            return "";
        }
        String clean = message.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
        return clean.length() > 1000 ? clean.substring(0, 1000) + "..." : clean;
    }

    /** Logs may contain personal data, so only the owner can read them. */
    private static void restrictPermissions() {
        try {
            Files.setPosixFilePermissions(Path.of(LOG_FILE),
                    PosixFilePermissions.fromString("rw-------"));
        } catch (IOException | UnsupportedOperationException ignored) {
            // Non-POSIX file systems do not support this restriction.
        }
    }
}
