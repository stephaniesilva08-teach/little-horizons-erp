package com.littlehorizons.erp.test;

/**
 * Minimal assertion helpers so the test suite runs with no external
 * dependencies.
 */
public final class Assert {

    private Assert() {
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void isFalse(boolean condition, String message) {
        isTrue(!condition, message);
    }

    public static void equals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (expected: " + expected
                    + ", actual: " + actual + ")");
        }
    }

    public static void notNull(Object value, String message) {
        isTrue(value != null, message);
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType,
                                                       ThrowingRunnable action,
                                                       String message) {
        try {
            action.run();
        } catch (Throwable error) {
            if (!expectedType.isInstance(error)) {
                throw new AssertionError(message + " (unexpected: " + error + ")");
            }
            return expectedType.cast(error);
        }
        throw new AssertionError(message + " (no exception was thrown)");
    }

    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
