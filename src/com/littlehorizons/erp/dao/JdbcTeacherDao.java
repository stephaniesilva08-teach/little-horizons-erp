package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
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
 * Data access object for teachers.
 */
public class JdbcTeacherDao implements TeacherDao {

    private final ConnectionProvider connectionProvider;

    public JdbcTeacherDao() {
        this(Database.provider());
    }

    public JdbcTeacherDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String COLUMNS =
            "id_profesor, tipo_documento, numero_documento, primer_nombre, "
            + "segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, "
            + "telefono, especializacion, fecha_contratacion, estado, "
            + "fecha_creacion, fecha_actualizacion";

    public int insert(Teacher teacher) {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, "
                + "primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, "
                + "correo_electronico, telefono, especializacion, fecha_contratacion, "
                + "estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, teacher);
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            teacher.setIdTeacher(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert teacher", e);
            throw new DataAccessException("No se pudo registrar el profesor.", e);
        }
    }

    public void update(Teacher teacher) {
        String sql = "UPDATE profesores SET tipo_documento = ?, numero_documento = ?, "
                + "primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, "
                + "segundo_apellido = ?, correo_electronico = ?, telefono = ?, "
                + "especializacion = ?, fecha_contratacion = ?, estado = ? "
                + "WHERE id_profesor = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, teacher);
            ps.setInt(12, teacher.getIdTeacher());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update teacher", e);
            throw new DataAccessException("No se pudo actualizar el profesor.", e);
        }
    }

    public boolean delete(int idTeacher) {
        String sql = "DELETE FROM profesores WHERE id_profesor = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idTeacher);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete teacher", e);
            throw new DataAccessException("No se pudo eliminar el profesor.", e);
        }
    }

    public Teacher findById(int idTeacher) {
        String sql = "SELECT " + COLUMNS + " FROM profesores WHERE id_profesor = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idTeacher);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find teacher", e);
            throw new DataAccessException("No se pudo consultar el profesor.", e);
        }
    }

    public List<Teacher> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM profesores "
                + "ORDER BY primer_apellido, primer_nombre";
        List<Teacher> teachers = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                teachers.add(map(rs));
            }
            return teachers;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list teachers", e);
            throw new DataAccessException("No se pudieron listar los profesores.", e);
        }
    }

    public List<Teacher> findActive() {
        String sql = "SELECT " + COLUMNS + " FROM profesores WHERE estado = 'Activo' "
                + "ORDER BY primer_apellido, primer_nombre";
        List<Teacher> teachers = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                teachers.add(map(rs));
            }
            return teachers;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list active teachers", e);
            throw new DataAccessException("No se pudieron listar los profesores activos.", e);
        }
    }

    private void bind(PreparedStatement ps, Teacher t) throws SQLException {
        ps.setString(1, t.getDocumentType());
        ps.setString(2, t.getDocumentNumber());
        ps.setString(3, t.getFirstName());
        ps.setString(4, t.getSecondName());
        ps.setString(5, t.getFirstLastName());
        ps.setString(6, t.getSecondLastName());
        ps.setString(7, t.getEmail());
        ps.setString(8, t.getPhone());
        ps.setString(9, t.getSpecialization());
        DaoSupport.setDate(ps, 10, t.getHireDate());
        ps.setString(11, t.getState());
    }

    private Teacher map(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setIdTeacher(rs.getInt("id_profesor"));
        t.setDocumentType(rs.getString("tipo_documento"));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("primer_nombre"));
        t.setSecondName(rs.getString("segundo_nombre"));
        t.setFirstLastName(rs.getString("primer_apellido"));
        t.setSecondLastName(rs.getString("segundo_apellido"));
        t.setEmail(rs.getString("correo_electronico"));
        t.setPhone(rs.getString("telefono"));
        t.setSpecialization(rs.getString("especializacion"));
        t.setHireDate(DaoSupport.getDate(rs, "fecha_contratacion"));
        t.setState(rs.getString("estado"));
        t.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        t.setUpdateDate(DaoSupport.getTimestamp(rs, "fecha_actualizacion"));
        return t;
    }
}
