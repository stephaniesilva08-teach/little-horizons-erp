package com.littlehorizons.erp.view;

import com.littlehorizons.erp.util.AuditLogger;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.TextInvoiceGenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shared helpers for the console views.
 */
public final class ViewUtils {

    private ViewUtils() {
    }

    public static void executeWithId(java.util.function.IntSupplier action,
                                     String successPrefix) {
        AuditLogger.beginAction(successPrefix);
        int id;
        try {
            id = action.getAsInt();
        } catch (IllegalArgumentException | DataAccessException e) {
            AuditLogger.completeAction(successPrefix, null, false,
                    e.getClass().getSimpleName());
            ConsoleUtils.printError(e.getMessage());
            LoggerUtil.warn("Business validation failed: " + e.getClass().getSimpleName());
            return;
        } catch (RuntimeException e) {
            AuditLogger.completeAction(successPrefix, null, false,
                    e.getClass().getSimpleName());
            throw e;
        }
        AuditLogger.completeAction(successPrefix, id, true, "");
        ConsoleUtils.printSuccess(successPrefix + " ID " + id + ".");
    }

    public static void execute(Runnable action, String successMessage) {
        execute(action, successMessage, null);
    }

    public static void execute(Runnable action, String successMessage, Integer recordId) {
        String auditAction = successMessage == null || successMessage.isBlank()
                ? AuditLogger.currentScreen() : successMessage;
        AuditLogger.beginAction(auditAction, recordId);
        try {
            action.run();
        } catch (IllegalArgumentException | DataAccessException e) {
            AuditLogger.completeAction(auditAction, recordId, false,
                    e.getClass().getSimpleName());
            ConsoleUtils.printError(e.getMessage());
            LoggerUtil.warn("Business validation failed: " + e.getClass().getSimpleName());
            return;
        } catch (RuntimeException e) {
            AuditLogger.completeAction(auditAction, recordId, false,
                    e.getClass().getSimpleName());
            throw e;
        }
        AuditLogger.completeAction(auditAction, recordId, true, "");
        if (successMessage != null) {
            ConsoleUtils.printSuccess(successMessage);
        }
    }

    public static <T> T query(String actionCode, Supplier<T> query) {
        return query(actionCode, null, query);
    }

    public static <T> T query(String actionCode, Integer recordId, Supplier<T> query) {
        AuditLogger.beginAction(actionCode, recordId);
        try {
            T result = query.get();
            AuditLogger.completeAction(actionCode, recordId, true, "");
            return result;
        } catch (RuntimeException e) {
            AuditLogger.completeAction(actionCode, recordId, false,
                    e.getClass().getSimpleName());
            throw e;
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
                AuditLogger.recordInputRejected();
                ConsoleUtils.printError("La fecha es obligatoria.");
                continue;
            }
            try {
                return java.time.LocalDate.parse(value).toString();
            } catch (java.time.format.DateTimeParseException e) {
                AuditLogger.recordInputRejected();
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
                AuditLogger.recordInputRejected();
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
