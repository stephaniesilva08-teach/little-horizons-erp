package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceAlert;
import com.littlehorizons.erp.model.AttendanceSummary;
import com.littlehorizons.erp.util.ConnectionProvider;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for attendance.
 */
public class JdbcAttendanceDao implements AttendanceDao {

    private final ConnectionProvider connectionProvider;

    public JdbcAttendanceDao() {
        this(Database.provider());
    }

    public JdbcAttendanceDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String BASE_SELECT =
            "SELECT a.id_asistencia, a.id_estudiante, a.id_materia, a.fecha_asistencia, "
            + "a.estado_asistencia, a.observaciones, a.registrado_por, a.fecha_creacion, "
            + "CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), "
            + "NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, m.nombre_materia AS subject_name "
            + "FROM asistencias a "
            + "INNER JOIN estudiantes e ON e.id_estudiante = a.id_estudiante "
            + "INNER JOIN materias m ON m.id_materia = a.id_materia ";

    public int insert(Attendance attendance) {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, "
                + "fecha_asistencia, estado_asistencia, observaciones, registrado_por) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, attendance);
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            attendance.setIdAttendance(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert attendance", e);
            throw new DataAccessException("No se pudo registrar la asistencia.", e);
        }
    }

    public void update(Attendance attendance) {
        String sql = "UPDATE asistencias SET estado_asistencia = ?, observaciones = ? "
                + "WHERE id_asistencia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, attendance.getState());
            ps.setString(2, attendance.getObservations());
            ps.setInt(3, attendance.getIdAttendance());
            DaoSupport.requireUpdated(ps.executeUpdate(), "la asistencia");
        } catch (SQLException e) {
            LoggerUtil.error("Could not update attendance", e);
            throw new DataAccessException("No se pudo actualizar la asistencia.", e);
        }
    }

    public boolean delete(int idAttendance) {
        String sql = "DELETE FROM asistencias WHERE id_asistencia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAttendance);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete attendance", e);
            throw new DataAccessException("No se pudo eliminar la asistencia.", e);
        }
    }

    public Attendance findById(int idAttendance) {
        String sql = BASE_SELECT + "WHERE a.id_asistencia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAttendance);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find attendance", e);
            throw new DataAccessException("No se pudo consultar la asistencia.", e);
        }
    }

    public List<Attendance> findByStudent(int idStudent) {
        String sql = BASE_SELECT + "WHERE a.id_estudiante = ? "
                + "ORDER BY a.fecha_asistencia DESC";
        List<Attendance> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list attendance", e);
            throw new DataAccessException("No se pudieron listar las asistencias.", e);
        }
    }

    public List<AttendanceSummary> summaryByStudent(int academicYear) {
        String sql = "SELECT a.id_estudiante, CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), "
                + "NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, "
                + "a.estado_asistencia, COUNT(*) AS total FROM asistencias a "
                + "INNER JOIN estudiantes e ON e.id_estudiante = a.id_estudiante "
                + "WHERE a.fecha_asistencia >= ? AND a.fecha_asistencia < ? "
                + "GROUP BY a.id_estudiante, student_name, a.estado_asistencia "
                + "ORDER BY student_name, a.estado_asistencia";
        List<AttendanceSummary> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(LocalDate.of(academicYear, 1, 1)));
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.of(academicYear + 1, 1, 1)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceSummary(rs.getInt("id_estudiante"),
                            rs.getString("student_name"), rs.getString("estado_asistencia"),
                            rs.getInt("total")));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not build attendance summary", e);
            throw new DataAccessException("No se pudo generar el resumen de asistencia.", e);
        }
    }

    public List<AttendanceAlert> findRecurringAbsences(int minimumAbsences) {
        String sql = "SELECT a.id_estudiante, CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), "
                + "NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, "
                + "DATE_SUB(a.fecha_asistencia, INTERVAL WEEKDAY(a.fecha_asistencia) DAY) "
                + "AS week_start, COUNT(*) AS total FROM asistencias a "
                + "INNER JOIN estudiantes e ON e.id_estudiante = a.id_estudiante "
                + "WHERE a.estado_asistencia = 'Ausente' "
                + "AND a.fecha_asistencia >= DATE_SUB(CURRENT_DATE, INTERVAL 90 DAY) "
                + "GROUP BY a.id_estudiante, student_name, week_start "
                + "HAVING COUNT(*) >= ? ORDER BY week_start DESC, total DESC";
        List<AttendanceAlert> alerts = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, minimumAbsences);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alerts.add(new AttendanceAlert(rs.getInt("id_estudiante"),
                            rs.getString("student_name"),
                            LocalDate.parse(DaoSupport.getDate(rs, "week_start")),
                            rs.getInt("total")));
                }
            }
            return alerts;
        } catch (SQLException e) {
            LoggerUtil.error("Could not build attendance alerts", e);
            throw new DataAccessException("No se pudieron generar las alertas de inasistencia.", e);
        }
    }

    private void bind(PreparedStatement ps, Attendance a) throws SQLException {
        ps.setInt(1, a.getIdStudent());
        ps.setInt(2, a.getIdSubject());
        DaoSupport.setDate(ps, 3, a.getAttendanceDate());
        ps.setString(4, a.getState());
        ps.setString(5, a.getObservations());
        if (a.getRegisteredBy() > 0) {
            ps.setInt(6, a.getRegisteredBy());
        } else {
            ps.setNull(6, java.sql.Types.INTEGER);
        }
    }

    private Attendance map(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setIdAttendance(rs.getInt("id_asistencia"));
        a.setIdStudent(rs.getInt("id_estudiante"));
        a.setIdSubject(rs.getInt("id_materia"));
        a.setAttendanceDate(DaoSupport.getDate(rs, "fecha_asistencia"));
        a.setState(rs.getString("estado_asistencia"));
        a.setObservations(rs.getString("observaciones"));
        a.setRegisteredBy(DaoSupport.getInt(rs, "registrado_por"));
        a.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        a.setStudentName(rs.getString("student_name"));
        a.setSubjectName(rs.getString("subject_name"));
        return a;
    }
}
