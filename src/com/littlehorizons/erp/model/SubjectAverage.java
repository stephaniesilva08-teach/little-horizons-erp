package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/** Average grade obtained by a student in a subject for one academic year. */
public record SubjectAverage(int subjectId, String subjectName, int academicYear,
                             BigDecimal average, int records) {
}
