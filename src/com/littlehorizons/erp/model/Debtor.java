package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/** Debtor report row. */
public record Debtor(int studentId, String studentName, BigDecimal charged,
                     BigDecimal paid, BigDecimal balance) {
}
