package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.ConnectionProvider;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for teacher assignments and weekly schedules.
 */
public class JdbcAssignmentDao implements AssignmentDao {

    private final ConnectionProvider connectionProvider;

    public JdbcAssignmentDao() {
        this(Database.provider());
    }

    public JdbcAssignmentDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String ASSIGNMENT_SELECT =
            "SELECT ap.id_asignacion, ap.id_profesor, ap.id_materia, ap.año_academico, "
            + "ap.seccion, ap.fecha_creacion, "
            + "CONCAT_WS(' ', NULLIF(TRIM(p.primer_nombre), ''), NULLIF(TRIM(p.segundo_nombre), ''), NULLIF(TRIM(p.primer_apellido), ''), "
            + "NULLIF(TRIM(p.segundo_apellido), '')) AS teacher_name, m.nombre_materia "
            + "FROM asignaciones_profesor ap "
            + "INNER JOIN profesores p ON p.id_profesor = ap.id_profesor "
            + "INNER JOIN materias m ON m.id_materia = ap.id_materia ";

    public int insert(TeacherAssignment assignment) {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, "
                + "año_academico, seccion) VALUES (?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            bindAssignment(ps, assignment);
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            assignment.setIdAssignment(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert assignment", e);
            throw new DataAccessException("No se pudo registrar la asignación.", e);
        }
    }

    public void update(TeacherAssignment assignment) {
        String sql = "UPDATE asignaciones_profesor SET id_profesor = ?, id_materia = ?, "
                + "año_academico = ?, seccion = ? WHERE id_asignacion = ?";
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int oldTeacher;
                int oldGrade;
                String currentSql = "SELECT ap.id_profesor, m.id_grado "
                        + "FROM asignaciones_profesor ap "
                        + "INNER JOIN materias m ON m.id_materia = ap.id_materia "
                        + "WHERE ap.id_asignacion = ? FOR UPDATE";
                try (PreparedStatement ps = connection.prepareStatement(currentSql)) {
                    ps.setInt(1, assignment.getIdAssignment());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("La asignación no existe.");
                        }
                        oldTeacher = rs.getInt("id_profesor");
                        oldGrade = rs.getInt("id_grado");
                    }
                }
                int newGrade;
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT id_grado FROM materias WHERE id_materia = ?")) {
                    ps.setInt(1, assignment.getIdSubject());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("La materia no existe.");
                        }
                        newGrade = rs.getInt("id_grado");
                    }
                }
                lockIds(connection, "profesores", "id_profesor",
                        oldTeacher, assignment.getIdTeacher());
                lockIds(connection, "grados", "id_grado", oldGrade, newGrade);
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    bindAssignment(ps, assignment);
                    ps.setInt(5, assignment.getIdAssignment());
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("La asignación no existe.");
                    }
                }
                if (hasAnyScheduleConflict(connection, assignment.getIdAssignment())) {
                    throw new IllegalArgumentException(
                            "La asignación crearía un conflicto de horario.");
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not update assignment", e);
            throw new DataAccessException("No se pudo actualizar la asignación.", e);
        }
    }

    public boolean delete(int idAssignment) {
        String sql = "DELETE FROM asignaciones_profesor WHERE id_asignacion = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAssignment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete assignment", e);
            throw new DataAccessException("No se pudo eliminar la asignación.", e);
        }
    }

    public TeacherAssignment findById(int idAssignment) {
        String sql = ASSIGNMENT_SELECT + "WHERE ap.id_asignacion = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAssignment);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapAssignment(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find assignment", e);
            throw new DataAccessException("No se pudo consultar la asignación.", e);
        }
    }

    public List<TeacherAssignment> findAll() {
        return queryList(ASSIGNMENT_SELECT
                + "ORDER BY ap.año_academico DESC, teacher_name, m.nombre_materia");
    }

    public List<TeacherAssignment> findByTeacher(int idTeacher) {
        return queryList(ASSIGNMENT_SELECT + "WHERE ap.id_profesor = ? "
                + "ORDER BY ap.año_academico DESC, m.nombre_materia", idTeacher);
    }

    public List<TeacherAssignment> findByYear(int academicYear) {
        return queryList(ASSIGNMENT_SELECT + "WHERE ap.año_academico = ? "
                + "ORDER BY teacher_name, m.nombre_materia", academicYear);
    }

    public boolean hasAssignment(int idTeacher, int idSubject, int academicYear,
                                 String section) {
        String sql = "SELECT EXISTS (SELECT 1 FROM asignaciones_profesor "
                + "WHERE id_profesor = ? AND id_materia = ? AND año_academico = ? "
                + "AND seccion = ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idTeacher);
            ps.setInt(2, idSubject);
            ps.setInt(3, academicYear);
            ps.setString(4, section);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not validate teacher assignment", e);
            throw new DataAccessException(
                    "No se pudo validar la asignación del profesor.", e);
        }
    }

    public int insertSchedule(Schedule schedule) {
        String sql = "INSERT INTO horarios (id_asignacion, dia_semana, hora_inicio, "
                + "hora_fin) VALUES (?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockScheduleScope(connection, schedule.getIdAssignment());
                if (hasScheduleConflict(connection, schedule)) {
                    throw new IllegalArgumentException(
                            "El horario se cruza con otra clase del profesor o de la sección.");
                }
                int id;
                try (PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, schedule.getIdAssignment());
                    ps.setString(2, schedule.getDayOfWeek());
                    ps.setString(3, schedule.getStartTime());
                    ps.setString(4, schedule.getEndTime());
                    ps.executeUpdate();
                    id = DaoSupport.generatedKey(ps);
                }
                connection.commit();
                schedule.setIdSchedule(id);
                return id;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert schedule", e);
            throw new DataAccessException("No se pudo registrar el horario.", e);
        }
    }

    public void updateSchedule(Schedule schedule) {
        String sql = "UPDATE horarios SET dia_semana = ?, hora_inicio = ?, "
                + "hora_fin = ? WHERE id_horario = ? AND id_asignacion = ?";
        String assignmentSql = "SELECT id_asignacion FROM horarios WHERE id_horario = ?";
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(assignmentSql)) {
                    ps.setInt(1, schedule.getIdSchedule());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("El horario no existe.");
                        }
                        schedule.setIdAssignment(rs.getInt("id_asignacion"));
                    }
                }
                lockScheduleScope(connection, schedule.getIdAssignment());
                if (hasScheduleConflict(connection, schedule)) {
                    throw new IllegalArgumentException(
                            "El horario se cruza con otra clase del profesor o de la sección.");
                }
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, schedule.getDayOfWeek());
                    ps.setString(2, schedule.getStartTime());
                    ps.setString(3, schedule.getEndTime());
                    ps.setInt(4, schedule.getIdSchedule());
                    ps.setInt(5, schedule.getIdAssignment());
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("El horario ya no existe.");
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not update schedule", e);
            throw new DataAccessException("No se pudo actualizar el horario.", e);
        }
    }

    public boolean hasScheduleConflict(Schedule schedule) {
        try (Connection connection = connectionProvider.getConnection()) {
            return hasScheduleConflict(connection, schedule);
        } catch (SQLException e) {
            LoggerUtil.error("Could not check schedule conflicts", e);
            throw new DataAccessException("No se pudo validar el horario.", e);
        }
    }

    private boolean hasScheduleConflict(Connection connection, Schedule schedule)
            throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM asignaciones_profesor actual "
                + "INNER JOIN materias materia_actual "
                + "ON materia_actual.id_materia = actual.id_materia "
                + "INNER JOIN asignaciones_profesor existente "
                + "ON existente.año_academico = actual.año_academico "
                + "INNER JOIN materias materia_existente "
                + "ON materia_existente.id_materia = existente.id_materia "
                + "INNER JOIN horarios h ON h.id_asignacion = existente.id_asignacion "
                + "WHERE actual.id_asignacion = ? AND h.dia_semana = ? "
                + "AND h.hora_inicio < ? AND h.hora_fin > ? "
                + "AND h.id_horario <> ? "
                + "AND (existente.id_profesor = actual.id_profesor OR "
                + "(existente.seccion = actual.seccion "
                + "AND materia_existente.id_grado = materia_actual.id_grado)))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, schedule.getIdAssignment());
            ps.setString(2, schedule.getDayOfWeek());
            ps.setString(3, schedule.getEndTime());
            ps.setString(4, schedule.getStartTime());
            ps.setInt(5, schedule.getIdSchedule());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private void lockScheduleScope(Connection connection, int idAssignment)
            throws SQLException {
        int idTeacher;
        int idGrade;
        String assignmentSql = "SELECT ap.id_profesor, m.id_grado "
                + "FROM asignaciones_profesor ap "
                + "INNER JOIN materias m ON m.id_materia = ap.id_materia "
                + "WHERE ap.id_asignacion = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(assignmentSql)) {
            ps.setInt(1, idAssignment);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("La asignación no existe.");
                }
                idTeacher = rs.getInt("id_profesor");
                idGrade = rs.getInt("id_grado");
            }
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id_profesor FROM profesores WHERE id_profesor = ? FOR UPDATE")) {
            ps.setInt(1, idTeacher);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("El profesor no existe.");
                }
            }
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id_grado FROM grados WHERE id_grado = ? FOR UPDATE")) {
            ps.setInt(1, idGrade);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("El grado no existe.");
                }
            }
        }
    }

    private void lockIds(Connection connection, String table, String idColumn,
                         int firstId, int secondId) throws SQLException {
        int lower = Math.min(firstId, secondId);
        int upper = Math.max(firstId, secondId);
        String sql = "SELECT " + idColumn + " FROM " + table
                + " WHERE " + idColumn + " = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, lower);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("No existe el registro relacionado.");
                }
            }
            if (upper != lower) {
                ps.setInt(1, upper);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("No existe el registro relacionado.");
                    }
                }
            }
        }
    }

    private boolean hasAnyScheduleConflict(Connection connection, int idAssignment)
            throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM horarios h1 "
                + "INNER JOIN asignaciones_profesor a1 ON a1.id_asignacion = h1.id_asignacion "
                + "INNER JOIN materias m1 ON m1.id_materia = a1.id_materia "
                + "INNER JOIN horarios h2 ON h2.id_horario > h1.id_horario "
                + "AND h2.dia_semana = h1.dia_semana "
                + "AND h1.hora_inicio < h2.hora_fin AND h1.hora_fin > h2.hora_inicio "
                + "INNER JOIN asignaciones_profesor a2 ON a2.id_asignacion = h2.id_asignacion "
                + "INNER JOIN materias m2 ON m2.id_materia = a2.id_materia "
                + "WHERE (a1.id_asignacion = ? OR a2.id_asignacion = ?) "
                + "AND a1.año_academico = a2.año_academico "
                + "AND (a1.id_profesor = a2.id_profesor OR "
                + "(a1.seccion = a2.seccion AND m1.id_grado = m2.id_grado)))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAssignment);
            ps.setInt(2, idAssignment);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    public boolean deleteSchedule(int idSchedule) {
        String sql = "DELETE FROM horarios WHERE id_horario = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSchedule);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete schedule", e);
            throw new DataAccessException("No se pudo eliminar el horario.", e);
        }
    }

    public List<Schedule> findSchedulesByAssignment(int idAssignment) {
        String sql = "SELECT id_horario, id_asignacion, dia_semana, hora_inicio, hora_fin "
                + "FROM horarios WHERE id_asignacion = ? "
                + "ORDER BY FIELD(dia_semana, 'Lunes','Martes','Miércoles','Jueves',"
                + "'Viernes','Sábado'), hora_inicio";
        List<Schedule> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAssignment);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSchedule(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list schedules", e);
            throw new DataAccessException("No se pudieron listar los horarios.", e);
        }
    }

    private List<TeacherAssignment> queryList(String sql) {
        List<TeacherAssignment> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAssignment(rs));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list assignments", e);
            throw new DataAccessException("No se pudieron listar las asignaciones.", e);
        }
    }

    private List<TeacherAssignment> queryList(String sql, int parameter) {
        List<TeacherAssignment> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAssignment(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list assignments", e);
            throw new DataAccessException("No se pudieron listar las asignaciones.", e);
        }
    }

    private void bindAssignment(PreparedStatement ps, TeacherAssignment a)
            throws SQLException {
        ps.setInt(1, a.getIdTeacher());
        ps.setInt(2, a.getIdSubject());
        ps.setInt(3, a.getAcademicYear());
        ps.setString(4, a.getSection());
    }

    private TeacherAssignment mapAssignment(ResultSet rs) throws SQLException {
        TeacherAssignment a = new TeacherAssignment();
        a.setIdAssignment(rs.getInt("id_asignacion"));
        a.setIdTeacher(rs.getInt("id_profesor"));
        a.setIdSubject(rs.getInt("id_materia"));
        a.setAcademicYear(rs.getInt("año_academico"));
        a.setSection(rs.getString("seccion"));
        a.setTeacherName(rs.getString("teacher_name"));
        a.setSubjectName(rs.getString("nombre_materia"));
        a.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        return a;
    }

    private Schedule mapSchedule(ResultSet rs) throws SQLException {
        Schedule s = new Schedule();
        s.setIdSchedule(rs.getInt("id_horario"));
        s.setIdAssignment(rs.getInt("id_asignacion"));
        s.setDayOfWeek(rs.getString("dia_semana"));
        s.setStartTime(rs.getString("hora_inicio"));
        s.setEndTime(rs.getString("hora_fin"));
        return s;
    }
}
