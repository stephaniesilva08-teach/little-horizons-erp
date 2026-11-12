package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.MatriculationController;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Matriculation;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.util.List;

/**
 * Console view for annual matriculations.
 */
public class MatriculationView {

    private final MatriculationController controller = new MatriculationController();
    private final AcademicController academicController = new AcademicController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("GESTIÓN DE MATRÍCULAS");
            System.out.println("  1. Listar todas las matrículas");
            System.out.println("  2. Listar matrículas por estudiante");
            System.out.println("  3. Listar matrículas por año");
            System.out.println("  4. Registrar matrícula");
            System.out.println("  5. Actualizar matrícula");
            System.out.println("  6. Eliminar matrícula");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 6);
            switch (option) {
                case 1 -> listAll();
                case 2 -> listByStudent();
                case 3 -> listByYear();
                case 4 -> register();
                case 5 -> update();
                case 6 -> delete();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listAll() {
        printList(controller.listAll());
        ConsoleUtils.pause();
    }

    private void listByStudent() {
        int id = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> printList(controller.listByStudent(id)), null);
        ConsoleUtils.pause();
    }

    private void listByYear() {
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> printList(controller.listByYear(year)), null);
        ConsoleUtils.pause();
    }

    private void printList(List<Matriculation> list) {
        ConsoleUtils.printSubHeader("MATRÍCULAS");
        if (list.isEmpty()) {
            ConsoleUtils.printInfo("No hay matrículas para mostrar.");
            return;
        }
        System.out.printf("%-4s %-28s %-14s %-6s %-12s %-10s%n",
                "ID", "ESTUDIANTE", "GRADO", "AÑO", "FECHA", "ESTADO");
        ViewUtils.printSeparator();
        list.forEach(m -> System.out.printf("%-4d %-28s %-14s %-6d %-12s %-10s%n",
                m.getIdMatriculation(), truncate(m.getStudentName(), 28),
                m.getGradeName(), m.getAcademicYear(), m.getEnrollmentDate(),
                m.getState()));
    }

    private void register() {
        ConsoleUtils.printSubHeader("REGISTRAR MATRÍCULA");
        Matriculation m = readMatriculation(null);
        ViewUtils.executeWithId(() -> controller.register(m),
                "Matrícula registrada con");
        ConsoleUtils.pause();
    }

    private void update() {
        ConsoleUtils.printSubHeader("ACTUALIZAR MATRÍCULA");
        int id = ConsoleUtils.readInt("ID de la matrícula: ");
        Matriculation m = controller.findById(id);
        if (m == null) {
            ConsoleUtils.printError("La matrícula no existe.");
            ConsoleUtils.pause();
            return;
        }
        readMatriculation(m);
        ViewUtils.execute(() -> controller.update(m),
                "Matrícula actualizada correctamente.");
        ConsoleUtils.pause();
    }

    private void delete() {
        ConsoleUtils.printSubHeader("ELIMINAR MATRÍCULA");
        int id = ConsoleUtils.readInt("ID de la matrícula: ");
        if (controller.findById(id) == null) {
            ConsoleUtils.printError("La matrícula no existe.");
        } else if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.remove(id),
                    "Matrícula eliminada correctamente.");
        }
        ConsoleUtils.pause();
    }

    private Matriculation readMatriculation(Matriculation current) {
        Matriculation m = current == null ? new Matriculation() : current;
        m.setIdStudent(ConsoleUtils.readInt("ID del estudiante: "));
        listGrades();
        m.setIdGrade(ConsoleUtils.readInt("ID del grado: "));
        m.setAcademicYear(ConsoleUtils.readInt("Año académico: ", 2000, 2100));
        if (current == null) {
            m.setEnrollmentDate(ConsoleUtils.readDate("Fecha de matrícula (YYYY-MM-DD): "));
        } else {
            m.setEnrollmentDate(ViewUtils.readDateWithDefault(
                    "Fecha de matrícula (YYYY-MM-DD)", m.getEnrollmentDate()));
        }
        m.setState(ConsoleUtils.readEnum("Estado", "Activa", "Completada", "Retirada"));
        if ("Retirada".equals(m.getState())) {
            m.setWithdrawalDate(ConsoleUtils.readDate("Fecha de retiro (YYYY-MM-DD): "));
        }
        m.setObservations(ViewUtils.readWithDefault("Observaciones", m.getObservations()));
        return m;
    }

    private void listGrades() {
        System.out.println("  Grados disponibles:");
        for (Grade g : academicController.listGrades()) {
            System.out.println("    " + g.getIdGrade() + " - " + g.getName());
        }
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
