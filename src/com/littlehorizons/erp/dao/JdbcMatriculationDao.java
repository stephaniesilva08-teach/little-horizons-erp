package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Matriculation;
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
 * Data access object for annual matriculations.
 */
public class JdbcMatriculationDao implements MatriculationDao {

    private final ConnectionProvider connectionProvider;

    public JdbcMatriculationDao() {
        this(Database.provider());
    }

    public JdbcMatriculationDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String BASE_SELECT =
            "SELECT m.id_matricula, m.id_estudiante, m.id_grado, m.año_academico, "
            + "m.estado_matricula, m.fecha_matricula, m.fecha_retiro, m.observaciones, "
            + "CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), "
            + "NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, g.nombre_grado "
            + "FROM matriculas m "
            + "INNER JOIN estudiantes e ON e.id_estudiante = m.id_estudiante "
            + "INNER JOIN grados g ON g.id_grado = m.id_grado ";

    public int insert(Matriculation matriculation) {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, año_academico, "
                + "estado_matricula, fecha_matricula, fecha_retiro, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, matriculation.getIdStudent());
            ps.setInt(2, matriculation.getIdGrade());
            ps.setInt(3, matriculation.getAcademicYear());
            ps.setString(4, matriculation.getState());
            DaoSupport.setDate(ps, 5, matriculation.getEnrollmentDate());
            DaoSupport.setDate(ps, 6, matriculation.getWithdrawalDate());
            ps.setString(7, matriculation.getObservations());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            matriculation.setIdMatriculation(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert matriculation", e);
            throw new DataAccessException("No se pudo registrar la matrícula.", e);
        }
    }

    public void update(Matriculation matriculation) {
        String sql = "UPDATE matriculas SET id_grado = ?, estado_matricula = ?, "
                + "fecha_retiro = ?, observaciones = ? WHERE id_matricula = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, matriculation.getIdGrade());
            ps.setString(2, matriculation.getState());
            DaoSupport.setDate(ps, 3, matriculation.getWithdrawalDate());
            ps.setString(4, matriculation.getObservations());
            ps.setInt(5, matriculation.getIdMatriculation());
            DaoSupport.requireUpdated(ps.executeUpdate(), "la matrícula");
        } catch (SQLException e) {
            LoggerUtil.error("Could not update matriculation", e);
            throw new DataAccessException("No se pudo actualizar la matrícula.", e);
        }
    }

    public boolean delete(int idMatriculation) {
        String sql = "DELETE FROM matriculas WHERE id_matricula = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMatriculation);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete matriculation", e);
            throw new DataAccessException("No se pudo eliminar la matrícula.", e);
        }
    }

    public Matriculation findById(int idMatriculation) {
        String sql = BASE_SELECT + "WHERE m.id_matricula = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMatriculation);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find matriculation", e);
            throw new DataAccessException("No se pudo consultar la matrícula.", e);
        }
    }

    public List<Matriculation> findAll() {
        return queryList(BASE_SELECT + "ORDER BY m.año_academico DESC, student_name", 0);
    }

    public List<Matriculation> findByStudent(int idStudent) {
        return queryList(BASE_SELECT + "WHERE m.id_estudiante = ? "
                + "ORDER BY m.año_academico DESC", idStudent);
    }

    public List<Matriculation> findByYear(int academicYear) {
        String sql = BASE_SELECT + "WHERE m.año_academico = ? ORDER BY student_name";
        List<Matriculation> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list matriculations by year", e);
            throw new DataAccessException("No se pudieron listar las matrículas.", e);
        }
    }

    private List<Matriculation> queryList(String sql, int parameter) {
        List<Matriculation> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (parameter > 0) {
                ps.setInt(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list matriculations", e);
            throw new DataAccessException("No se pudieron listar las matrículas.", e);
        }
    }

    private Matriculation map(ResultSet rs) throws SQLException {
        Matriculation m = new Matriculation();
        m.setIdMatriculation(rs.getInt("id_matricula"));
        m.setIdStudent(rs.getInt("id_estudiante"));
        m.setIdGrade(rs.getInt("id_grado"));
        m.setAcademicYear(rs.getInt("año_academico"));
        m.setState(rs.getString("estado_matricula"));
        m.setEnrollmentDate(DaoSupport.getDate(rs, "fecha_matricula"));
        m.setWithdrawalDate(DaoSupport.getDate(rs, "fecha_retiro"));
        m.setObservations(rs.getString("observaciones"));
        m.setStudentName(rs.getString("student_name"));
        m.setGradeName(rs.getString("nombre_grado"));
        return m;
    }
}
