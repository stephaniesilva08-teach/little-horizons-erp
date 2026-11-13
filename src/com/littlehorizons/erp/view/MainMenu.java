package com.littlehorizons.erp.view;

import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.AuditLogger;
import com.littlehorizons.erp.dao.JdbcSystemUserDao;
import com.littlehorizons.erp.model.SessionUser;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.PasswordHasher;
import com.littlehorizons.erp.controller.SystemUserController;

/**
 * Main application menu. It wires every module of the ERP.
 */
public class MainMenu {

    private final SessionUser currentUser;
    private final SystemUserView systemUserView;

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

    public MainMenu(SessionUser currentUser) {
        this.currentUser = currentUser;
        this.systemUserView = new SystemUserView(
                new SystemUserController(new JdbcSystemUserDao(Database.provider()),
                        new PasswordHasher(), currentUser), currentUser);
    }

    public void show() {
        boolean exit = false;
        while (!exit) {
            ConsoleUtils.printHeader("COLEGIO LITTLE HORIZONS - SISTEMA DE GESTIÓN");
            if (allowed(1)) System.out.println("  1. Estudiantes");
            if (allowed(2)) System.out.println("  2. Acudientes");
            if (allowed(3)) System.out.println("  3. Profesores");
            if (allowed(4)) System.out.println("  4. Materias");
            if (allowed(5)) System.out.println("  5. Matrículas");
            if (allowed(6)) System.out.println("  6. Módulo académico");
            if (allowed(7)) System.out.println("  7. Actividades extracurriculares");
            if (allowed(8)) System.out.println("  8. Módulo financiero y facturación");
            if (allowed(9)) System.out.println("  9. Recursos y préstamos");
            if (allowed(10)) System.out.println(" 10. Reportes");
            if (allowed(11)) System.out.println(" 11. Administrar usuarios");
            System.out.println("  0. Salir");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 11);
            if (option != 0 && !allowed(option)) {
                AuditLogger.recordAccessDenied(option, currentUser.role().name());
                ConsoleUtils.printError("Su rol no tiene acceso a esa opción.");
                continue;
            }
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
                case 11 -> systemUserView.show();
                case 0 -> exit = true;
                default -> { }
            }
        }
        System.out.println("\nGracias por usar el sistema de Little Horizons.");
    }

    private boolean allowed(int option) {
        return currentUser.role().canAccessOption(option);
    }
}
