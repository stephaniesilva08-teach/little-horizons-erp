package com.littlehorizons.erp.model;

/** Attendance summary row. */
public record AttendanceSummary(int studentId, String studentName,
                                String state, int total) {
}
