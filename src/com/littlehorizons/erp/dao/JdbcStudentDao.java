package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.ConnectionProvider;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for students.
 */
public class JdbcStudentDao implements StudentDao {

    private final ConnectionProvider connectionProvider;

    public JdbcStudentDao() {
        this(Database.provider());
    }

    public JdbcStudentDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String COLUMNS =
            "id_estudiante, tipo_documento, numero_documento, primer_nombre, "
            + "segundo_nombre, primer_apellido, segundo_apellido, fecha_nacimiento, "
            + "genero, direccion, telefono, correo_electronico, tipo_sangre, "
            + "alergias, condiciones_medicas, fecha_ingreso, estado, "
            + "fecha_creacion, fecha_actualizacion";

    public int insert(Student student) {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, "
                + "primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, "
                + "fecha_nacimiento, genero, direccion, telefono, correo_electronico, "
                + "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection()) {
            int id = insert(connection, sql, student);
            LoggerUtil.info("Student created with id " + id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert student", e);
            throw new DataAccessException("No se pudo registrar el estudiante.", e);
        }
    }

    @Override
    public int insertWithGuardian(Student student, int idParent) {
        String studentSql = "INSERT INTO estudiantes (tipo_documento, numero_documento, "
                + "primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, "
                + "fecha_nacimiento, genero, direccion, telefono, correo_electronico, "
                + "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String guardianSql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, "
                + "es_principal, es_contacto_emergencia) VALUES (?, ?, TRUE, TRUE)";
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int idStudent = insert(connection, studentSql, student);
                try (PreparedStatement ps = connection.prepareStatement(guardianSql)) {
                    ps.setInt(1, idStudent);
                    ps.setInt(2, idParent);
                    ps.executeUpdate();
                }
                connection.commit();
                LoggerUtil.info("Student created with guardian: " + idStudent);
                return idStudent;
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
            LoggerUtil.error("Could not insert student with guardian", e);
            throw new DataAccessException(
                    "No se pudo registrar el estudiante con su acudiente.", e);
        }
    }

    private int insert(Connection connection, String sql, Student student)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, student);
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            student.setIdStudent(id);
            return id;
        }
    }

    public void update(Student student) {
        String sql = "UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, "
                + "primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, "
                + "segundo_apellido = ?, fecha_nacimiento = ?, genero = ?, "
                + "direccion = ?, telefono = ?, correo_electronico = ?, "
                + "tipo_sangre = ?, alergias = ?, condiciones_medicas = ?, "
                + "fecha_ingreso = ?, estado = ? WHERE id_estudiante = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, student);
            ps.setInt(17, student.getIdStudent());
            ps.executeUpdate();
            LoggerUtil.info("Student updated: " + student.getIdStudent());
        } catch (SQLException e) {
            LoggerUtil.error("Could not update student", e);
            throw new DataAccessException("No se pudo actualizar el estudiante.", e);
        }
    }

    public boolean delete(int idStudent) {
        String sql = "DELETE FROM estudiantes WHERE id_estudiante = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            LoggerUtil.error("Student has financial history and cannot be deleted", e);
            throw new DataAccessException(
                    "No se puede eliminar el estudiante porque tiene historial financiero "
                    + "(pagos o cargos). Use el estado 'Retirado' en su lugar.", e);
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete student", e);
            throw new DataAccessException("No se pudo eliminar el estudiante.", e);
        }
    }

    public Student findById(int idStudent) {
        String sql = "SELECT " + COLUMNS + " FROM estudiantes WHERE id_estudiante = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find student", e);
            throw new DataAccessException("No se pudo consultar el estudiante.", e);
        }
    }

    public Student findByDocument(String documentNumber) {
        String sql = "SELECT " + COLUMNS + " FROM estudiantes WHERE numero_documento = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find student by document", e);
            throw new DataAccessException("No se pudo consultar el estudiante.", e);
        }
    }

    public List<Student> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM estudiantes ORDER BY primer_apellido, primer_nombre";
        return queryList(sql, null);
    }

    public List<Student> findActive() {
        String sql = "SELECT " + COLUMNS + " FROM estudiantes WHERE estado = 'Activo' "
                + "ORDER BY primer_apellido, primer_nombre";
        return queryList(sql, null);
    }

    public List<Student> search(String text) {
        String sql = "SELECT " + COLUMNS + " FROM estudiantes WHERE numero_documento LIKE ? "
                + "OR primer_nombre LIKE ? OR primer_apellido LIKE ? "
                + "ORDER BY primer_apellido, primer_nombre";
        String pattern = "%" + text + "%";
        return queryList(sql, new String[]{pattern, pattern, pattern});
    }

    public void assignGuardian(int idStudent, int idParent, boolean main,
                               boolean emergency) {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, "
                + "es_principal, es_contacto_emergencia) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE es_principal = VALUES(es_principal), "
                + "es_contacto_emergencia = VALUES(es_contacto_emergencia)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, idParent);
            ps.setBoolean(3, main);
            ps.setBoolean(4, emergency);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not link guardian", e);
            throw new DataAccessException("No se pudo asociar el acudiente.", e);
        }
    }

    public boolean exists(int idStudent) {
        return findById(idStudent) != null;
    }

    private List<Student> queryList(String sql, String[] parameters) {
        List<Student> students = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    ps.setString(i + 1, parameters[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(map(rs));
                }
            }
            return students;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list students", e);
            throw new DataAccessException("No se pudieron listar los estudiantes.", e);
        }
    }

    private void bind(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getDocumentType());
        ps.setString(2, s.getDocumentNumber());
        ps.setString(3, s.getFirstName());
        ps.setString(4, s.getSecondName());
        ps.setString(5, s.getFirstLastName());
        ps.setString(6, s.getSecondLastName());
        DaoSupport.setDate(ps, 7, s.getBirthDate());
        ps.setString(8, s.getGender());
        ps.setString(9, s.getAddress());
        ps.setString(10, s.getPhone());
        ps.setString(11, s.getEmail());
        ps.setString(12, s.getBloodType());
        ps.setString(13, s.getAllergies());
        ps.setString(14, s.getMedicalConditions());
        DaoSupport.setDate(ps, 15, s.getEntryDate());
        ps.setString(16, s.getEnrollmentState());
    }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setIdStudent(rs.getInt("id_estudiante"));
        s.setDocumentType(rs.getString("tipo_documento"));
        s.setDocumentNumber(rs.getString("numero_documento"));
        s.setFirstName(rs.getString("primer_nombre"));
        s.setSecondName(rs.getString("segundo_nombre"));
        s.setFirstLastName(rs.getString("primer_apellido"));
        s.setSecondLastName(rs.getString("segundo_apellido"));
        s.setBirthDate(DaoSupport.getDate(rs, "fecha_nacimiento"));
        s.setGender(rs.getString("genero"));
        s.setAddress(rs.getString("direccion"));
        s.setPhone(rs.getString("telefono"));
        s.setEmail(rs.getString("correo_electronico"));
        s.setBloodType(rs.getString("tipo_sangre"));
        s.setAllergies(rs.getString("alergias"));
        s.setMedicalConditions(rs.getString("condiciones_medicas"));
        s.setEntryDate(DaoSupport.getDate(rs, "fecha_ingreso"));
        s.setEnrollmentState(rs.getString("estado"));
        s.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        s.setUpdateDate(DaoSupport.getTimestamp(rs, "fecha_actualizacion"));
        return s;
    }
}
