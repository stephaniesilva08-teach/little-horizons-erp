package com.littlehorizons.erp.view;

import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.TextInvoiceGenerator;

import java.math.BigDecimal;
import java.util.List;

/**
 * Shared helpers for the console views.
 */
public final class ViewUtils {

    private ViewUtils() {
    }

    public static void executeWithId(java.util.function.IntSupplier action,
                                     String successPrefix) {
        try {
            int id = action.getAsInt();
            ConsoleUtils.printSuccess(successPrefix + " ID " + id + ".");
        } catch (IllegalArgumentException | DataAccessException e) {
            ConsoleUtils.printError(e.getMessage());
            LoggerUtil.warn("Validation/business error: " + e.getMessage());
        }
    }

    public static void execute(Runnable action, String successMessage) {
        try {
            action.run();
            if (successMessage != null) {
                ConsoleUtils.printSuccess(successMessage);
            }
        } catch (IllegalArgumentException | DataAccessException e) {
            ConsoleUtils.printError(e.getMessage());
            LoggerUtil.warn("Validation/business error: " + e.getMessage());
        }
    }

    public static String readWithDefault(String prompt, String current) {
        String label = current == null || current.isBlank()
                ? prompt : prompt + " [" + current + "]";
        String value = ConsoleUtils.readLine(label + ": ");
        return value.isBlank() ? current : value;
    }

    public static String readDateWithDefault(String prompt, String current) {
        while (true) {
            String value = readWithDefault(prompt, current);
            if (value == null || value.isBlank()) {
                ConsoleUtils.printError("La fecha es obligatoria.");
                continue;
            }
            try {
                return java.time.LocalDate.parse(value).toString();
            } catch (java.time.format.DateTimeParseException e) {
                ConsoleUtils.printError("Fecha inválida. Use el formato YYYY-MM-DD.");
            }
        }
    }

    public static BigDecimal readAmountWithDefault(String prompt, BigDecimal current) {
        while (true) {
            String label = current == null ? prompt
                    : prompt + " [" + current + "]";
            String value = ConsoleUtils.readLine(label + ": ");
            if (value.isBlank() && current != null) {
                return current;
            }
            try {
                return new BigDecimal(value.replace(",", "."));
            } catch (NumberFormatException e) {
                ConsoleUtils.printError("Ingrese un valor numérico válido.");
            }
        }
    }

    public static void printSeparator() {
        System.out.println("----------------------------------------------------------");
    }

    public static String money(BigDecimal value) {
        return TextInvoiceGenerator.formatCurrency(value);
    }

    public static void printLines(List<String> lines) {
        if (lines.isEmpty()) {
            ConsoleUtils.printInfo("No hay información para mostrar.");
            return;
        }
        lines.forEach(System.out::println);
    }
}
