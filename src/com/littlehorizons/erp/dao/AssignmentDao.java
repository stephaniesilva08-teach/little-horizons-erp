package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.ConnectionFactory;
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
public class AssignmentDao {

    private static final String ASSIGNMENT_SELECT =
            "SELECT ap.id_asignacion, ap.id_profesor, ap.id_materia, ap.año_academico, "
            + "ap.seccion, ap.fecha_creacion, "
            + "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, "
            + "p.segundo_apellido) AS teacher_name, m.nombre_materia "
            + "FROM asignaciones_profesor ap "
            + "INNER JOIN profesores p ON p.id_profesor = ap.id_profesor "
            + "INNER JOIN materias m ON m.id_materia = ap.id_materia ";

    public int insert(TeacherAssignment assignment) {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, "
                + "año_academico, seccion) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
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
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindAssignment(ps, assignment);
            ps.setInt(5, assignment.getIdAssignment());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update assignment", e);
            throw new DataAccessException("No se pudo actualizar la asignación.", e);
        }
    }

    public boolean delete(int idAssignment) {
        String sql = "DELETE FROM asignaciones_profesor WHERE id_asignacion = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
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
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
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

    public int insertSchedule(Schedule schedule) {
        String sql = "INSERT INTO horarios (id_asignacion, dia_semana, hora_inicio, "
                + "hora_fin) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, schedule.getIdAssignment());
            ps.setString(2, schedule.getDayOfWeek());
            ps.setString(3, schedule.getStartTime());
            ps.setString(4, schedule.getEndTime());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            schedule.setIdSchedule(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert schedule", e);
            throw new DataAccessException("No se pudo registrar el horario.", e);
        }
    }

    public void updateSchedule(Schedule schedule) {
        String sql = "UPDATE horarios SET dia_semana = ?, hora_inicio = ?, "
                + "hora_fin = ? WHERE id_horario = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schedule.getDayOfWeek());
            ps.setString(2, schedule.getStartTime());
            ps.setString(3, schedule.getEndTime());
            ps.setInt(4, schedule.getIdSchedule());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update schedule", e);
            throw new DataAccessException("No se pudo actualizar el horario.", e);
        }
    }

    public boolean deleteSchedule(int idSchedule) {
        String sql = "DELETE FROM horarios WHERE id_horario = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
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
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
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
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
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
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
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
