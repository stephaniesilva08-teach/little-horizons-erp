package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.JdbcAttendanceDao;
import com.littlehorizons.erp.dao.JdbcStudentDao;
import com.littlehorizons.erp.dao.JdbcSubjectDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceImportResult;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.Validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Imports attendance records from the CSV file requested by the academic
 * coordination.
 *
 * <p>Each non-empty data line is validated and stored independently. A bad
 * line is counted as failed without stopping the remaining lines.</p>
 */
public class AttendanceImportController {

    public static final String FILE_NAME = "asistencias_importar.csv";
    public static final String HEADER = "id_estudiante;id_materia;fecha;estado";

    private static final Set<String> VALID_STATES = Set.of(
            "Presente", "Ausente", "Tardanza", "Excusa Médica");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private final StudentDao studentDao;
    private final SubjectDao subjectDao;
    private final AttendanceDao attendanceDao;
    private final Path importFile;

    public AttendanceImportController() {
        this(new JdbcStudentDao(), new JdbcSubjectDao(), new JdbcAttendanceDao(),
                Path.of(FILE_NAME));
    }

    public AttendanceImportController(StudentDao studentDao, SubjectDao subjectDao,
                                      AttendanceDao attendanceDao, Path importFile) {
        this.studentDao = studentDao;
        this.subjectDao = subjectDao;
        this.attendanceDao = attendanceDao;
        this.importFile = importFile;
    }

    public AttendanceImportResult importAttendances() {
        if (!Files.isRegularFile(importFile)) {
            return AttendanceImportResult.fileNotFound();
        }
        try (BufferedReader reader = Files.newBufferedReader(importFile,
                StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!HEADER.equals(header)) {
                return AttendanceImportResult.invalidHeader();
            }

            int total = 0;
            int successful = 0;
            int failed = 0;
            Map<Integer, Boolean> studentCache = new HashMap<>();
            Map<Integer, Boolean> subjectCache = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                total++;
                if (importLine(line, studentCache, subjectCache)) {
                    successful++;
                } else {
                    failed++;
                }
            }
            LoggerUtil.info("Attendance import finished: total=" + total
                    + ", successful=" + successful + ", failed=" + failed);
            return AttendanceImportResult.completed(total, successful, failed);
        } catch (IOException e) {
            LoggerUtil.error("Could not read attendance import file", e);
            throw new DataAccessException(
                    "No se pudo leer el archivo de importación de asistencias.", e);
        }
    }

    private boolean importLine(String line, Map<Integer, Boolean> studentCache,
                               Map<Integer, Boolean> subjectCache) {
        String[] fields = line.split(";", -1);
        if (fields.length != 4) {
            return false;
        }

        int idStudent;
        int idSubject;
        try {
            idStudent = Integer.parseInt(fields[0]);
            idSubject = Integer.parseInt(fields[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        if (idStudent <= 0 || idSubject <= 0) {
            return false;
        }

        String date = fields[2];
        if (!DATE_PATTERN.matcher(date).matches() || !Validator.isValidDate(date)) {
            return false;
        }

        String state = fields[3];
        if (!VALID_STATES.contains(state)) {
            return false;
        }

        boolean studentExists = studentCache.computeIfAbsent(idStudent,
                studentDao::exists);
        if (!studentExists) {
            return false;
        }
        boolean subjectExists = subjectCache.computeIfAbsent(idSubject,
                id -> subjectDao.findById(id) != null);
        if (!subjectExists) {
            return false;
        }

        Attendance attendance = new Attendance();
        attendance.setIdStudent(idStudent);
        attendance.setIdSubject(idSubject);
        attendance.setAttendanceDate(date);
        attendance.setState(state);
        try {
            attendanceDao.insert(attendance);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
