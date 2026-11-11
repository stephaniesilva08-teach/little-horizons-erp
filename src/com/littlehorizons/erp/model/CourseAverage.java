package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/** Average grade for a course (subject + grade + section). */
public record CourseAverage(int subjectId, String subjectName, int gradeId,
                            String section, BigDecimal average, int students) {
}
