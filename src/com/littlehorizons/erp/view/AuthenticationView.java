package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AuthenticationController;
import com.littlehorizons.erp.model.SessionUser;
import com.littlehorizons.erp.util.AuditLogger;
import com.littlehorizons.erp.util.AuthenticationException;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.PasswordHasher;

import java.util.Arrays;

/** Console login and initial administrator setup. */
public final class AuthenticationView {

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private final AuthenticationController controller;

    public AuthenticationView(AuthenticationController controller) {
        this.controller = controller;
    }

    public SessionUser authenticate() {
        if (!controller.hasAccounts()) {
            return createFirstAdministrator();
        }
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            ConsoleUtils.printHeader("INICIO DE SESIÓN");
            String username = ConsoleUtils.readNonEmpty("Usuario: ");
            char[] password = ConsoleUtils.readPassword("Contraseña: ");
            try {
                SessionUser user = controller.authenticate(username, password);
                AuditLogger.recordAuthenticationAttempt(username, true);
                AuditLogger.startSession(user.username());
                ConsoleUtils.printSuccess("Bienvenido, " + user.displayName()
                        + " (" + user.role() + ").");
                return user;
            } catch (AuthenticationException e) {
                AuditLogger.recordAuthenticationAttempt(username, false);
                ConsoleUtils.printError(e.getMessage());
            }
        }
        ConsoleUtils.printError("Se agotaron los intentos de inicio de sesión.");
        return null;
    }

    private SessionUser createFirstAdministrator() {
        ConsoleUtils.printHeader("CONFIGURACIÓN INICIAL DEL ADMINISTRADOR");
        ConsoleUtils.printInfo("No hay cuentas todavía. Cree el primer administrador; "
                + "esta opción solo aparece una vez.");
        while (true) {
            String displayName = ConsoleUtils.readNonEmpty("Nombre del administrador: ");
            String username = ConsoleUtils.readNonEmpty("Usuario: ");
            char[] password = ConsoleUtils.readPassword("Contraseña nueva: ");
            char[] confirmation = ConsoleUtils.readPassword("Confirme la contraseña: ");
            boolean matches = Arrays.equals(password, confirmation);
            PasswordHasher.clear(confirmation);
            if (!matches) {
                PasswordHasher.clear(password);
                ConsoleUtils.printError("Las contraseñas no coinciden.");
                continue;
            }
            AuditLogger.startSession(username);
            AuditLogger.beginAction("Crear administrador inicial");
            try {
                SessionUser user = controller.createFirstAdministrator(
                        username, displayName, password);
                AuditLogger.completeAction("Crear administrador inicial",
                        user.id(), true, "");
                AuditLogger.recordAuthenticationAttempt(username, true);
                ConsoleUtils.printSuccess("Administrador creado. Sesión iniciada.");
                return user;
            } catch (IllegalArgumentException e) {
                AuditLogger.completeAction("Crear administrador inicial",
                        null, false, e.getClass().getSimpleName());
                ConsoleUtils.printError(e.getMessage());
                AuditLogger.endSession();
            }
        }
    }
}
