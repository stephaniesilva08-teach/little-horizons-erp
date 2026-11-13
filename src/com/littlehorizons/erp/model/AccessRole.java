package com.littlehorizons.erp.model;

/** Roles available to authenticated ERP console users. */
public enum AccessRole {
    ADMINISTRATOR("ADMINISTRADOR", "Administrador"),
    SECRETARY("SECRETARIA", "Secretaría"),
    TEACHER("DOCENTE", "Docente"),
    FINANCE("FINANZAS", "Finanzas"),
    LIBRARIAN("BIBLIOTECA", "Biblioteca");

    private final String databaseValue;
    private final String displayName;

    AccessRole(String databaseValue, String displayName) {
        this.databaseValue = databaseValue;
        this.displayName = displayName;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public String displayName() {
        return displayName;
    }

    public static AccessRole fromDatabaseValue(String value) {
        for (AccessRole role : values()) {
            if (role.databaseValue.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown database role.");
    }

    public static AccessRole fromDisplayName(String value) {
        for (AccessRole role : values()) {
            if (role.displayName.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role label.");
    }

    public boolean canAccessOption(int option) {
        return switch (this) {
            case ADMINISTRATOR -> option >= 1 && option <= 11;
            case SECRETARY -> option >= 1 && option <= 7;
            case TEACHER -> option == 6;
            case FINANCE -> option == 8;
            case LIBRARIAN -> option == 9;
        };
    }
}
