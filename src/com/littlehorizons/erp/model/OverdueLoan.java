package com.littlehorizons.erp.model;

/** Overdue loan report row. */
public record OverdueLoan(int loanId, String resourceName, String borrowerName,
                          String borrowerType, String expectedReturnDate) {
}
