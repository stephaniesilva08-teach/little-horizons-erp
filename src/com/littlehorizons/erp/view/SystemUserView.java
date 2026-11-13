package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.SystemUserController;
import com.littlehorizons.erp.model.AccessRole;
import com.littlehorizons.erp.model.SessionUser;
import com.littlehorizons.erp.util.AuditLogger;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.PasswordHasher;

import java.util.Arrays;

/** Console account administration available only to administrators. */
public final class SystemUserView {

    private final SystemUserController controller;
    private final SessionUser currentUser;

    public SystemUserView(SystemUserController controller, SessionUser currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;
    }

    public void show() {
        if (currentUser.role() != AccessRole.ADMINISTRADOR) {
            AuditLogger.recordAccessDenied(11, currentUser.role().name());
            ConsoleUtils.printError("No tiene permiso para administrar cuentas.");
            return;
        }
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("ADMINISTRACIÓN DE CUENTAS");
            System.out.println("  1. Listar cuentas");
            System.out.println("  2. Crear cuenta");
            System.out.println("  3. Desactivar cuenta");
            System.out.println("  4. Restablecer contraseña");
            System.out.println("  5. Reactivar cuenta");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> listUsers();
                case 2 -> createUser();
                case 3 -> deactivateUser();
                case 4 -> resetPassword();
                case 5 -> activateUser();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listUsers() {
        ConsoleUtils.printSubHeader("CUENTAS DEL SISTEMA");
        System.out.printf("%-5s %-24s %-30s %-18s %-10s%n",
                "ID", "USUARIO", "NOMBRE", "ROL", "ESTADO");
        ViewUtils.printSeparator();
        controller.listUsers().forEach(user -> System.out.printf("%-5d %-24s %-30s %-18s %-10s%n",
                user.id(), user.username(), user.displayName(), user.role(),
                user.active() ? "Activa" : "Inactiva"));
        ConsoleUtils.pause();
    }

    private void createUser() {
        ConsoleUtils.printSubHeader("CREAR CUENTA");
        String displayName = ConsoleUtils.readNonEmpty("Nombre visible: ");
        String username = ConsoleUtils.readNonEmpty("Usuario: ");
        AccessRole role = AccessRole.valueOf(ConsoleUtils.readEnum("Rol",
                Arrays.stream(AccessRole.values()).map(Enum::name).toArray(String[]::new)));
        char[] password = ConsoleUtils.readPassword("Contraseña temporal: ");
        char[] confirmation = ConsoleUtils.readPassword("Confirme la contraseña: ");
        boolean matches = Arrays.equals(password, confirmation);
        PasswordHasher.clear(confirmation);
        if (!matches) {
            PasswordHasher.clear(password);
            ConsoleUtils.printError("Las contraseñas no coinciden.");
            return;
        }
        ViewUtils.executeWithId(() -> controller.createUser(
                username, displayName, role, password), "Cuenta creada con");
        ConsoleUtils.pause();
    }

    private void deactivateUser() {
        ConsoleUtils.printSubHeader("DESACTIVAR CUENTA");
        int id = ConsoleUtils.readInt("ID de la cuenta: ");
        if (!ConsoleUtils.readYesNo("¿Confirma la desactivación?")) {
            ConsoleUtils.printInfo("Operación cancelada.");
            return;
        }
        ViewUtils.execute(() -> controller.deactivate(id),
                "Cuenta desactivada correctamente.", id);
        ConsoleUtils.pause();
    }

    private void resetPassword() {
        ConsoleUtils.printSubHeader("RESTABLECER CONTRASEÑA");
        int id = ConsoleUtils.readInt("ID de la cuenta: ");
        char[] password = ConsoleUtils.readPassword("Contraseña nueva: ");
        char[] confirmation = ConsoleUtils.readPassword("Confirme la contraseña: ");
        boolean matches = Arrays.equals(password, confirmation);
        PasswordHasher.clear(confirmation);
        if (!matches) {
            PasswordHasher.clear(password);
            ConsoleUtils.printError("Las contraseñas no coinciden.");
            return;
        }
        ViewUtils.execute(() -> controller.resetPassword(id, password),
                "Contraseña restablecida correctamente.", id);
        ConsoleUtils.pause();
    }

    private void activateUser() {
        ConsoleUtils.printSubHeader("REACTIVAR CUENTA");
        int id = ConsoleUtils.readInt("ID de la cuenta: ");
        ViewUtils.execute(() -> controller.activate(id),
                "Cuenta reactivada correctamente.", id);
        ConsoleUtils.pause();
    }
}
