package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.util.List;

/**
 * Console view for teacher management.
 */
public class TeacherView {

    private final TeacherController controller = new TeacherController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("GESTIÓN DE PROFESORES");
            System.out.println("  1. Listar profesores");
            System.out.println("  2. Registrar profesor");
            System.out.println("  3. Actualizar profesor");
            System.out.println("  4. Eliminar profesor");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 4);
            switch (option) {
                case 1 -> listAll();
                case 2 -> register();
                case 3 -> update();
                case 4 -> delete();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listAll() {
        ConsoleUtils.printSubHeader("LISTADO DE PROFESORES");
        List<Teacher> teachers = ViewUtils.query("list_teachers", controller::listAll);
        if (teachers.isEmpty()) {
            ConsoleUtils.printInfo("No hay profesores registrados.");
        } else {
            System.out.printf("%-4s %-12s %-28s %-24s %-10s%n",
                    "ID", "DOCUMENTO", "NOMBRE", "ESPECIALIZACIÓN", "ESTADO");
            ViewUtils.printSeparator();
            teachers.forEach(t -> System.out.printf("%-4d %-12s %-28s %-24s %-10s%n",
                    t.getIdTeacher(), t.getDocumentNumber(), truncate(t.getFullName(), 28),
                    truncate(t.getSpecialization(), 24), t.getState()));
        }
        ConsoleUtils.pause();
    }

    private void register() {
        ConsoleUtils.printSubHeader("REGISTRAR PROFESOR");
        Teacher teacher = readTeacher(null);
        ViewUtils.executeWithId(() -> controller.register(teacher),
                "Profesor registrado con");
        ConsoleUtils.pause();
    }

    private void update() {
        ConsoleUtils.printSubHeader("ACTUALIZAR PROFESOR");
        int id = ConsoleUtils.readInt("ID del profesor: ");
        Teacher teacher = ViewUtils.query("find_teacher", id, () -> controller.findById(id));
        if (teacher == null) {
            ConsoleUtils.printError("El profesor no existe.");
            ConsoleUtils.pause();
            return;
        }
        readTeacher(teacher);
        ViewUtils.execute(() -> controller.update(teacher),
                "Profesor actualizado correctamente.", id);
        ConsoleUtils.pause();
    }

    private void delete() {
        ConsoleUtils.printSubHeader("ELIMINAR PROFESOR");
        int id = ConsoleUtils.readInt("ID del profesor: ");
        if (ViewUtils.query("find_teacher", id, () -> controller.findById(id)) == null) {
            ConsoleUtils.printError("El profesor no existe.");
        } else if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.remove(id),
                    "Profesor eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private Teacher readTeacher(Teacher current) {
        Teacher t = current == null ? new Teacher() : current;
        t.setDocumentType(ConsoleUtils.readEnum("Tipo de documento", "CC", "CE", "Pasaporte"));
        t.setDocumentNumber(ViewUtils.readWithDefault("Número de documento",
                t.getDocumentNumber()));
        t.setFirstName(ViewUtils.readWithDefault("Primer nombre", t.getFirstName()));
        t.setSecondName(ViewUtils.readWithDefault("Segundo nombre (opcional)",
                t.getSecondName()));
        t.setFirstLastName(ViewUtils.readWithDefault("Primer apellido",
                t.getFirstLastName()));
        t.setSecondLastName(ViewUtils.readWithDefault("Segundo apellido (opcional)",
                t.getSecondLastName()));
        t.setEmail(ViewUtils.readWithDefault("Correo electrónico", t.getEmail()));
        t.setPhone(ViewUtils.readWithDefault("Teléfono", t.getPhone()));
        t.setSpecialization(ViewUtils.readWithDefault("Especialización",
                t.getSpecialization()));
        t.setHireDate(ViewUtils.readDateWithDefault("Fecha de contratación (YYYY-MM-DD)",
                t.getHireDate()));
        t.setState(ConsoleUtils.readEnum("Estado", "Activo", "Inactivo", "Licencia"));
        return t;
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
