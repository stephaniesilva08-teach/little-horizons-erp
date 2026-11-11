package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/** Student grade report row. */
public record StudentGradeReport(int studentId, String studentName, String subjectName,
                                 int academicYear, String period, BigDecimal grade) {
}
