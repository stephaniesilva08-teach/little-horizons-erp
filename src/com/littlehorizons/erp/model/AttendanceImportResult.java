package com.littlehorizons.erp.model;

/**
 * Outcome of a bulk attendance import.
 */
public record AttendanceImportResult(Status status, int totalLines,
                                     int successful, int failed) {

    public enum Status {
        FILE_NOT_FOUND,
        INVALID_HEADER,
        COMPLETED
    }

    public static AttendanceImportResult fileNotFound() {
        return new AttendanceImportResult(Status.FILE_NOT_FOUND, 0, 0, 0);
    }

    public static AttendanceImportResult invalidHeader() {
        return new AttendanceImportResult(Status.INVALID_HEADER, 0, 0, 0);
    }

    public static AttendanceImportResult completed(int totalLines, int successful, int failed) {
        return new AttendanceImportResult(Status.COMPLETED, totalLines, successful, failed);
    }
}
