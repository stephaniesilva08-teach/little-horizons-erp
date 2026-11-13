package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ParentController;
import com.littlehorizons.erp.model.Parent;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.util.List;

/**
 * Console view for guardian management.
 */
public class ParentView {

    private final ParentController controller = new ParentController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("GESTIÓN DE ACUDIENTES");
            System.out.println("  1. Listar acudientes");
            System.out.println("  2. Listar acudientes de un estudiante");
            System.out.println("  3. Registrar acudiente");
            System.out.println("  4. Actualizar acudiente");
            System.out.println("  5. Eliminar acudiente");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> listAll();
                case 2 -> listByStudent();
                case 3 -> register();
                case 4 -> update();
                case 5 -> delete();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listAll() {
        ConsoleUtils.printSubHeader("LISTADO DE ACUDIENTES");
        List<Parent> parents = controller.listAll();
        if (parents.isEmpty()) {
            ConsoleUtils.printInfo("No hay acudientes registrados.");
        } else {
            System.out.printf("%-4s %-12s %-28s %-12s %-14s %-8s%n",
                    "ID", "DOCUMENTO", "NOMBRE", "PARENTESCO", "TELÉFONO", "FINAN.");
            ViewUtils.printSeparator();
            parents.forEach(p -> System.out.printf("%-4d %-12s %-28s %-12s %-14s %-8s%n",
                    p.getIdParent(), p.getDocumentNumber(), truncate(p.getFullName(), 28),
                    p.getRelationship(), p.getPhonePrimary(),
                    p.isIsFinancialResponsible() ? "Sí" : "No"));
        }
        ConsoleUtils.pause();
    }

    private void listByStudent() {
        ConsoleUtils.printSubHeader("ACUDIENTES DE UN ESTUDIANTE");
        int id = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> {
            List<Parent> parents = controller.listByStudent(id);
            if (parents.isEmpty()) {
                ConsoleUtils.printInfo("El estudiante no tiene acudientes asociados.");
            } else {
                parents.forEach(p -> System.out.println("  [" + p.getIdParent() + "] "
                        + p.getFullName() + " - " + p.getRelationship()
                        + " - " + p.getPhonePrimary()));
            }
        }, null);
        ConsoleUtils.pause();
    }

    private void register() {
        ConsoleUtils.printSubHeader("REGISTRAR ACUDIENTE");
        Parent parent = readParent(null);
        ViewUtils.executeWithId(() -> controller.register(parent),
                "Acudiente registrado con");
        ConsoleUtils.pause();
    }

    private void update() {
        ConsoleUtils.printSubHeader("ACTUALIZAR ACUDIENTE");
        int id = ConsoleUtils.readInt("ID del acudiente: ");
        Parent parent = controller.findById(id);
        if (parent == null) {
            ConsoleUtils.printError("El acudiente no existe.");
            ConsoleUtils.pause();
            return;
        }
        readParent(parent);
        ViewUtils.execute(() -> controller.update(parent),
                "Acudiente actualizado correctamente.", id);
        ConsoleUtils.pause();
    }

    private void delete() {
        ConsoleUtils.printSubHeader("ELIMINAR ACUDIENTE");
        int id = ConsoleUtils.readInt("ID del acudiente: ");
        if (controller.findById(id) == null) {
            ConsoleUtils.printError("El acudiente no existe.");
        } else if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.remove(id),
                    "Acudiente eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private Parent readParent(Parent current) {
        Parent p = current == null ? new Parent() : current;
        p.setDocumentType(ConsoleUtils.readEnum("Tipo de documento", "CC", "CE", "Pasaporte"));
        p.setDocumentNumber(ViewUtils.readWithDefault("Número de documento",
                p.getDocumentNumber()));
        p.setFirstName(ViewUtils.readWithDefault("Primer nombre", p.getFirstName()));
        p.setSecondName(ViewUtils.readWithDefault("Segundo nombre (opcional)",
                p.getSecondName()));
        p.setFirstLastName(ViewUtils.readWithDefault("Primer apellido",
                p.getFirstLastName()));
        p.setSecondLastName(ViewUtils.readWithDefault("Segundo apellido (opcional)",
                p.getSecondLastName()));
        p.setRelationship(ViewUtils.readWithDefault("Parentesco", p.getRelationship()));
        p.setPhonePrimary(ViewUtils.readWithDefault("Teléfono principal",
                p.getPhonePrimary()));
        p.setPhoneSecondary(ViewUtils.readWithDefault("Teléfono secundario (opcional)",
                p.getPhoneSecondary()));
        p.setEmail(ViewUtils.readWithDefault("Correo electrónico", p.getEmail()));
        p.setAddress(ViewUtils.readWithDefault("Dirección", p.getAddress()));
        p.setOccupation(ViewUtils.readWithDefault("Ocupación", p.getOccupation()));
        p.setIsFinancialResponsible(ConsoleUtils.readYesNo(
                "¿Es responsable financiero?"));
        return p;
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
