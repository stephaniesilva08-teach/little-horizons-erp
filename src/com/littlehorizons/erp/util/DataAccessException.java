package com.littlehorizons.erp.util;

/**
 * Unchecked exception for database access errors.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
