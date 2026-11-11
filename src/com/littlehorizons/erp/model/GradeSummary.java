package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/** Grade summary by subject and period. */
public record GradeSummary(String subjectName, String period, BigDecimal average,
                           BigDecimal maximum, BigDecimal minimum, int records) {
}
