package com.littlehorizons.erp.model;

import java.time.LocalDate;

/** Weekly attendance alert for a student with recurrent absences. */
public class AttendanceAlert {

    private int studentId;
    private String studentName;
    private LocalDate weekStart;
    private int total;

    public AttendanceAlert() {
    }

    public AttendanceAlert(int studentId, String studentName, LocalDate weekStart, int total) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.weekStart = weekStart;
        this.total = total;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    @Override
    public String toString() {
        return studentName + " -> " + total + " absence(s) in week " + weekStart;
    }
}
