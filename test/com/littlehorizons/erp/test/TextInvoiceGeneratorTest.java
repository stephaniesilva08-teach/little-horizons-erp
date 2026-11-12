package com.littlehorizons.erp.test;

import com.littlehorizons.erp.util.TextInvoiceGenerator;
import com.littlehorizons.erp.util.DataAccessException;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

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
        suite.add("Invoice fields cannot inject extra text lines", () -> {
            String invoiceNumber = "TEST-" + UUID.randomUUID();
            TextInvoiceGenerator.GeneratedInvoice invoice = TextInvoiceGenerator.generate(
                    invoiceNumber, "Estudiante", "Acudiente\nNIT falso: 123",
                    "1020304050", "Calle 1\nTOTAL: $0", "PENSION",
                    "Pensión\nTotal alterno", 1, new BigDecimal("100000"));
            Path path = Path.of(invoice.fullPath());
            try {
                String content = Files.readString(path);
                Assert.isTrue(content.contains("Acudiente NIT falso: 123"),
                        "line breaks in names should be neutralized");
                Assert.isTrue(content.contains("Calle 1 TOTAL: $0"),
                        "line breaks in addresses should be neutralized");
                Assert.isFalse(content.contains("\nNIT falso:"),
                        "user data must not create a new invoice row");
            } finally {
                Files.deleteIfExists(path);
            }
        });
        suite.add("Invoice generation never overwrites an existing file", () -> {
            String invoiceNumber = "TEST-" + UUID.randomUUID();
            TextInvoiceGenerator.GeneratedInvoice invoice = TextInvoiceGenerator.generate(
                    invoiceNumber, "Estudiante", "Acudiente", "1020304050", "Calle 1",
                    "PENSION", "Pensión", 1, new BigDecimal("100000"));
            Path path = Path.of(invoice.fullPath());
            try {
                Files.writeString(path, "preserve-existing-content");
                Assert.assertThrows(DataAccessException.class,
                        () -> TextInvoiceGenerator.generate(invoiceNumber, "Otro", "Otro",
                                "1020304051", "Otra calle", "PENSION", "Pensión", 1,
                                new BigDecimal("100000")),
                        "an existing invoice path must fail instead of being replaced");
                Assert.equals("preserve-existing-content", Files.readString(path),
                        "the original invoice must remain intact");
            } finally {
                Files.deleteIfExists(path);
            }
        });
    }
}
