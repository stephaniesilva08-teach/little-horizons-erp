package com.littlehorizons.erp.model;

/** Roles available to authenticated ERP console users. */
public enum AccessRole {
    ADMINISTRADOR,
    SECRETARIA,
    DOCENTE,
    FINANZAS,
    BIBLIOTECA;

    public boolean canAccessOption(int option) {
        return switch (this) {
            case ADMINISTRADOR -> option >= 1 && option <= 11;
            case SECRETARIA -> option >= 1 && option <= 7;
            case DOCENTE -> option == 6;
            case FINANZAS -> option == 8;
            case BIBLIOTECA -> option == 9;
        };
    }
}
