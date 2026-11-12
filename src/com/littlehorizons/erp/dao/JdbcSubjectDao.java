package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Subject;
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
 * Data access object for subjects.
 */
public class JdbcSubjectDao implements SubjectDao {

    private final ConnectionProvider connectionProvider;

    public JdbcSubjectDao() {
        this(Database.provider());
    }

    public JdbcSubjectDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String COLUMNS =
            "id_materia, codigo_materia, nombre_materia, descripcion, id_grado, "
            + "horas_semanales, esta_activa, fecha_creacion";

    public int insert(Subject subject) {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, "
                + "id_grado, horas_semanales, esta_activa) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, subject);
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            subject.setIdSubject(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert subject", e);
            throw new DataAccessException("No se pudo registrar la materia.", e);
        }
    }

    public void update(Subject subject) {
        String sql = "UPDATE materias SET codigo_materia = ?, nombre_materia = ?, "
                + "descripcion = ?, id_grado = ?, horas_semanales = ?, esta_activa = ? "
                + "WHERE id_materia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, subject);
            ps.setInt(7, subject.getIdSubject());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update subject", e);
            throw new DataAccessException("No se pudo actualizar la materia.", e);
        }
    }

    public boolean delete(int idSubject) {
        String sql = "DELETE FROM materias WHERE id_materia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSubject);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete subject", e);
            throw new DataAccessException("No se pudo eliminar la materia.", e);
        }
    }

    public Subject findById(int idSubject) {
        String sql = "SELECT " + COLUMNS + " FROM materias WHERE id_materia = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSubject);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find subject", e);
            throw new DataAccessException("No se pudo consultar la materia.", e);
        }
    }

    public List<Subject> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM materias ORDER BY nombre_materia";
        return queryList(sql, null);
    }

    public List<Subject> findActive() {
        String sql = "SELECT " + COLUMNS + " FROM materias WHERE esta_activa = TRUE "
                + "ORDER BY nombre_materia";
        return queryList(sql, null);
    }

    public List<Subject> findByGrade(int idGrade) {
        String sql = "SELECT " + COLUMNS + " FROM materias WHERE id_grado = ? "
                + "ORDER BY nombre_materia";
        List<Subject> subjects = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idGrade);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    subjects.add(map(rs));
                }
            }
            return subjects;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list subjects by grade", e);
            throw new DataAccessException("No se pudieron listar las materias del grado.", e);
        }
    }

    private List<Subject> queryList(String sql, String[] parameters) {
        List<Subject> subjects = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    ps.setString(i + 1, parameters[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    subjects.add(map(rs));
                }
            }
            return subjects;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list subjects", e);
            throw new DataAccessException("No se pudieron listar las materias.", e);
        }
    }

    private void bind(PreparedStatement ps, Subject s) throws SQLException {
        ps.setString(1, s.getSubjectCode());
        ps.setString(2, s.getName());
        ps.setString(3, s.getDescription());
        ps.setInt(4, s.getIdGrade());
        ps.setInt(5, s.getWeeklyHours());
        ps.setBoolean(6, s.isActive());
    }

    private Subject map(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setIdSubject(rs.getInt("id_materia"));
        s.setSubjectCode(rs.getString("codigo_materia"));
        s.setName(rs.getString("nombre_materia"));
        s.setDescription(rs.getString("descripcion"));
        s.setIdGrade(rs.getInt("id_grado"));
        s.setWeeklyHours(rs.getInt("horas_semanales"));
        s.setActive(rs.getBoolean("esta_activa"));
        s.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        return s;
    }
}
