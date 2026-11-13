package com.littlehorizons.erp.util;

import java.sql.SQLException;

/**
 * Unchecked exception for database access errors.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String operationMessage;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
        this.operationMessage = message;
    }

    @Override
    public String getMessage() {
        SQLException sql = findSqlException(getCause());
        if (sql == null) {
            return operationMessage;
        }
        return switch (sql.getErrorCode()) {
            case 1062 -> "Ya existe un registro con el mismo código o identificador.";
            case 1451 -> "No se puede eliminar el registro porque tiene datos asociados.";
            case 1452 -> "No se encontró uno de los registros relacionados seleccionados.";
            default -> operationMessage;
        };
    }

    private static SQLException findSqlException(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof SQLException sql) {
                return sql;
            }
            current = current.getCause();
        }
        return null;
    }
}
