package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.CourseAverage;
import com.littlehorizons.erp.model.Debtor;
import com.littlehorizons.erp.model.GradeSummary;
import com.littlehorizons.erp.model.OverdueLoan;
import com.littlehorizons.erp.model.StudentGradeReport;
import com.littlehorizons.erp.util.ConnectionProvider;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only DAO used to build the management reports.
 */
public class JdbcReportDao implements ReportDao {

    private final ConnectionProvider connectionProvider;

    public JdbcReportDao() {
        this(Database.provider());
    }

    public JdbcReportDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public List<CourseAverage> courseAverages(int academicYear) {
        String sql = "SELECT m.id_materia, m.nombre_materia, g.id_grado, "
                + "COALESCE(im.seccion, '-') AS seccion, AVG(r.valor_nota) AS promedio, "
                + "COUNT(DISTINCT r.id_estudiante) AS estudiantes "
                + "FROM registros_academicos r "
                + "INNER JOIN materias m ON m.id_materia = r.id_materia "
                + "INNER JOIN grados g ON g.id_grado = m.id_grado "
                + "LEFT JOIN inscripciones_materia im ON im.id_estudiante = r.id_estudiante "
                + "AND im.id_materia = r.id_materia AND im.año_academico = r.año_academico "
                + "AND im.periodo_academico = r.periodo_academico "
                + "WHERE r.año_academico = ? "
                + "GROUP BY m.id_materia, m.nombre_materia, g.id_grado, seccion "
                + "ORDER BY m.nombre_materia, seccion";
        List<CourseAverage> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CourseAverage(rs.getInt("id_materia"),
                            rs.getString("nombre_materia"), rs.getInt("id_grado"),
                            rs.getString("seccion"), rs.getBigDecimal("promedio"),
                            rs.getInt("estudiantes")));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not build course averages", e);
            throw new DataAccessException("No se pudo generar el promedio por curso.", e);
        }
    }

    public List<StudentGradeReport> studentGradeReport(int idStudent, int academicYear) {
        String sql = "SELECT r.id_estudiante, CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), "
                + "NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, "
                + "m.nombre_materia, r.año_academico, r.periodo_academico, r.valor_nota "
                + "FROM registros_academicos r "
                + "INNER JOIN estudiantes e ON e.id_estudiante = r.id_estudiante "
                + "INNER JOIN materias m ON m.id_materia = r.id_materia "
                + "WHERE r.id_estudiante = ? AND r.año_academico = ? "
                + "ORDER BY m.nombre_materia, r.periodo_academico";
        List<StudentGradeReport> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentGradeReport(rs.getInt("id_estudiante"),
                            rs.getString("student_name"), rs.getString("nombre_materia"),
                            rs.getInt("año_academico"), rs.getString("periodo_academico"),
                            rs.getBigDecimal("valor_nota")));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not build student grade report", e);
            throw new DataAccessException("No se pudo generar el boletín del estudiante.", e);
        }
    }

    public List<GradeSummary> gradeSummary(int academicYear) {
        String sql = "SELECT m.nombre_materia, r.periodo_academico, AVG(r.valor_nota) AS promedio, "
                + "MAX(r.valor_nota) AS maxima, MIN(r.valor_nota) AS minima, "
                + "COUNT(*) AS registros FROM registros_academicos r "
                + "INNER JOIN materias m ON m.id_materia = r.id_materia "
                + "WHERE r.año_academico = ? "
                + "GROUP BY m.nombre_materia, r.periodo_academico "
                + "ORDER BY m.nombre_materia, r.periodo_academico";
        List<GradeSummary> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new GradeSummary(rs.getString("nombre_materia"),
                            rs.getString("periodo_academico"), rs.getBigDecimal("promedio"),
                            rs.getBigDecimal("maxima"), rs.getBigDecimal("minima"),
                            rs.getInt("registros")));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not build grade summary", e);
            throw new DataAccessException("No se pudo generar el resumen de notas.", e);
        }
    }

    public List<Debtor> debtors() {
        String sql = "WITH saldos AS (SELECT c.id_estudiante, c.id_cargo, "
                + "c.monto AS charged, COALESCE(SUM(pc.monto_aplicado), 0) AS paid, "
                + "c.fecha_vencimiento FROM cargos_estudiante c "
                + "LEFT JOIN pagos_cargos pc ON pc.id_cargo = c.id_cargo "
                + "WHERE c.estado <> 'Anulado' "
                + "GROUP BY c.id_estudiante, c.id_cargo, c.monto, c.fecha_vencimiento), "
                + "morosos AS (SELECT id_estudiante, charged, paid, "
                + "charged - paid AS balance FROM saldos "
                + "WHERE fecha_vencimiento < CURRENT_DATE - INTERVAL 1 MONTH "
                + "AND charged > paid) "
                + "SELECT e.id_estudiante, CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), "
                + "NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, "
                + "SUM(m.charged) AS charged, SUM(m.paid) AS paid, "
                + "SUM(m.balance) AS balance FROM morosos m "
                + "INNER JOIN estudiantes e ON e.id_estudiante = m.id_estudiante "
                + "GROUP BY e.id_estudiante, e.primer_nombre, e.segundo_nombre, "
                + "e.primer_apellido, e.segundo_apellido "
                + "HAVING SUM(m.balance) > 0 "
                + "ORDER BY balance DESC";
        List<Debtor> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Debtor(rs.getInt("id_estudiante"),
                        rs.getString("student_name"), rs.getBigDecimal("charged"),
                        rs.getBigDecimal("paid"), rs.getBigDecimal("balance")));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not build debtor report", e);
            throw new DataAccessException("No se pudo generar el reporte de morosos.", e);
        }
    }

    public List<OverdueLoan> overdueLoans() {
        String sql = "SELECT ps.id_prestamo, r.nombre_recurso, CONCAT_WS(' ', "
                + "NULLIF(TRIM(e.primer_nombre), ''), NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), NULLIF(TRIM(e.segundo_apellido), '')) "
                + "AS borrower_name, 'Estudiante' AS borrower_type, "
                + "ps.fecha_devolucion_esperada FROM prestamos_estudiante ps "
                + "INNER JOIN recursos r ON r.id_recurso = ps.id_recurso "
                + "INNER JOIN estudiantes e ON e.id_estudiante = ps.id_estudiante "
                + "WHERE ps.estado_prestamo IN ('Activo', 'Vencido') "
                + "AND ps.fecha_devolucion_esperada < CURRENT_DATE "
                + "UNION ALL "
                + "SELECT pp.id_prestamo, r.nombre_recurso, CONCAT_WS(' ', "
                + "NULLIF(TRIM(p.primer_nombre), ''), NULLIF(TRIM(p.segundo_nombre), ''), NULLIF(TRIM(p.primer_apellido), ''), NULLIF(TRIM(p.segundo_apellido), '')), "
                + "'Profesor', pp.fecha_devolucion_esperada FROM prestamos_profesor pp "
                + "INNER JOIN recursos r ON r.id_recurso = pp.id_recurso "
                + "INNER JOIN profesores p ON p.id_profesor = pp.id_profesor "
                + "WHERE pp.estado_prestamo IN ('Activo', 'Vencido') "
                + "AND pp.fecha_devolucion_esperada < CURRENT_DATE "
                + "ORDER BY fecha_devolucion_esperada";
        List<OverdueLoan> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new OverdueLoan(rs.getInt("id_prestamo"),
                        rs.getString("nombre_recurso"), rs.getString("borrower_name"),
                        rs.getString("borrower_type"),
                        DaoSupport.getDate(rs, "fecha_devolucion_esperada")));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not build overdue loan report", e);
            throw new DataAccessException("No se pudo generar el reporte de préstamos vencidos.", e);
        }
    }

    public List<String> enrollmentSummary(int academicYear) {
        String sql = "SELECT g.nombre_grado, COUNT(*) AS total FROM matriculas m "
                + "INNER JOIN grados g ON g.id_grado = m.id_grado "
                + "WHERE m.año_academico = ? AND m.estado_matricula = 'Activa' "
                + "GROUP BY g.nombre_grado, g.nivel_numerico ORDER BY g.nivel_numerico";
        List<String> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("nombre_grado") + ": " + rs.getInt("total")
                            + " estudiante(s)");
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not build enrollment summary", e);
            throw new DataAccessException("No se pudo generar el resumen de matrícula.", e);
        }
    }
}
