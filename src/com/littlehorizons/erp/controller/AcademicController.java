package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.AssignmentDao;
import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.GradeDao;
import com.littlehorizons.erp.dao.JdbcGradeDao;
import com.littlehorizons.erp.dao.JdbcEnrollmentDao;
import com.littlehorizons.erp.dao.JdbcAssignmentDao;
import com.littlehorizons.erp.dao.JdbcAttendanceDao;
import com.littlehorizons.erp.dao.JdbcAcademicRecordDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceAlert;
import com.littlehorizons.erp.model.AttendanceSummary;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.SubjectAverage;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.Validator;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Business rules for grades, attendance, assignments, schedules and
 * subject enrollments.
 */
public class AcademicController {

    private final AcademicRecordDao recordDao;
    private final AttendanceDao attendanceDao;
    private final AssignmentDao assignmentDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;

    public AcademicController() {
        this(new JdbcAcademicRecordDao(), new JdbcAttendanceDao(), new JdbcAssignmentDao(), new JdbcEnrollmentDao(), new JdbcGradeDao());
    }

    public AcademicController(AcademicRecordDao recordDao, AttendanceDao attendanceDao, AssignmentDao assignmentDao, EnrollmentDao enrollmentDao, GradeDao gradeDao) {
        this.recordDao = recordDao;
        this.attendanceDao = attendanceDao;
        this.assignmentDao = assignmentDao;
        this.enrollmentDao = enrollmentDao;
        this.gradeDao = gradeDao;
    }

    public List<Grade> listGrades() {
        return gradeDao.findAll();
    }

    public int registerGrade(AcademicRecord record) {
        validateRecord(record);
        return recordDao.insert(record);
    }

    public void updateGrade(AcademicRecord record) {
        if (record.getIdRecord() <= 0) {
            throw new IllegalArgumentException("Falta el identificador de la nota.");
        }
        if (!Validator.isValidGrade(record.getGrade())) {
            throw new IllegalArgumentException(
                    "La nota debe estar entre 0.0 y 5.0 con un solo decimal.");
        }
        if (!Validator.isValidDate(record.getRecordDate())) {
            throw new IllegalArgumentException("La fecha de registro es inválida.");
        }
        recordDao.update(record);
    }

    public void removeGrade(int idRecord) {
        if (!recordDao.delete(idRecord)) {
            throw new IllegalArgumentException("No se encontró la nota a eliminar.");
        }
    }

    public AcademicRecord findGrade(int idRecord) {
        return recordDao.findById(idRecord);
    }

    public List<AcademicRecord> listGradesByStudent(int idStudent, int year) {
        return recordDao.findByStudent(idStudent, year);
    }

    public List<AcademicRecord> academicHistory(int idStudent) {
        if (idStudent <= 0) {
            throw new IllegalArgumentException("El estudiante es obligatorio.");
        }
        return recordDao.findHistoryByStudent(idStudent);
    }

    public List<SubjectAverage> subjectAverages(int idStudent, int year) {
        if (idStudent <= 0) {
            throw new IllegalArgumentException("El estudiante es obligatorio.");
        }
        if (!Validator.isInRange(year, 2000, 2100)) {
            throw new IllegalArgumentException("El año académico es inválido.");
        }
        return recordDao.findSubjectAverages(idStudent, year);
    }

    public BigDecimal periodAverage(int idStudent, int year, String period) {
        if (!Validator.isValidPeriod(period)) {
            throw new IllegalArgumentException("El período es inválido.");
        }
        return recordDao.averageByStudent(idStudent, year, period);
    }

    public BigDecimal overallAverage(int idStudent, int year) {
        return recordDao.overallAverageByStudent(idStudent, year);
    }

    public int registerAttendance(Attendance attendance) {
        validateAttendance(attendance);
        return attendanceDao.insert(attendance);
    }

    public void updateAttendance(Attendance attendance) {
        if (attendance.getIdAttendance() <= 0) {
            throw new IllegalArgumentException("Falta el identificador de la asistencia.");
        }
        if (!Validator.isOneOf(attendance.getState(), "Presente", "Ausente",
                "Tardanza", "Excusa Médica")) {
            throw new IllegalArgumentException("Estado de asistencia inválido.");
        }
        attendanceDao.update(attendance);
    }

    public void removeAttendance(int idAttendance) {
        if (!attendanceDao.delete(idAttendance)) {
            throw new IllegalArgumentException("No se encontró la asistencia a eliminar.");
        }
    }

    public Attendance findAttendance(int idAttendance) {
        return attendanceDao.findById(idAttendance);
    }

    public List<Attendance> listAttendanceByStudent(int idStudent) {
        return attendanceDao.findByStudent(idStudent);
    }

    public List<AttendanceSummary> attendanceSummary(int year) {
        return attendanceDao.summaryByStudent(year);
    }

    public List<AttendanceAlert> recurringAbsences(int minimum) {
        if (minimum < 1) {
            throw new IllegalArgumentException("El mínimo de ausencias debe ser positivo.");
        }
        return attendanceDao.findRecurringAbsences(minimum);
    }

    public int createAssignment(TeacherAssignment assignment) {
        validateAssignment(assignment);
        return assignmentDao.insert(assignment);
    }

    public void updateAssignment(TeacherAssignment assignment) {
        if (assignment.getIdAssignment() <= 0) {
            throw new IllegalArgumentException("Falta el identificador de la asignación.");
        }
        validateAssignment(assignment);
        assignmentDao.update(assignment);
    }

    public void removeAssignment(int idAssignment) {
        if (!assignmentDao.delete(idAssignment)) {
            throw new IllegalArgumentException("No se encontró la asignación a eliminar.");
        }
    }

    public List<TeacherAssignment> listAssignments() {
        return assignmentDao.findAll();
    }

    public List<TeacherAssignment> listAssignmentsByTeacher(int idTeacher) {
        return assignmentDao.findByTeacher(idTeacher);
    }

    public List<TeacherAssignment> listAssignmentsByYear(int year) {
        return assignmentDao.findByYear(year);
    }

    public int addSchedule(Schedule schedule) {
        validateSchedule(schedule);
        rejectScheduleConflict(schedule);
        return assignmentDao.insertSchedule(schedule);
    }

    public void updateSchedule(Schedule schedule) {
        if (schedule.getIdSchedule() <= 0) {
            throw new IllegalArgumentException("Falta el identificador del horario.");
        }
        validateSchedule(schedule);
        rejectScheduleConflict(schedule);
        assignmentDao.updateSchedule(schedule);
    }

    public void removeSchedule(int idSchedule) {
        if (!assignmentDao.deleteSchedule(idSchedule)) {
            throw new IllegalArgumentException("No se encontró el horario a eliminar.");
        }
    }

    public List<Schedule> listSchedules(int idAssignment) {
        return assignmentDao.findSchedulesByAssignment(idAssignment);
    }

    public int enrollSubject(SubjectEnrollment enrollment) {
        validateEnrollment(enrollment);
        return enrollmentDao.insert(enrollment);
    }

    public void updateEnrollment(SubjectEnrollment enrollment) {
        if (enrollment == null) {
            throw new IllegalArgumentException("Los datos de la inscripción son obligatorios.");
        }
        if (enrollment.getIdEnrollment() <= 0) {
            throw new IllegalArgumentException("Falta el identificador de la inscripción.");
        }
        if (!Validator.isNotEmpty(enrollment.getSection())) {
            throw new IllegalArgumentException("La sección es obligatoria.");
        }
        if (!Validator.isOneOf(enrollment.getState(), "Activa", "Retirada", "Completada")) {
            throw new IllegalArgumentException("El estado de la inscripción es inválido.");
        }
        enrollmentDao.update(enrollment);
    }

    public void removeEnrollment(int idEnrollment) {
        if (!enrollmentDao.delete(idEnrollment)) {
            throw new IllegalArgumentException("No se encontró la inscripción a eliminar.");
        }
    }

    public List<SubjectEnrollment> listEnrollments() {
        return enrollmentDao.findAll();
    }

    public List<SubjectEnrollment> listEnrollmentsByStudent(int idStudent) {
        return enrollmentDao.findByStudent(idStudent);
    }

    private void validateRecord(AcademicRecord r) {
        if (r.getIdStudent() <= 0 || r.getIdSubject() <= 0 || r.getIdTeacher() <= 0) {
            throw new IllegalArgumentException(
                    "Estudiante, materia y profesor son obligatorios.");
        }
        if (!Validator.isInRange(r.getAcademicYear(), 2000, 2100)) {
            throw new IllegalArgumentException("El año académico es inválido.");
        }
        if (!Validator.isValidPeriod(r.getAcademicPeriod())) {
            throw new IllegalArgumentException("El período es inválido.");
        }
        if (!Validator.isValidGrade(r.getGrade())) {
            throw new IllegalArgumentException(
                    "La nota debe estar entre 0.0 y 5.0 con un solo decimal.");
        }
        if (!Validator.isValidDate(r.getRecordDate())) {
            throw new IllegalArgumentException("La fecha de registro es inválida.");
        }
    }

    private void validateAttendance(Attendance a) {
        if (a.getIdStudent() <= 0 || a.getIdSubject() <= 0) {
            throw new IllegalArgumentException("Estudiante y materia son obligatorios.");
        }
        if (!Validator.isValidDate(a.getAttendanceDate())) {
            throw new IllegalArgumentException("La fecha de asistencia es inválida.");
        }
        if (!Validator.isOneOf(a.getState(), "Presente", "Ausente", "Tardanza",
                "Excusa Médica")) {
            throw new IllegalArgumentException("Estado de asistencia inválido.");
        }
    }

    private void validateAssignment(TeacherAssignment a) {
        if (a.getIdTeacher() <= 0 || a.getIdSubject() <= 0) {
            throw new IllegalArgumentException("Profesor y materia son obligatorios.");
        }
        if (!Validator.isInRange(a.getAcademicYear(), 2000, 2100)) {
            throw new IllegalArgumentException("El año académico es inválido.");
        }
        if (!Validator.isNotEmpty(a.getSection())) {
            throw new IllegalArgumentException("La sección es obligatoria.");
        }
    }

    private void validateSchedule(Schedule s) {
        if (s.getIdAssignment() <= 0) {
            throw new IllegalArgumentException("La asignación es obligatoria.");
        }
        if (!Validator.isOneOf(s.getDayOfWeek(), "Lunes", "Martes", "Miércoles",
                "Jueves", "Viernes", "Sábado")) {
            throw new IllegalArgumentException("Día de la semana inválido.");
        }
        try {
            LocalTime start = LocalTime.parse(normalizeTime(s.getStartTime()));
            LocalTime end = LocalTime.parse(normalizeTime(s.getEndTime()));
            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("La hora final debe ser mayor a la inicial.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de hora inválido (HH:MM).");
        }
    }

    private void rejectScheduleConflict(Schedule schedule) {
        if (assignmentDao.hasScheduleConflict(schedule)) {
            throw new IllegalArgumentException(
                    "El horario se cruza con otra clase del profesor o de la sección.");
        }
    }

    private String normalizeTime(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() == 5 ? trimmed + ":00" : trimmed;
    }

    private void validateEnrollment(SubjectEnrollment e) {
        if (e.getIdStudent() <= 0 || e.getIdSubject() <= 0) {
            throw new IllegalArgumentException("Estudiante y materia son obligatorios.");
        }
        if (!Validator.isInRange(e.getAcademicYear(), 2000, 2100)) {
            throw new IllegalArgumentException("El año académico es inválido.");
        }
        if (!Validator.isOneOf(e.getAcademicPeriod(), "I", "II", "III", "IV")) {
            throw new IllegalArgumentException("El período es inválido.");
        }
        if (!Validator.isValidDate(e.getEnrollmentDate())) {
            throw new IllegalArgumentException("La fecha de inscripción es inválida.");
        }
        if (!Validator.isOneOf(e.getState(), "Activa", "Retirada", "Completada")) {
            throw new IllegalArgumentException("El estado de la inscripción es inválido.");
        }
    }
}
