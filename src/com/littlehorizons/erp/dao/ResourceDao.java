package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.util.ConnectionFactory;
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
import java.util.Set;

/**
 * Data access object for resources, categories and loans.
 *
 * <p>Loans update the resource inventory state inside the same transaction.</p>
 */
public class ResourceDao {

    /** Only these tables may be referenced when returning a loan. */
    private static final Set<String> LOAN_TABLES = Set.of(
            "prestamos_estudiante", "prestamos_profesor");

    private static final String RESOURCE_SELECT =
            "SELECT r.id_recurso, r.codigo_recurso, r.nombre_recurso, r.id_categoria, "
            + "r.descripcion, r.fecha_adquisicion, r.ubicacion_fisica, r.estado_recurso, "
            + "r.fecha_creacion, r.fecha_actualizacion, c.nombre_categoria "
            + "FROM recursos r "
            + "INNER JOIN categorias_recurso c ON c.id_categoria = r.id_categoria ";

    private static final String STUDENT_LOAN_SELECT =
            "SELECT ps.id_prestamo, ps.id_recurso, ps.id_estudiante, ps.fecha_prestamo, "
            + "ps.fecha_devolucion_esperada, ps.fecha_devolucion_real, ps.estado_prestamo, "
            + "ps.observaciones, ps.fecha_creacion, r.nombre_recurso, "
            + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, "
            + "e.segundo_apellido) AS borrower_name "
            + "FROM prestamos_estudiante ps "
            + "INNER JOIN recursos r ON r.id_recurso = ps.id_recurso "
            + "INNER JOIN estudiantes e ON e.id_estudiante = ps.id_estudiante ";

    private static final String TEACHER_LOAN_SELECT =
            "SELECT pp.id_prestamo, pp.id_recurso, pp.id_profesor, pp.fecha_prestamo, "
            + "pp.fecha_devolucion_esperada, pp.fecha_devolucion_real, pp.estado_prestamo, "
            + "pp.observaciones, pp.fecha_creacion, r.nombre_recurso, "
            + "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, "
            + "p.segundo_apellido) AS borrower_name "
            + "FROM prestamos_profesor pp "
            + "INNER JOIN recursos r ON r.id_recurso = pp.id_recurso "
            + "INNER JOIN profesores p ON p.id_profesor = pp.id_profesor ";

    public List<ResourceCategory> findAllCategories() {
        String sql = "SELECT id_categoria, nombre_categoria, descripcion "
                + "FROM categorias_recurso ORDER BY nombre_categoria";
        List<ResourceCategory> list = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ResourceCategory(rs.getInt("id_categoria"),
                        rs.getString("nombre_categoria"), rs.getString("descripcion")));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list resource categories", e);
            throw new DataAccessException("No se pudieron listar las categorías.", e);
        }
    }

    public int insertCategory(ResourceCategory category) {
        String sql = "INSERT INTO categorias_recurso (nombre_categoria, descripcion) "
                + "VALUES (?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            category.setIdCategory(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert resource category", e);
            throw new DataAccessException("No se pudo registrar la categoría.", e);
        }
    }

    public int insertResource(Resource resource) {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, "
                + "descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getResourceCode());
            ps.setString(2, resource.getName());
            ps.setInt(3, resource.getIdCategory());
            ps.setString(4, resource.getDescription());
            DaoSupport.setDate(ps, 5, resource.getAcquisitionDate());
            ps.setString(6, resource.getLocation());
            ps.setString(7, resource.getState());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            resource.setIdResource(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert resource", e);
            throw new DataAccessException("No se pudo registrar el recurso.", e);
        }
    }

    public void updateResource(Resource resource) {
        String sql = "UPDATE recursos SET codigo_recurso = ?, nombre_recurso = ?, "
                + "id_categoria = ?, descripcion = ?, fecha_adquisicion = ?, "
                + "ubicacion_fisica = ?, estado_recurso = ? WHERE id_recurso = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, resource.getResourceCode());
            ps.setString(2, resource.getName());
            ps.setInt(3, resource.getIdCategory());
            ps.setString(4, resource.getDescription());
            DaoSupport.setDate(ps, 5, resource.getAcquisitionDate());
            ps.setString(6, resource.getLocation());
            ps.setString(7, resource.getState());
            ps.setInt(8, resource.getIdResource());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update resource", e);
            throw new DataAccessException("No se pudo actualizar el recurso.", e);
        }
    }

    public boolean deleteResource(int idResource) {
        String sql = "DELETE FROM recursos WHERE id_recurso = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idResource);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete resource", e);
            throw new DataAccessException("No se pudo eliminar el recurso.", e);
        }
    }

    public Resource findResourceById(int idResource) {
        String sql = RESOURCE_SELECT + "WHERE r.id_recurso = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idResource);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResource(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find resource", e);
            throw new DataAccessException("No se pudo consultar el recurso.", e);
        }
    }

    public List<Resource> findAllResources() {
        return queryResources(RESOURCE_SELECT + "ORDER BY r.nombre_recurso");
    }

    public List<Resource> findAvailableResources() {
        return queryResources(RESOURCE_SELECT + "WHERE r.estado_recurso = 'Disponible' "
                + "ORDER BY r.nombre_recurso");
    }

    public int insertStudentLoan(Loan loan) {
        String sql = "INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, "
                + "fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        return insertLoan(sql, loan, false);
    }

    public int insertTeacherLoan(Loan loan) {
        String sql = "INSERT INTO prestamos_profesor (id_recurso, id_profesor, "
                + "fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        return insertLoan(sql, loan, true);
    }

    public Loan findStudentLoanById(int idLoan) {
        String sql = STUDENT_LOAN_SELECT + "WHERE ps.id_prestamo = ?";
        return querySingleLoan(sql, idLoan, "Estudiante");
    }

    public Loan findTeacherLoanById(int idLoan) {
        String sql = TEACHER_LOAN_SELECT + "WHERE pp.id_prestamo = ?";
        return querySingleLoan(sql, idLoan, "Profesor");
    }

    public List<Loan> findStudentLoans() {
        return queryLoans(STUDENT_LOAN_SELECT + "ORDER BY ps.fecha_prestamo DESC", "Estudiante");
    }

    public List<Loan> findTeacherLoans() {
        return queryLoans(TEACHER_LOAN_SELECT + "ORDER BY pp.fecha_prestamo DESC", "Profesor");
    }

    public List<Loan> findLoansByStudent(int idStudent) {
        return queryLoans(STUDENT_LOAN_SELECT + "WHERE ps.id_estudiante = ? "
                + "ORDER BY ps.fecha_prestamo DESC", "Estudiante", idStudent);
    }

    public List<Loan> findLoansByTeacher(int idTeacher) {
        return queryLoans(TEACHER_LOAN_SELECT + "WHERE pp.id_profesor = ? "
                + "ORDER BY pp.fecha_prestamo DESC", "Profesor", idTeacher);
    }

    public void returnStudentLoan(int idLoan) {
        returnLoan("prestamos_estudiante", idLoan);
    }

    public void returnTeacherLoan(int idLoan) {
        returnLoan("prestamos_profesor", idLoan);
    }

    private int insertLoan(String sql, Loan loan, boolean teacher) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id;
                int idResource;
                try (PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, loan.getIdResource());
                    ps.setInt(2, teacher ? loan.getIdTeacher() : loan.getIdStudent());
                    DaoSupport.setDate(ps, 3, loan.getLoanDate());
                    DaoSupport.setDate(ps, 4, loan.getExpectedReturnDate());
                    ps.setString(5, "Activo");
                    ps.setString(6, loan.getObservations());
                    ps.executeUpdate();
                    id = DaoSupport.generatedKey(ps);
                    idResource = loan.getIdResource();
                }
                updateResourceState(connection, idResource, "Prestado");
                connection.commit();
                loan.setIdLoan(id);
                return id;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not register loan", e);
            throw new DataAccessException("No se pudo registrar el préstamo.", e);
        }
    }

    private void returnLoan(String table, int idLoan) {
        if (!LOAN_TABLES.contains(table)) {
            throw new IllegalArgumentException("Tipo de préstamo no válido.");
        }
        String selectSql = "SELECT id_recurso FROM " + table + " WHERE id_prestamo = ?";
        String updateSql = "UPDATE " + table + " SET fecha_devolucion_real = ?, "
                + "estado_prestamo = 'Devuelto' WHERE id_prestamo = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int idResource;
                try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                    ps.setInt(1, idLoan);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new DataAccessException(
                                    "No existe el préstamo indicado.", null);
                        }
                        idResource = rs.getInt("id_recurso");
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                    ps.setInt(2, idLoan);
                    ps.executeUpdate();
                }
                updateResourceState(connection, idResource, "Disponible");
                connection.commit();
                LoggerUtil.info("Loan returned: " + table + " #" + idLoan);
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not return loan", e);
            throw new DataAccessException("No se pudo registrar la devolución.", e);
        }
    }

    private void updateResourceState(Connection connection, int idResource, String state)
            throws SQLException {
        String sql = "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, state);
            ps.setInt(2, idResource);
            ps.executeUpdate();
        }
    }

    private List<Resource> queryResources(String sql) {
        List<Resource> list = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResource(rs));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list resources", e);
            throw new DataAccessException("No se pudieron listar los recursos.", e);
        }
    }

    private Loan querySingleLoan(String sql, int idLoan, String type) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idLoan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapLoan(rs, type) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find loan", e);
            throw new DataAccessException("No se pudo consultar el préstamo.", e);
        }
    }

    private List<Loan> queryLoans(String sql, String type) {
        List<Loan> list = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapLoan(rs, type));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list loans", e);
            throw new DataAccessException("No se pudieron listar los préstamos.", e);
        }
    }

    private List<Loan> queryLoans(String sql, String type, int parameter) {
        List<Loan> list = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLoan(rs, type));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list loans", e);
            throw new DataAccessException("No se pudieron listar los préstamos.", e);
        }
    }

    private Resource mapResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setIdResource(rs.getInt("id_recurso"));
        r.setResourceCode(rs.getString("codigo_recurso"));
        r.setName(rs.getString("nombre_recurso"));
        r.setIdCategory(rs.getInt("id_categoria"));
        r.setDescription(rs.getString("descripcion"));
        r.setAcquisitionDate(DaoSupport.getDate(rs, "fecha_adquisicion"));
        r.setLocation(rs.getString("ubicacion_fisica"));
        r.setState(rs.getString("estado_recurso"));
        r.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        r.setUpdateDate(DaoSupport.getTimestamp(rs, "fecha_actualizacion"));
        r.setCategoryName(rs.getString("nombre_categoria"));
        return r;
    }

    private Loan mapLoan(ResultSet rs, String type) throws SQLException {
        Loan l = new Loan();
        l.setIdLoan(rs.getInt("id_prestamo"));
        l.setIdResource(rs.getInt("id_recurso"));
        if ("Estudiante".equals(type)) {
            l.setIdStudent(rs.getInt("id_estudiante"));
        } else {
            l.setIdTeacher(rs.getInt("id_profesor"));
        }
        l.setLoanDate(DaoSupport.getDate(rs, "fecha_prestamo"));
        l.setExpectedReturnDate(DaoSupport.getDate(rs, "fecha_devolucion_esperada"));
        l.setActualReturnDate(DaoSupport.getDate(rs, "fecha_devolucion_real"));
        l.setState(rs.getString("estado_prestamo"));
        l.setObservations(rs.getString("observaciones"));
        l.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        l.setResourceName(rs.getString("nombre_recurso"));
        l.setBorrowerName(rs.getString("borrower_name"));
        return l;
    }
}
