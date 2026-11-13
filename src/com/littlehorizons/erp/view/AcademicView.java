package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.SubjectController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceAlert;
import com.littlehorizons.erp.model.AttendanceSummary;
import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectAverage;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.ConsoleUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Console view for the academic module.
 */
public class AcademicView {

    private final AcademicController controller = new AcademicController();
    private final SubjectController subjectController = new SubjectController();
    private final TeacherController teacherController = new TeacherController();

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("MÓDULO ACADÉMICO");
            System.out.println("  1. Notas y promedios");
            System.out.println("  2. Asistencia");
            System.out.println("  3. Asignaciones profesor-materia");
            System.out.println("  4. Horarios");
            System.out.println("  5. Inscripciones a materias");
            System.out.println("  0. Volver al menú principal");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> gradesMenu();
                case 2 -> attendanceMenu();
                case 3 -> assignmentsMenu();
                case 4 -> schedulesMenu();
                case 5 -> enrollmentsMenu();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    // ---------------------------------------------------------------- grades

    private void gradesMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("NOTAS Y PROMEDIOS");
            System.out.println("  1. Listar notas de un estudiante");
            System.out.println("  2. Historial académico completo");
            System.out.println("  3. Promedio por materia");
            System.out.println("  4. Registrar nota");
            System.out.println("  5. Actualizar nota");
            System.out.println("  6. Eliminar nota");
            System.out.println("  7. Promedio por período");
            System.out.println("  8. Promedio general del estudiante");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 8);
            switch (option) {
                case 1 -> listGrades();
                case 2 -> academicHistory();
                case 3 -> subjectAverages();
                case 4 -> registerGrade();
                case 5 -> updateGrade();
                case 6 -> deleteGrade();
                case 7 -> periodAverage();
                case 8 -> overallAverage();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listGrades() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("NOTAS DEL ESTUDIANTE");
            List<AcademicRecord> records = controller.listGradesByStudent(idStudent, year);
            if (records.isEmpty()) {
                ConsoleUtils.printInfo("No hay notas registradas.");
                return;
            }
            System.out.printf("%-4s %-28s %-10s %-6s %-6s %-12s%n",
                    "ID", "MATERIA", "PERÍODO", "NOTA", "AÑO", "FECHA");
            ViewUtils.printSeparator();
            records.forEach(r -> System.out.printf("%-4d %-28s %-10s %-6s %-6d %-12s%n",
                    r.getIdRecord(), truncate(r.getSubjectName(), 28),
                    r.getAcademicPeriod(), r.getGrade(), r.getAcademicYear(),
                    r.getRecordDate()));
        }, null);
        ConsoleUtils.pause();
    }

    private void academicHistory() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("HISTORIAL ACADÉMICO COMPLETO");
            List<AcademicRecord> records = controller.academicHistory(idStudent);
            if (records.isEmpty()) {
                ConsoleUtils.printInfo("No hay notas registradas para el estudiante.");
                return;
            }
            System.out.printf("%-4s %-28s %-8s %-6s %-6s %-12s%n",
                    "ID", "MATERIA", "PERÍODO", "NOTA", "AÑO", "FECHA");
            ViewUtils.printSeparator();
            records.forEach(r -> System.out.printf("%-4d %-28s %-8s %-6s %-6d %-12s%n",
                    r.getIdRecord(), truncate(r.getSubjectName(), 28),
                    r.getAcademicPeriod(), r.getGrade(), r.getAcademicYear(),
                    r.getRecordDate()));
        }, null);
        ConsoleUtils.pause();
    }

    private void subjectAverages() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("PROMEDIO POR MATERIA");
            List<SubjectAverage> averages = controller.subjectAverages(idStudent, year);
            if (averages.isEmpty()) {
                ConsoleUtils.printInfo("No hay notas registradas para el año indicado.");
                return;
            }
            System.out.printf("%-4s %-30s %-6s %-10s %-8s%n",
                    "ID", "MATERIA", "AÑO", "PROMEDIO", "NOTAS");
            ViewUtils.printSeparator();
            averages.forEach(a -> System.out.printf("%-4d %-30s %-6d %-10s %-8d%n",
                    a.subjectId(), truncate(a.subjectName(), 30), a.academicYear(),
                    a.average().setScale(2, java.math.RoundingMode.HALF_UP),
                    a.records()));
        }, null);
        ConsoleUtils.pause();
    }

    private void registerGrade() {
        ConsoleUtils.printSubHeader("REGISTRAR NOTA");
        listSubjects();
        listTeachers();
        AcademicRecord r = new AcademicRecord();
        r.setIdStudent(ConsoleUtils.readInt("ID del estudiante: "));
        r.setIdSubject(ConsoleUtils.readInt("ID de la materia: "));
        r.setIdTeacher(ConsoleUtils.readInt("ID del profesor: "));
        r.setAcademicYear(ConsoleUtils.readInt("Año académico: ", 2000, 2100));
        r.setAcademicPeriod(ConsoleUtils.readEnum("Período",
                "I", "II", "III", "IV", "Final"));
        r.setGrade(ConsoleUtils.readDecimal("Nota (0.0 - 5.0): ", 0, 5));
        r.setObservations(ConsoleUtils.readOptional("Observaciones: "));
        r.setRecordDate(ConsoleUtils.readDate("Fecha de registro (YYYY-MM-DD): "));
        ViewUtils.executeWithId(() -> controller.registerGrade(r),
                "Nota registrada con");
        ConsoleUtils.pause();
    }

    private void updateGrade() {
        ConsoleUtils.printSubHeader("ACTUALIZAR NOTA");
        int id = ConsoleUtils.readInt("ID de la nota: ");
        AcademicRecord r = controller.findGrade(id);
        if (r == null) {
            ConsoleUtils.printError("La nota no existe.");
            ConsoleUtils.pause();
            return;
        }
        r.setGrade(ConsoleUtils.readDecimal("Nueva nota (0.0 - 5.0): ", 0, 5));
        r.setObservations(ViewUtils.readWithDefault("Observaciones", r.getObservations()));
        r.setRecordDate(ViewUtils.readDateWithDefault("Fecha de registro", r.getRecordDate()));
        ViewUtils.execute(() -> controller.updateGrade(r),
                "Nota actualizada correctamente.", r.getIdRecord());
        ConsoleUtils.pause();
    }

    private void deleteGrade() {
        int id = ConsoleUtils.readInt("ID de la nota: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeGrade(id),
                    "Nota eliminada correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private void periodAverage() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        String period = ConsoleUtils.readEnum("Período", "I", "II", "III", "IV", "Final");
        ViewUtils.execute(() -> {
            BigDecimal average = controller.periodAverage(idStudent, year, period);
            ConsoleUtils.printInfo("Promedio del período " + period + ": "
                    + (average == null ? "sin notas" : average.setScale(2,
                    java.math.RoundingMode.HALF_UP)));
        }, null);
        ConsoleUtils.pause();
    }

    private void overallAverage() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            BigDecimal average = controller.overallAverage(idStudent, year);
            ConsoleUtils.printInfo("Promedio general: " + (average == null ? "sin notas"
                    : average.setScale(2, java.math.RoundingMode.HALF_UP)));
        }, null);
        ConsoleUtils.pause();
    }

    // ------------------------------------------------------------ attendance

    private void attendanceMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("ASISTENCIA");
            System.out.println("  1. Listar asistencia de un estudiante");
            System.out.println("  2. Registrar asistencia");
            System.out.println("  3. Actualizar asistencia");
            System.out.println("  4. Eliminar asistencia");
            System.out.println("  5. Resumen por año");
            System.out.println("  6. Alertas de inasistencia semanal");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 6);
            switch (option) {
                case 1 -> listAttendance();
                case 2 -> registerAttendance();
                case 3 -> updateAttendance();
                case 4 -> deleteAttendance();
                case 5 -> attendanceSummary();
                case 6 -> absenceAlerts();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listAttendance() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("ASISTENCIA DEL ESTUDIANTE");
            List<Attendance> list = controller.listAttendanceByStudent(idStudent);
            if (list.isEmpty()) {
                ConsoleUtils.printInfo("No hay registros de asistencia.");
                return;
            }
            System.out.printf("%-4s %-12s %-28s %-14s%n",
                    "ID", "FECHA", "MATERIA", "ESTADO");
            ViewUtils.printSeparator();
            list.forEach(a -> System.out.printf("%-4d %-12s %-28s %-14s%n",
                    a.getIdAttendance(), a.getAttendanceDate(),
                    truncate(a.getSubjectName(), 28), a.getState()));
        }, null);
        ConsoleUtils.pause();
    }

    private void registerAttendance() {
        ConsoleUtils.printSubHeader("REGISTRAR ASISTENCIA");
        listSubjects();
        listTeachers();
        Attendance a = new Attendance();
        a.setIdStudent(ConsoleUtils.readInt("ID del estudiante: "));
        a.setIdSubject(ConsoleUtils.readInt("ID de la materia: "));
        a.setAttendanceDate(ConsoleUtils.readDate("Fecha (YYYY-MM-DD): "));
        a.setState(ConsoleUtils.readEnum("Estado", "Presente", "Ausente", "Tardanza",
                "Excusa Médica"));
        a.setObservations(ConsoleUtils.readOptional("Observaciones: "));
        a.setRegisteredBy(ConsoleUtils.readInt("ID del profesor que registra: "));
        ViewUtils.executeWithId(() -> controller.registerAttendance(a),
                "Asistencia registrada con");
        ConsoleUtils.pause();
    }

    private void updateAttendance() {
        int id = ConsoleUtils.readInt("ID de la asistencia: ");
        Attendance a = controller.findAttendance(id);
        if (a == null) {
            ConsoleUtils.printError("No se encontró el registro de asistencia.");
            ConsoleUtils.pause();
            return;
        }
        a.setState(ConsoleUtils.readEnum("Nuevo estado", "Presente", "Ausente",
                "Tardanza", "Excusa Médica"));
        a.setObservations(ViewUtils.readWithDefault("Observaciones", a.getObservations()));
        ViewUtils.execute(() -> controller.updateAttendance(a),
                "Asistencia actualizada correctamente.", a.getIdAttendance());
        ConsoleUtils.pause();
    }

    private void deleteAttendance() {
        int id = ConsoleUtils.readInt("ID de la asistencia: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeAttendance(id),
                    "Asistencia eliminada correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    private void attendanceSummary() {
        int year = ConsoleUtils.readInt("Año académico: ", 2000, 2100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("RESUMEN DE ASISTENCIA");
            List<AttendanceSummary> summary = controller.attendanceSummary(year);
            if (summary.isEmpty()) {
                ConsoleUtils.printInfo("No hay datos para el año indicado.");
                return;
            }
            System.out.printf("%-4s %-30s %-16s %-6s%n",
                    "ID", "ESTUDIANTE", "ESTADO", "TOTAL");
            ViewUtils.printSeparator();
            summary.forEach(s -> System.out.printf("%-4d %-30s %-16s %-6d%n",
                    s.studentId(), truncate(s.studentName(), 30), s.state(), s.total()));
        }, null);
        ConsoleUtils.pause();
    }

    private void absenceAlerts() {
        int minimum = ConsoleUtils.readInt("Mínimo de ausencias por semana: ", 1, 100);
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("ALERTAS DE INASISTENCIA SEMANAL");
            List<AttendanceAlert> alerts = controller.recurringAbsences(minimum);
            if (alerts.isEmpty()) {
                ConsoleUtils.printInfo("No hay estudiantes con inasistencia recurrente.");
                return;
            }
            System.out.printf("  %-4s %-30s %-25s %-8s%n",
                    "ID", "ESTUDIANTE", "SEMANA", "FALTAS");
            ViewUtils.printSeparator();
            alerts.forEach(a -> System.out.printf("  %-4d %-30s %-25s %-8d%n",
                    a.getStudentId(), truncate(a.getStudentName(), 30),
                    a.getWeekStart() + " al " + a.getWeekStart().plusDays(6),
                    a.getTotal()));
        }, null);
        ConsoleUtils.pause();
    }

    // ----------------------------------------------------------- assignments

    private void assignmentsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("ASIGNACIONES PROFESOR-MATERIA");
            System.out.println("  1. Listar todas las asignaciones");
            System.out.println("  2. Listar asignaciones de un profesor");
            System.out.println("  3. Registrar asignación");
            System.out.println("  4. Actualizar asignación");
            System.out.println("  5. Eliminar asignación");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> printAssignments(controller.listAssignments());
                case 2 -> assignmentsByTeacher();
                case 3 -> createAssignment();
                case 4 -> updateAssignment();
                case 5 -> deleteAssignment();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void assignmentsByTeacher() {
        int idTeacher = ConsoleUtils.readInt("ID del profesor: ");
        ViewUtils.execute(() -> printAssignments(
                controller.listAssignmentsByTeacher(idTeacher)), null);
        ConsoleUtils.pause();
    }

    private void printAssignments(List<TeacherAssignment> list) {
        ConsoleUtils.printSubHeader("ASIGNACIONES");
        if (list.isEmpty()) {
            ConsoleUtils.printInfo("No hay asignaciones para mostrar.");
            return;
        }
        System.out.printf("%-4s %-28s %-28s %-6s %-8s%n",
                "ID", "PROFESOR", "MATERIA", "AÑO", "SECCIÓN");
        ViewUtils.printSeparator();
        list.forEach(a -> System.out.printf("%-4d %-28s %-28s %-6d %-8s%n",
                a.getIdAssignment(), truncate(a.getTeacherName(), 28),
                truncate(a.getSubjectName(), 28), a.getAcademicYear(), a.getSection()));
    }

    private void createAssignment() {
        ConsoleUtils.printSubHeader("REGISTRAR ASIGNACIÓN");
        listTeachers();
        listSubjects();
        TeacherAssignment a = readAssignment(new TeacherAssignment());
        ViewUtils.executeWithId(() -> controller.createAssignment(a),
                "Asignación registrada con");
        ConsoleUtils.pause();
    }

    private void updateAssignment() {
        int id = ConsoleUtils.readInt("ID de la asignación: ");
        TeacherAssignment current = controller.listAssignments().stream()
                .filter(a -> a.getIdAssignment() == id).findFirst().orElse(null);
        if (current == null) {
            ConsoleUtils.printError("La asignación no existe.");
            ConsoleUtils.pause();
            return;
        }
        readAssignment(current);
        ViewUtils.execute(() -> controller.updateAssignment(current),
                "Asignación actualizada correctamente.", current.getIdAssignment());
        ConsoleUtils.pause();
    }

    private TeacherAssignment readAssignment(TeacherAssignment a) {
        listTeachers();
        listSubjects();
        a.setIdTeacher(ConsoleUtils.readInt("ID del profesor: "));
        a.setIdSubject(ConsoleUtils.readInt("ID de la materia: "));
        a.setAcademicYear(ConsoleUtils.readInt("Año académico: ", 2000, 2100));
        a.setSection(ConsoleUtils.readNonEmpty("Sección: "));
        return a;
    }

    private void deleteAssignment() {
        int id = ConsoleUtils.readInt("ID de la asignación: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeAssignment(id),
                    "Asignación eliminada correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    // -------------------------------------------------------------- schedules

    private void schedulesMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("HORARIOS");
            System.out.println("  1. Listar horarios de una asignación");
            System.out.println("  2. Registrar horario");
            System.out.println("  3. Actualizar horario");
            System.out.println("  4. Eliminar horario");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 4);
            switch (option) {
                case 1 -> listSchedules();
                case 2 -> addSchedule();
                case 3 -> updateSchedule();
                case 4 -> deleteSchedule();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void listSchedules() {
        int idAssignment = ConsoleUtils.readInt("ID de la asignación: ");
        ViewUtils.execute(() -> {
            ConsoleUtils.printSubHeader("HORARIOS DE LA ASIGNACIÓN " + idAssignment);
            List<Schedule> schedules = controller.listSchedules(idAssignment);
            if (schedules.isEmpty()) {
                ConsoleUtils.printInfo("No hay horarios registrados.");
                return;
            }
            schedules.forEach(s -> System.out.println("  [" + s.getIdSchedule() + "] "
                    + s.getDayOfWeek() + " " + s.getStartTime()
                    + " - " + s.getEndTime()));
        }, null);
        ConsoleUtils.pause();
    }

    private void addSchedule() {
        Schedule s = new Schedule();
        s.setIdAssignment(ConsoleUtils.readInt("ID de la asignación: "));
        readScheduleTimes(s);
        ViewUtils.executeWithId(() -> controller.addSchedule(s),
                "Horario registrado con");
        ConsoleUtils.pause();
    }

    private void updateSchedule() {
        Schedule s = new Schedule();
        s.setIdSchedule(ConsoleUtils.readInt("ID del horario: "));
        s.setIdAssignment(ConsoleUtils.readInt("ID de la asignación: "));
        readScheduleTimes(s);
        ViewUtils.execute(() -> controller.updateSchedule(s),
                "Horario actualizado correctamente.", s.getIdSchedule());
        ConsoleUtils.pause();
    }

    private void readScheduleTimes(Schedule s) {
        s.setDayOfWeek(ConsoleUtils.readEnum("Día", "Lunes", "Martes", "Miércoles",
                "Jueves", "Viernes", "Sábado"));
        s.setStartTime(ConsoleUtils.readNonEmpty("Hora inicio (HH:MM): "));
        s.setEndTime(ConsoleUtils.readNonEmpty("Hora fin (HH:MM): "));
    }

    private void deleteSchedule() {
        int id = ConsoleUtils.readInt("ID del horario: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeSchedule(id),
                    "Horario eliminado correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    // ------------------------------------------------------------ enrollments

    private void enrollmentsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("INSCRIPCIONES A MATERIAS");
            System.out.println("  1. Listar todas las inscripciones");
            System.out.println("  2. Listar inscripciones de un estudiante");
            System.out.println("  3. Registrar inscripción");
            System.out.println("  4. Actualizar inscripción");
            System.out.println("  5. Eliminar inscripción");
            System.out.println("  0. Volver");
            int option = ConsoleUtils.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> printEnrollments(controller.listEnrollments());
                case 2 -> enrollmentsByStudent();
                case 3 -> createEnrollment();
                case 4 -> updateEnrollment();
                case 5 -> deleteEnrollment();
                case 0 -> back = true;
                default -> { }
            }
        }
    }

    private void enrollmentsByStudent() {
        int idStudent = ConsoleUtils.readInt("ID del estudiante: ");
        ViewUtils.execute(() -> printEnrollments(
                controller.listEnrollmentsByStudent(idStudent)), null);
        ConsoleUtils.pause();
    }

    private void printEnrollments(List<SubjectEnrollment> list) {
        ConsoleUtils.printSubHeader("INSCRIPCIONES");
        if (list.isEmpty()) {
            ConsoleUtils.printInfo("No hay inscripciones para mostrar.");
            return;
        }
        System.out.printf("%-4s %-28s %-28s %-6s %-6s %-10s%n",
                "ID", "ESTUDIANTE", "MATERIA", "AÑO", "PER.", "ESTADO");
        ViewUtils.printSeparator();
        list.forEach(e -> System.out.printf("%-4d %-28s %-28s %-6d %-6s %-10s%n",
                e.getIdEnrollment(), truncate(e.getStudentName(), 28),
                truncate(e.getSubjectName(), 28), e.getAcademicYear(),
                e.getAcademicPeriod(), e.getState()));
    }

    private void createEnrollment() {
        ConsoleUtils.printSubHeader("REGISTRAR INSCRIPCIÓN");
        listSubjects();
        SubjectEnrollment e = new SubjectEnrollment();
        e.setIdStudent(ConsoleUtils.readInt("ID del estudiante: "));
        e.setIdSubject(ConsoleUtils.readInt("ID de la materia: "));
        e.setAcademicYear(ConsoleUtils.readInt("Año académico: ", 2000, 2100));
        e.setAcademicPeriod(ConsoleUtils.readEnum("Período", "I", "II", "III", "IV"));
        e.setSection(ConsoleUtils.readNonEmpty("Sección: "));
        e.setEnrollmentDate(ConsoleUtils.readDate("Fecha de inscripción (YYYY-MM-DD): "));
        e.setState("Activa");
        ViewUtils.executeWithId(() -> controller.enrollSubject(e),
                "Inscripción registrada con");
        ConsoleUtils.pause();
    }

    private void updateEnrollment() {
        SubjectEnrollment e = new SubjectEnrollment();
        e.setIdEnrollment(ConsoleUtils.readInt("ID de la inscripción: "));
        e.setSection(ConsoleUtils.readNonEmpty("Nueva sección: "));
        e.setState(ConsoleUtils.readEnum("Nuevo estado", "Activa", "Retirada",
                "Completada"));
        ViewUtils.execute(() -> controller.updateEnrollment(e),
                "Inscripción actualizada correctamente.", e.getIdEnrollment());
        ConsoleUtils.pause();
    }

    private void deleteEnrollment() {
        int id = ConsoleUtils.readInt("ID de la inscripción: ");
        if (ConsoleUtils.readYesNo("¿Confirma la eliminación?")) {
            ViewUtils.execute(() -> controller.removeEnrollment(id),
                    "Inscripción eliminada correctamente.", id);
        }
        ConsoleUtils.pause();
    }

    // ----------------------------------------------------------------- helpers

    private void listSubjects() {
        System.out.println("  Materias disponibles:");
        for (Subject s : subjectController.listActive()) {
            System.out.println("    " + s.getIdSubject() + " - " + s.getSubjectCode()
                    + " " + s.getName());
        }
    }

    private void listTeachers() {
        System.out.println("  Profesores disponibles:");
        for (Teacher t : teacherController.listActive()) {
            System.out.println("    " + t.getIdTeacher() + " - " + t.getFullName());
        }
    }

    private String truncate(String value, int length) {
        if (value == null) {
            return "";
        }
        return value.length() <= length ? value : value.substring(0, length - 1) + ".";
    }
}
