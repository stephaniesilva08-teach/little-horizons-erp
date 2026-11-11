package com.littlehorizons.erp.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

/**
 * Generates the visual invoice as a plain-text file.
 *
 * <p>The academic project requires a visual electronic invoice. Per the
 * client request for this delivery, the output format is plain text (.txt).
 * The file keeps all DIAN-related data: school information, client data,
 * consecutive number, items, subtotal, VAT (when applicable), total, a
 * simulated CUFE and a simulated QR payload.</p>
 */
public final class TextInvoiceGenerator {

    private static final String SCHOOL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    private static final String SCHOOL_NIT = "8927456321";
    private static final String SCHOOL_ADDRESS = "Calle 12 #34-56, Bogotá, Colombia";
    private static final String SCHOOL_PHONE = "+57 1 234 5678";
    private static final String SCHOOL_EMAIL = "soporte@littlehorizons.edu.co";
    private static final BigDecimal VAT_RATE = new BigDecimal("0.19");
    private static final Set<String> EXEMPT_CODES = Set.of("MAT", "PENSION");
    private static final String INVOICE_DIR = "facturas";

    private TextInvoiceGenerator() {
    }

    public record GeneratedInvoice(String invoiceNumber, String cufe,
                                   String fileName, String fullPath) {
    }

    public static GeneratedInvoice generate(String invoiceNumber,
                                            String studentName,
                                            String guardianName,
                                            String guardianDocument,
                                            String conceptCode,
                                            String conceptName,
                                            int quantity,
                                            BigDecimal amountPaid) {
        try {
            Path directory = Path.of(INVOICE_DIR);
            Files.createDirectories(directory);

            boolean taxable = !EXEMPT_CODES.contains(conceptCode.toUpperCase());
            BigDecimal total = amountPaid.setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal;
            BigDecimal vat;
            if (taxable) {
                subtotal = total.divide(BigDecimal.ONE.add(VAT_RATE), 2, RoundingMode.HALF_UP);
                vat = total.subtract(subtotal);
            } else {
                subtotal = total;
                vat = BigDecimal.ZERO.setScale(2);
            }
            BigDecimal unitPrice = quantity == 0
                    ? subtotal
                    : subtotal.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);

            LocalDate issueDate = LocalDate.now();
            String cufe = buildCufe(invoiceNumber, issueDate);
            String fileName = "factura_" + sanitize(invoiceNumber) + ".txt";
            Path fullPath = directory.resolve(fileName);

            String content = buildContent(invoiceNumber, studentName, guardianName,
                    guardianDocument, conceptCode, conceptName, quantity,
                    unitPrice, subtotal, vat, total, cufe, issueDate);
            Files.writeString(fullPath, content, StandardCharsets.UTF_8);
            restrictPermissions(fullPath);

            LoggerUtil.info("Invoice generated: " + fullPath.toAbsolutePath());
            return new GeneratedInvoice(invoiceNumber, cufe, fileName,
                    fullPath.toAbsolutePath().toString());
        } catch (IOException e) {
            LoggerUtil.error("Could not generate invoice", e);
            throw new DataAccessException("No se pudo generar la factura de texto.", e);
        }
    }

    private static String buildContent(String invoiceNumber, String studentName,
                                       String guardianName, String guardianDocument,
                                       String conceptCode, String conceptName,
                                       int quantity, BigDecimal unitPrice,
                                       BigDecimal subtotal, BigDecimal vat,
                                       BigDecimal total, String cufe,
                                       LocalDate issueDate) {
        String qrPayload = "NIT=" + SCHOOL_NIT + "|FACTURA=" + invoiceNumber
                + "|FECHA=" + issueDate + "|TOTAL=" + total + "|CUFE=" + cufe;
        StringBuilder text = new StringBuilder();
        text.append("==========================================================\n");
        text.append("              FACTURA ELECTRÓNICA - LITTLE HORIZONS\n");
        text.append("==========================================================\n");
        text.append("Colegio: ").append(SCHOOL_NAME).append('\n');
        text.append("NIT: ").append(SCHOOL_NIT).append('\n');
        text.append("Dirección: ").append(SCHOOL_ADDRESS).append('\n');
        text.append("Teléfono: ").append(SCHOOL_PHONE).append('\n');
        text.append("Correo: ").append(SCHOOL_EMAIL).append('\n');
        text.append("Resolución de habilitación: 001-2026 (simulada)\n");
        text.append("----------------------------------------------------------\n");
        text.append("Número de factura: ").append(invoiceNumber).append('\n');
        text.append("Fecha de emisión: ").append(issueDate).append('\n');
        text.append("----------------------------------------------------------\n");
        text.append("INFORMACIÓN DEL CLIENTE\n");
        text.append("Acudiente responsable: ").append(guardianName).append('\n');
        text.append("Documento: ").append(guardianDocument).append('\n');
        text.append("Estudiante: ").append(studentName).append('\n');
        text.append("----------------------------------------------------------\n");
        text.append("DETALLE\n");
        text.append(String.format("%-30s %8s %15s %15s%n",
                "Descripción", "Cant.", "Valor unit.", "Subtotal"));
        text.append(String.format("%-30s %8d %15s %15s%n",
                conceptName, quantity, formatCurrency(unitPrice), formatCurrency(subtotal)));
        text.append("----------------------------------------------------------\n");
        text.append(String.format("%-55s %15s%n", "Subtotal:", formatCurrency(subtotal)));
        text.append(String.format("%-55s %15s%n", "IVA (19%):", formatCurrency(vat)));
        text.append(String.format("%-55s %15s%n", "TOTAL:", formatCurrency(total)));
        text.append("----------------------------------------------------------\n");
        text.append("CUMPLIMIENTO DIAN (SIMULADO)\n");
        text.append("CUFE: ").append(cufe).append('\n');
        text.append("QR: ").append(qrPayload).append('\n');
        text.append("==========================================================\n");
        text.append("Gracias por su pago.\n");
        return text.toString();
    }

    public static String buildCufe(String invoiceNumber, LocalDate issueDate) {
        String source = invoiceNumber + SCHOOL_NIT + issueDate;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static String formatCurrency(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        String formatted = String.format(Locale.US, "%,.2f", value)
                .replace(",", "#")
                .replace(".", ",")
                .replace("#", ".");
        return "$" + formatted;
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    /** Invoice files contain customer data, so only the owner may read them. */
    private static void restrictPermissions(Path file) {
        try {
            Files.setPosixFilePermissions(file,
                    PosixFilePermissions.fromString("rw-------"));
        } catch (IOException | UnsupportedOperationException ignored) {
            // Non-POSIX file systems do not support this restriction.
        }
    }
}
