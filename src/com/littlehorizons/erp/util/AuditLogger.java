package com.littlehorizons.erp.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Append-only, one-line audit trail for actions performed through the console.
 * Personal field values are deliberately excluded from audit entries.
 */
public final class AuditLogger {

    private static final Path AUDIT_FILE = Path.of("audit_log.txt");
    private static String operator = "";
    private static String sessionId = "";
    private static String screen = "application";

    private AuditLogger() {
    }

    public static synchronized void startSession(String operatorName) {
        operator = clean(operatorName, 80);
        if (operator.isBlank()) {
            throw new IllegalArgumentException("El nombre del operador es obligatorio.");
        }
        sessionId = UUID.randomUUID().toString();
        append("SESSION_START", "console", "SUCCESS", "", "");
    }

    public static synchronized void endSession() {
        if (!sessionId.isBlank()) {
            append("SESSION_END", "console", "SUCCESS", "", "");
            sessionId = "";
        }
    }

    public static synchronized void setScreen(String screenName) {
        screen = screenCode(screenName);
    }

    public static synchronized void recordScreenOpened(String screenName) {
        screen = screenCode(screenName);
        if (!sessionId.isBlank()) {
            append("SCREEN_OPENED", "open_" + screen, "OPENED", "", "");
        }
    }

    public static synchronized String currentScreen() {
        return screen;
    }

    public static synchronized void recordMenuSelection(int option) {
        append("MENU_SELECTION", "option=" + option, "SELECTED", "", "");
    }

    public static synchronized void recordAuthenticationAttempt(String username,
                                                                 boolean succeeded) {
        append("AUTHENTICATION_ATTEMPT", "username=" + clean(username, 40),
                succeeded ? "SUCCESS" : "FAILURE", "", "");
    }

    public static synchronized void recordAccessDenied(int option, String role) {
        append("ACCESS_DENIED", "option=" + option + " role=" + clean(role, 40),
                "DENIED", "", "");
    }

    public static synchronized void beginAction(String action) {
        beginAction(action, null);
    }

    public static synchronized void beginAction(String action, Integer recordId) {
        append("ACTION_STARTED", actionCode(action), "STARTED",
                recordId == null ? "" : recordId.toString(), "");
    }

    public static synchronized void completeAction(String action, Integer recordId,
                                                    boolean succeeded,
                                                    String errorType) {
        append("ACTION_COMPLETED", actionCode(action),
                succeeded ? "SUCCESS" : "FAILURE",
                recordId == null ? "" : recordId.toString(),
                succeeded ? "" : clean(errorType, 80));
    }

    private static void append(String event, String action, String outcome,
                               String recordId, String errorType) {
        String entry = "timestamp=" + Instant.now()
                + " | session=" + clean(sessionId, 40)
                + " | operator=" + clean(operator, 80)
                + " | screen=" + clean(screen, 100)
                + " | event=" + clean(event, 40)
                + " | action=" + clean(action, 120)
                + " | outcome=" + clean(outcome, 24)
                + " | recordId=" + clean(recordId, 20)
                + " | errorType=" + clean(errorType, 80)
                + System.lineSeparator();
        try {
            Files.writeString(AUDIT_FILE, entry, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND, StandardOpenOption.WRITE);
            restrictPermissions();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo escribir en audit_log.txt; no se puede continuar "
                    + "sin registrar la acción.", e);
        }
    }

    private static String clean(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String clean = value.replaceAll("[\\p{Cc}\\p{Cf}|=]", " ").trim();
        return clean.length() > maxLength
                ? clean.substring(0, maxLength) : clean;
    }

    private static String screenCode(String screenName) {
        String value = normalize(screenName);
        if (value.contains("inicio de sesion") || value.contains("configuracion inicial")) {
            return "authentication";
        }
        if (value.contains("reporte") || value.contains("boletin")
                || value.contains("promedio por curso") || value.contains("prestamos vencidos")) {
            return "reports";
        }
        if (value.contains("estudiante")) return "students";
        if (value.contains("acudiente")) return "guardians";
        if (value.contains("profesor")) return "teachers";
        if (value.contains("materia") || value.contains("inscripcion")) return "subjects";
        if (value.contains("matricula")) return "enrollments";
        if (value.contains("academ") || value.contains("nota") || value.contains("asistencia")
                || value.contains("horario") || value.contains("asignacion")) return "academic";
        if (value.contains("club") || value.contains("evento") || value.contains("membresia")
                || value.contains("participante") || value.contains("extracurricular")) {
            return "extracurricular";
        }
        if (value.contains("pago") || value.contains("factura") || value.contains("cargo")
                || value.contains("concepto") || value.contains("financiero")
                || value.contains("valor personalizado")) return "finance";
        if (value.contains("recurso") || value.contains("prestamo")
                || value.contains("categoria")) return "resources";
        if (value.contains("cuenta") || value.contains("administracion")) return "user_admin";
        return "console";
    }

    private static String actionCode(String action) {
        String value = normalize(action);
        if (value.contains("pago")) return "register_payment";
        if (value.contains("devolucion")) return "return_resource";
        if (value.contains("asociad")) return "associate_record";
        if (value.contains("restablecid")) return "reset_credential";
        if (value.contains("reactivad")) return "activate_account";
        if (value.contains("desactivad") || value.contains("eliminad")
                || value.contains("retirad")) return "deactivate_record";
        if (value.contains("actualizad") || value.contains("actualizar")) {
            return "update_record";
        }
        if (value.contains("cread") || value.contains("registrad")
                || value.contains("crea")) return "create_record";
        return "console_operation";
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private static void restrictPermissions() {
        try {
            Files.setPosixFilePermissions(AUDIT_FILE,
                    PosixFilePermissions.fromString("rw-------"));
        } catch (IOException | UnsupportedOperationException ignored) {
            // Non-POSIX file systems do not support POSIX permissions.
        }
    }
}
