package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validation helpers for user input and business values.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9][0-9\\s-]{6,18}$");
    private static final Pattern DOCUMENT_PATTERN =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9\\s-]{3,19}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ'\\-\\s]+$");

    private Validator() {
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        return isNotEmpty(name) && NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidDocumentNumber(String document) {
        return isNotEmpty(document) && DOCUMENT_PATTERN.matcher(document.trim()).matches();
    }

    public static boolean isValidDate(String date) {
        if (!isNotEmpty(date)) {
            return false;
        }
        try {
            LocalDate.parse(date.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidGrade(BigDecimal grade) {
        return grade != null
                && grade.compareTo(BigDecimal.ZERO) >= 0
                && grade.compareTo(new BigDecimal("5.0")) <= 0
                && grade.stripTrailingZeros().scale() <= 1;
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean isOneOf(String value, String... allowed) {
        if (value == null) {
            return false;
        }
        for (String item : allowed) {
            if (item.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidPeriod(String period) {
        return isOneOf(period, "I", "II", "III", "IV", "Final", "Anual");
    }
}
