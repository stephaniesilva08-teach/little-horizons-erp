package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.SubjectController;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.util.List;

/**
 * Console view for subject management.
 */
public class SubjectView {

    private final SubjectController controller = new SubjectController();
    private final AcademicController academicController = new AcademicController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("GESTIÓN DE MATERIAS");
            System.out.println("  1. Listar materias");
            System.out.println("  2. Listar materias por grado");
            System.out.println("  3. Registrar materia");
            System.out.println("  4. Actualizar materia");
            System.out.println("  5. Eliminar materia");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> listAll();
                case 2 -> listByGrade();
                case 3 -> register();
                case 4 -> update();
                case 5 -> delete();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listAll() {
        ConsoleUtils.printSubHeader("LISTADO DE MATERIAS");
        List<Subject> subjects = ViewUtils.query("list_subjects", controller::listAll);
        if (subjects.isEmpty()) {
            ConsoleUtils.printInfo("No hay materias registradas.");
        } else {
            System.out.printf("%-4s %-10s %-28s %-6s %-6s %-8s%n",
                    "ID", "CÓDIGO", "MATERIA", "GRADO", "HORAS", "ACTIVA");
            ViewUtils.printSeparator();
            subjects.forEach(s -> System.out.printf("%-4d %-10s %-28s %-6d %-6d %-8s%n",
                    s.getIdSubject(), s.getSubjectCode(), truncate(s.getName(), 28),
                    s.getIdGrade(), s.getWeeklyHours(), s.isActive() ? "Sí" : "No"));
        }
        ConsoleUtils.pause();
    }

    private void listByGrade() {
        ConsoleUtils.printSubHeader("MATERIAS POR GRADO");
        int idGrade = ConsoleUtils.readInt("ID del grado: ");
        ViewUtils.execute(() -> {
            List<Subject> subjects = controller.listByGrade(idGrade);
            if (subjects.isEmpty()) {
                ConsoleUtils.printInfo("No hay materias para el grado indicado.");
            } else {
                subjects.forEach(s -> System.out.println("  [" + s.getIdSubject() + "] "
                        + s.getSubjectCode() + " - " + s.getName()
                        + " (" + s.getWeeklyHours() + " h/semana)"));
            }
        }, null);
        ConsoleUtils.pause();
    }

    private void register() {
        ConsoleUtils.printSubHeader("REGISTRAR MATERIA");
        Subject subject = readSubject(null);
        ViewUtils.executeWithId(() -> controller.register(subject),
                "Materia registrada con");
        ConsoleUtils.pause();
    }

    private void update() {
        ConsoleUtils.printSubHeader("ACTUALIZAR MATERIA");
        int id = ConsoleUtils.readInt("ID de la materia: ");
        Subject subject = ViewUtils.query("find_subject", id, () -> controller.findById(id));
        if (subject == null) {
            ConsoleUtils.printError("La materia no existe.");
            ConsoleUtils.pause();
            return;
        }
        readSubject(subject);
        ViewUtils.execute(() -> controller.update(subject),
                "Materia actualizada correctamente.", id);
        ConsoleUtils.pause();
    }

    private void delete() {
        ConsoleUtils.printSubHeader("ELIMINAR MATERIA");
        int id = ConsoleUtils.readInt("ID de la materia: ");
        if (ViewUtils.query("find_subject", id, () -> controller.findById(id)) == null) {
            ConsoleUtils.printError("La materia no existe.");
        } else if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.remove(id),
                    "Materia eliminada correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private Subject readSubject(Subject current) {
        Subject s = current == null ? new Subject() : current;
        s.setSubjectCode(ViewUtils.readWithDefault("Código de la materia",
                s.getSubjectCode()));
        s.setName(ViewUtils.readWithDefault("Nombre de la materia", s.getName()));
        s.setDescription(ViewUtils.readWithDefault("Descripción", s.getDescription()));
        listGrades();
        s.setIdGrade(ConsoleUtils.readInt("ID del grado: "));
        s.setWeeklyHours(ConsoleUtils.readInt("Horas semanales: ", 1, 40));
        s.setActive(ConsoleUtils.readYesNo("¿Materia activa?"));
        return s;
    }

    private void listGrades() {
        List<Grade> grades = academicController.listGrades();
        System.out.println("  Grados disponibles:");
        grades.forEach(g -> System.out.println("    " + g.getIdGrade() + " - " + g.getName()));
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
