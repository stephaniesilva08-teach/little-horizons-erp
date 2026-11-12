package com.littlehorizons.erp.view;

import com.littlehorizons.erp.util.ConsoleUtils;

/**
 * Main application menu. It wires every module of the ERP.
 */
public class MainMenu {

    private final StudentView studentView = new StudentView();
    private final ParentView parentView = new ParentView();
    private final TeacherView teacherView = new TeacherView();
    private final SubjectView subjectView = new SubjectView();
    private final MatriculationView matriculationView = new MatriculationView();
    private final AcademicView academicView = new AcademicView();
    private final ExtracurricularView extracurricularView = new ExtracurricularView();
    private final FinanceView financeView = new FinanceView();
    private final ResourceView resourceView = new ResourceView();
    private final ReportView reportView = new ReportView();

    public void show() {
        boolean exit = false;
        while (!exit) {
            ConsoleUtils.printHeader("COLEGIO LITTLE HORIZONS - SISTEMA DE GESTIÓN");
            System.out.println("  1. Estudiantes");
            System.out.println("  2. Acudientes");
            System.out.println("  3. Profesores");
            System.out.println("  4. Materias");
            System.out.println("  5. Matrículas");
            System.out.println("  6. Módulo académico");
            System.out.println("  7. Actividades extracurriculares");
            System.out.println("  8. Módulo financiero y facturación");
            System.out.println("  9. Recursos y préstamos");
            System.out.println(" 10. Reportes");
            System.out.println("  0. Salir");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 10);
            switch (option) {
                case 1 -> studentView.show();
                case 2 -> parentView.show();
                case 3 -> teacherView.show();
                case 4 -> subjectView.show();
                case 5 -> matriculationView.show();
                case 6 -> academicView.show();
                case 7 -> extracurricularView.show();
                case 8 -> financeView.show();
                case 9 -> resourceView.show();
                case 10 -> reportView.show();
                case 0 -> exit = true;
                default -> { }
            }
        }
        System.out.println("\nGracias por usar el sistema de Little Horizons.");
    }
}
