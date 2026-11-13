package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.SubjectEnrollment;
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
 * Data access object for subject enrollments.
 */
public class JdbcEnrollmentDao implements EnrollmentDao {

    private final ConnectionProvider connectionProvider;

    public JdbcEnrollmentDao() {
        this(Database.provider());
    }

    public JdbcEnrollmentDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String BASE_SELECT =
            "SELECT im.id_inscripcion, im.id_estudiante, im.id_materia, "
            + "im.año_academico, im.periodo_academico, im.seccion, "
            + "im.fecha_inscripcion, im.estado, im.fecha_creacion, "
            + "CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), "
            + "NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, m.nombre_materia "
            + "FROM inscripciones_materia im "
            + "INNER JOIN estudiantes e ON e.id_estudiante = im.id_estudiante "
            + "INNER JOIN materias m ON m.id_materia = im.id_materia ";

    public int insert(SubjectEnrollment enrollment) {
        String sql = "INSERT INTO inscripciones_materia (id_estudiante, id_materia, "
                + "año_academico, periodo_academico, seccion, fecha_inscripcion, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, enrollment);
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            enrollment.setIdEnrollment(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert enrollment", e);
            throw new DataAccessException("No se pudo registrar la inscripción.", e);
        }
    }

    public void update(SubjectEnrollment enrollment) {
        String sql = "UPDATE inscripciones_materia SET seccion = ?, estado = ? "
                + "WHERE id_inscripcion = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, enrollment.getSection());
            ps.setString(2, enrollment.getState());
            ps.setInt(3, enrollment.getIdEnrollment());
            DaoSupport.requireUpdated(ps.executeUpdate(), "la inscripción");
        } catch (SQLException e) {
            LoggerUtil.error("Could not update enrollment", e);
            throw new DataAccessException("No se pudo actualizar la inscripción.", e);
        }
    }

    public boolean delete(int idEnrollment) {
        String sql = "DELETE FROM inscripciones_materia WHERE id_inscripcion = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEnrollment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete enrollment", e);
            throw new DataAccessException("No se pudo eliminar la inscripción.", e);
        }
    }

    public SubjectEnrollment findById(int idEnrollment) {
        String sql = BASE_SELECT + "WHERE im.id_inscripcion = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEnrollment);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find enrollment", e);
            throw new DataAccessException("No se pudo consultar la inscripción.", e);
        }
    }

    public List<SubjectEnrollment> findAll() {
        List<SubjectEnrollment> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY im.año_academico DESC, im.periodo_academico, "
                + "student_name";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list enrollments", e);
            throw new DataAccessException("No se pudieron listar las inscripciones.", e);
        }
    }

    public List<SubjectEnrollment> findByStudent(int idStudent) {
        List<SubjectEnrollment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE im.id_estudiante = ? "
                + "ORDER BY im.año_academico DESC, im.periodo_academico";
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
            LoggerUtil.error("Could not list enrollments by student", e);
            throw new DataAccessException("No se pudieron listar las inscripciones.", e);
        }
    }

    public SubjectEnrollment findActive(int idStudent, int idSubject, int academicYear,
                                        String academicPeriod) {
        String sql = BASE_SELECT + "WHERE im.id_estudiante = ? AND im.id_materia = ? "
                + "AND im.año_academico = ? AND im.estado = 'Activa' ";
        if (academicPeriod != null) {
            sql += "AND im.periodo_academico = ? ";
        }
        sql += "ORDER BY im.periodo_academico DESC LIMIT 1";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, idSubject);
            ps.setInt(3, academicYear);
            if (academicPeriod != null) {
                ps.setString(4, academicPeriod);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find active enrollment", e);
            throw new DataAccessException("No se pudo validar la inscripción.", e);
        }
    }

    private void bind(PreparedStatement ps, SubjectEnrollment e) throws SQLException {
        ps.setInt(1, e.getIdStudent());
        ps.setInt(2, e.getIdSubject());
        ps.setInt(3, e.getAcademicYear());
        ps.setString(4, e.getAcademicPeriod());
        ps.setString(5, e.getSection());
        DaoSupport.setDate(ps, 6, e.getEnrollmentDate());
        ps.setString(7, e.getState());
    }

    private SubjectEnrollment map(ResultSet rs) throws SQLException {
        SubjectEnrollment e = new SubjectEnrollment();
        e.setIdEnrollment(rs.getInt("id_inscripcion"));
        e.setIdStudent(rs.getInt("id_estudiante"));
        e.setIdSubject(rs.getInt("id_materia"));
        e.setAcademicYear(rs.getInt("año_academico"));
        e.setAcademicPeriod(rs.getString("periodo_academico"));
        e.setSection(rs.getString("seccion"));
        e.setEnrollmentDate(DaoSupport.getDate(rs, "fecha_inscripcion"));
        e.setState(rs.getString("estado"));
        e.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        e.setStudentName(rs.getString("student_name"));
        e.setSubjectName(rs.getString("nombre_materia"));
        return e;
    }
}
