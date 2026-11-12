package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ReportController;
import com.littlehorizons.erp.model.AttendanceSummary;
import com.littlehorizons.erp.model.CourseAverage;
import com.littlehorizons.erp.model.Debtor;
import com.littlehorizons.erp.model.GradeSummary;
import com.littlehorizons.erp.model.OverdueLoan;
import com.littlehorizons.erp.model.StudentGradeReport;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.util.List;

/**
 * Console view for management reports.
 */
public class ReportView {

    private final ReportController controller = new ReportController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("REPORTES");
            System.out.println("  1. Promedio por curso");
            System.out.println("  2. Boletín de un estudiante");
            System.out.println("  3. Resumen de notas por materia y período");
            System.out.println("  4. Resumen de asistencia");
            System.out.println("  5. Reporte de morosos");
            System.out.println("  6. Préstamos vencidos");
            System.out.println("  7. Resumen de matrícula");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 7);
            switch (option) {
                case 1 -> courseAverages();
                case 2 -> studentReport();
                case 3 -> gradeSummary();
                case 4 -> attendanceSummary();
                case 5 -> debtors();
                case 6 -> overdueLoans();
                case 7 -> enrollmentSummary();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void courseAverages() {
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("PROMEDIO POR CURSO");
            List<CourseAverage> list = controller.courseAverages(year);
            if (list.isEmpty()) {
                ConsoleUtils.printInfo("No hay datos para el año indicado.");
                return;
            }
            System.out.printf("%-6s %-28s %-14s %-10s %-12s%n",
                    "GRADO", "MATERIA", "SECCIÓN", "PROMEDIO", "ESTUDIANTES");
            ViewUtils.printSeparator();
            list.forEach(c -> System.out.printf("%-6d %-28s %-14s %-10s %-12d%n",
                    c.gradeId(), truncate(c.subjectName(), 28), c.section(),
                    c.average(), c.students()));
        }, null);
        ConsoleUtils.pause();
    }

    private void studentReport() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("BOLETÍN DEL ESTUDIANTE");
            List<StudentGradeReport> list = controller.studentGradeReport(idStudent, year);
            if (list.isEmpty()) {
                ConsoleUtils.printInfo("No hay notas registradas.");
                return;
            }
            System.out.println("  Estudiante: " + list.get(0).studentName());
            System.out.printf("%-30s %-8s %-8s%n", "MATERIA", "PERÍODO", "NOTA");
            ViewUtils.printSeparator();
            list.forEach(r -> System.out.printf("%-30s %-8s %-8s%n",
                    truncate(r.subjectName(), 30), r.period(), r.grade()));
        }, null);
        ConsoleUtils.pause();
    }

    private void gradeSummary() {
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("RESUMEN DE NOTAS");
            List<GradeSummary> list = controller.gradeSummary(year);
            if (list.isEmpty()) {
                ConsoleUtils.printInfo("No hay datos para el año indicado.");
                return;
            }
            System.out.printf("%-28s %-8s %-10s %-10s %-10s %-10s%n",
                    "MATERIA", "PERÍODO", "PROMEDIO", "MÁXIMA", "MÍNIMA", "REGISTROS");
            ViewUtils.printSeparator();
            list.forEach(g -> System.out.printf("%-28s %-8s %-10s %-10s %-10s %-10d%n",
                    truncate(g.subjectName(), 28), g.period(), g.average(),
                    g.maximum(), g.minimum(), g.records()));
        }, null);
        ConsoleUtils.pause();
    }

    private void attendanceSummary() {
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("RESUMEN DE ASISTENCIA");
            List<AttendanceSummary> list = controller.attendanceSummary(year);
            if (list.isEmpty()) {
                ConsoleUtils.printInfo("No hay datos para el año indicado.");
                return;
            }
            System.out.printf("%-6s %-30s %-16s %-8s%n",
                    "ID", "ESTUDIANTE", "ESTADO", "TOTAL");
            ViewUtils.printSeparator();
            list.forEach(a -> System.out.printf("%-6d %-30s %-16s %-8d%n",
                    a.studentId(), truncate(a.studentName(), 30), a.state(), a.total()));
        }, null);
        ConsoleUtils.pause();
    }

    private void debtors() {
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("REPORTE DE MOROSOS");
            List<Debtor> list = controller.debtors();
            if (list.isEmpty()) {
                ConsoleUtils.printInfo("No hay estudiantes morosos.");
                return;
            }
            System.out.printf("%-6s %-30s %-14s %-14s %-14s%n",
                    "ID", "ESTUDIANTE", "CARGADO", "PAGADO", "SALDO");
            ViewUtils.printSeparator();
            list.forEach(d -> System.out.printf("%-6d %-30s %-14s %-14s %-14s%n",
                    d.studentId(), truncate(d.studentName(), 30),
                    ViewUtils.money(d.charged()), ViewUtils.money(d.paid()),
                    ViewUtils.money(d.balance())));
        }, null);
        ConsoleUtils.pause();
    }

    private void overdueLoans() {
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("PRÉSTAMOS VENCIDOS");
            List<OverdueLoan> list = controller.overdueLoans();
            if (list.isEmpty()) {
                ConsoleUtils.printInfo("No hay préstamos vencidos.");
                return;
            }
            System.out.printf("%-6s %-28s %-26s %-12s %-12s%n",
                    "ID", "RECURSO", "BENEFICIARIO", "TIPO", "VENCIMIENTO");
            ViewUtils.printSeparator();
            list.forEach(l -> System.out.printf("%-6d %-28s %-26s %-12s %-12s%n",
                    l.loanId(), truncate(l.resourceName(), 28),
                    truncate(l.borrowerName(), 26), l.borrowerType(),
                    l.expectedReturnDate()));
        }, null);
        ConsoleUtils.pause();
    }

    private void enrollmentSummary() {
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("RESUMEN DE MATRÍCULA");
            ViewUtils.printLines(controller.enrollmentSummary(year));
        }, null);
        ConsoleUtils.pause();
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
