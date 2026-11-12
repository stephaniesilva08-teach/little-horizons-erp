package com.littlehorizons.erp.test;

import com.littlehorizons.erp.util.Validator;

import java.math.BigDecimal;

/**
 * Tests for the input validators used by the controllers.
 */
public final class ValidatorTest {

    private ValidatorTest() {
    }

    public static void register(TestSuite suite) {
        suite.add("Validator accepts a normal name", () -> {
            Assert.isTrue(Validator.isValidName("María José Pérez"), "name should be valid");
        });
        suite.add("Validator rejects digits in names", () -> {
            Assert.isFalse(Validator.isValidName("Ana 123"), "digits are not allowed");
        });
        suite.add("Validator accepts a valid email", () -> {
            Assert.isTrue(Validator.isValidEmail("acudiente@correo.com"), "email should be valid");
        });
        suite.add("Validator rejects an email without domain", () -> {
            Assert.isFalse(Validator.isValidEmail("acudiente@"), "email should be rejected");
        });
        suite.add("Validator accepts Colombian phone formats", () -> {
            Assert.isTrue(Validator.isValidPhone("+57 300 123 4567"), "phone should be valid");
        });
        suite.add("Validator accepts ISO dates only", () -> {
            Assert.isTrue(Validator.isValidDate("2026-02-01"), "ISO date should be valid");
            Assert.isFalse(Validator.isValidDate("01/02/2026"), "slash date should be rejected");
        });
        suite.add("Validator rejects grades with two decimals", () -> {
            Assert.isTrue(Validator.isValidGrade(new BigDecimal("4.5")), "4.5 should be valid");
            Assert.isFalse(Validator.isValidGrade(new BigDecimal("4.55")), "4.55 should be rejected");
            Assert.isFalse(Validator.isValidGrade(new BigDecimal("5.1")), "5.1 should be rejected");
        });
        suite.add("Validator rejects non-positive amounts", () -> {
            Assert.isFalse(Validator.isValidAmount(BigDecimal.ZERO), "zero is not a payment");
            Assert.isTrue(Validator.isValidAmount(new BigDecimal("0.01")), "0.01 should be valid");
        });
        suite.add("Validator is case-insensitive for options", () -> {
            Assert.isTrue(Validator.isOneOf("activo", "Activo", "Retirado"), "option should match");
            Assert.isFalse(Validator.isOneOf("Inactivo", "Activo", "Retirado"), "unknown option");
        });
    }
}
