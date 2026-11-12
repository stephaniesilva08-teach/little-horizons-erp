package com.littlehorizons.erp.test;

import com.littlehorizons.erp.util.TextInvoiceGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tests for the pure helpers of the text invoice generator.
 */
public final class TextInvoiceGeneratorTest {

    private TextInvoiceGeneratorTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("CUFE is a 64 character SHA-256 hash", () -> {
            String cufe = TextInvoiceGenerator.buildCufe(
                    "FAC-2026-00001", LocalDate.of(2026, 11, 12));
            Assert.equals(64, cufe.length(), "CUFE length");
            Assert.isTrue(cufe.matches("[0-9a-f]{64}"), "CUFE must be lowercase hex");
        });
        suite.add("CUFE changes with the invoice number", () -> {
            LocalDate date = LocalDate.of(2026, 11, 12);
            String first = TextInvoiceGenerator.buildCufe("FAC-2026-00001", date);
            String second = TextInvoiceGenerator.buildCufe("FAC-2026-00002", date);
            Assert.isFalse(first.equals(second), "different invoices need different CUFE");
        });
        suite.add("CUFE is deterministic for the same invoice", () -> {
            LocalDate date = LocalDate.of(2026, 11, 12);
            Assert.equals(
                    TextInvoiceGenerator.buildCufe("FAC-2026-00007", date),
                    TextInvoiceGenerator.buildCufe("FAC-2026-00007", date),
                    "same input must produce the same CUFE");
        });
        suite.add("Currency is formatted with Colombian separators", () -> {
            Assert.equals("$1.234,50",
                    TextInvoiceGenerator.formatCurrency(new BigDecimal("1234.5")),
                    "thousands and decimals");
            Assert.equals("$0,00",
                    TextInvoiceGenerator.formatCurrency(null),
                    "null amounts are formatted as zero");
        });
    }
}
