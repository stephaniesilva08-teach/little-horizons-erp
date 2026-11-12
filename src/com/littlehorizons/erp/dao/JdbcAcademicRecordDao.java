package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.SubjectAverage;
import com.littlehorizons.erp.util.ConnectionProvider;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for academic records (grades).
 */
public class JdbcAcademicRecordDao implements AcademicRecordDao {

    private final ConnectionProvider connectionProvider;

    public JdbcAcademicRecordDao() {
        this(Database.provider());
    }

    public JdbcAcademicRecordDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String BASE_SELECT =
            "SELECT r.id_registro, r.id_estudiante, r.id_materia, r.id_profesor, "
            + "r.año_academico, r.periodo_academico, r.valor_nota, r.observaciones, "
            + "r.fecha_registro, r.fecha_creacion, r.fecha_actualizacion, "
            + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, "
            + "e.segundo_apellido) AS student_name, m.nombre_materia AS subject_name, "
            + "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, "
            + "p.segundo_apellido) AS teacher_name "
            + "FROM registros_academicos r "
            + "INNER JOIN estudiantes e ON e.id_estudiante = r.id_estudiante "
            + "INNER JOIN materias m ON m.id_materia = r.id_materia "
            + "INNER JOIN profesores p ON p.id_profesor = r.id_profesor ";

    public int insert(AcademicRecord record) {
        String sql = "INSERT INTO registros_academicos (id_estudiante, id_materia, "
                + "id_profesor, año_academico, periodo_academico, valor_nota, "
                + "observaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, record);
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            record.setIdRecord(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert academic record", e);
            throw new DataAccessException("No se pudo registrar la nota.", e);
        }
    }

    public void update(AcademicRecord record) {
        String sql = "UPDATE registros_academicos SET valor_nota = ?, observaciones = ?, "
                + "fecha_registro = ? WHERE id_registro = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, record.getGrade());
            ps.setString(2, record.getObservations());
            DaoSupport.setDate(ps, 3, record.getRecordDate());
            ps.setInt(4, record.getIdRecord());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update academic record", e);
            throw new DataAccessException("No se pudo actualizar la nota.", e);
        }
    }

    public boolean delete(int idRecord) {
        String sql = "DELETE FROM registros_academicos WHERE id_registro = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idRecord);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete academic record", e);
            throw new DataAccessException("No se pudo eliminar la nota.", e);
        }
    }

    public AcademicRecord findById(int idRecord) {
        String sql = BASE_SELECT + "WHERE r.id_registro = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idRecord);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find academic record", e);
            throw new DataAccessException("No se pudo consultar la nota.", e);
        }
    }

    public List<AcademicRecord> findAll() {
        return queryList(BASE_SELECT
                + "ORDER BY r.año_academico DESC, r.periodo_academico, student_name");
    }

    public List<AcademicRecord> findByStudent(int idStudent, int academicYear) {
        String sql = BASE_SELECT + "WHERE r.id_estudiante = ? AND r.año_academico = ? "
                + "ORDER BY r.periodo_academico, m.nombre_materia";
        List<AcademicRecord> records = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(map(rs));
                }
            }
            return records;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list academic records", e);
            throw new DataAccessException("No se pudieron listar las notas.", e);
        }
    }

    /**
     * Returns every grade of a student from the first academic year on record.
     */
    public List<AcademicRecord> findHistoryByStudent(int idStudent) {
        String sql = BASE_SELECT + "WHERE r.id_estudiante = ? "
                + "ORDER BY r.año_academico DESC, r.periodo_academico, m.nombre_materia";
        List<AcademicRecord> records = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(map(rs));
                }
            }
            return records;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list full academic history", e);
            throw new DataAccessException("No se pudo consultar el historial académico.", e);
        }
    }

    /**
     * Returns the average grade per subject for a student in one academic year.
     */
    public List<SubjectAverage> findSubjectAverages(int idStudent, int academicYear) {
        String sql = "SELECT m.id_materia, m.nombre_materia, "
                + "AVG(r.valor_nota) AS promedio, COUNT(*) AS total "
                + "FROM registros_academicos r "
                + "INNER JOIN materias m ON m.id_materia = r.id_materia "
                + "WHERE r.id_estudiante = ? AND r.año_academico = ? "
                + "GROUP BY m.id_materia, m.nombre_materia ORDER BY m.nombre_materia";
        List<SubjectAverage> averages = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    averages.add(new SubjectAverage(rs.getInt("id_materia"),
                            rs.getString("nombre_materia"), academicYear,
                            rs.getBigDecimal("promedio"), rs.getInt("total")));
                }
            }
            return averages;
        } catch (SQLException e) {
            LoggerUtil.error("Could not calculate subject averages", e);
            throw new DataAccessException("No se pudieron calcular los promedios por materia.", e);
        }
    }

    public BigDecimal averageByStudent(int idStudent, int academicYear, String period) {
        String sql = "SELECT AVG(valor_nota) AS promedio FROM registros_academicos "
                + "WHERE id_estudiante = ? AND año_academico = ? AND periodo_academico = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, academicYear);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("promedio") : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not calculate student average", e);
            throw new DataAccessException("No se pudo calcular el promedio.", e);
        }
    }

    public BigDecimal overallAverageByStudent(int idStudent, int academicYear) {
        String sql = "SELECT AVG(valor_nota) AS promedio FROM registros_academicos "
                + "WHERE id_estudiante = ? AND año_academico = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("promedio") : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not calculate overall average", e);
            throw new DataAccessException("No se pudo calcular el promedio general.", e);
        }
    }

    private List<AcademicRecord> queryList(String sql) {
        List<AcademicRecord> records = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                records.add(map(rs));
            }
            return records;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list academic records", e);
            throw new DataAccessException("No se pudieron listar los registros académicos.", e);
        }
    }

    private void bind(PreparedStatement ps, AcademicRecord r) throws SQLException {
        ps.setInt(1, r.getIdStudent());
        ps.setInt(2, r.getIdSubject());
        ps.setInt(3, r.getIdTeacher());
        ps.setInt(4, r.getAcademicYear());
        ps.setString(5, r.getAcademicPeriod());
        ps.setBigDecimal(6, r.getGrade());
        ps.setString(7, r.getObservations());
        DaoSupport.setDate(ps, 8, r.getRecordDate());
    }

    private AcademicRecord map(ResultSet rs) throws SQLException {
        AcademicRecord r = new AcademicRecord();
        r.setIdRecord(rs.getInt("id_registro"));
        r.setIdStudent(rs.getInt("id_estudiante"));
        r.setIdSubject(rs.getInt("id_materia"));
        r.setIdTeacher(rs.getInt("id_profesor"));
        r.setAcademicYear(rs.getInt("año_academico"));
        r.setAcademicPeriod(rs.getString("periodo_academico"));
        r.setGrade(rs.getBigDecimal("valor_nota"));
        r.setObservations(rs.getString("observaciones"));
        r.setRecordDate(DaoSupport.getDate(rs, "fecha_registro"));
        r.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        r.setUpdateDate(DaoSupport.getTimestamp(rs, "fecha_actualizacion"));
        r.setStudentName(rs.getString("student_name"));
        r.setSubjectName(rs.getString("subject_name"));
        r.setTeacherName(rs.getString("teacher_name"));
        return r;
    }
}
