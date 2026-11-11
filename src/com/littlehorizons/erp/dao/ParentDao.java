package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Parent;
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
 * Data access object for guardians (acudientes).
 */
public class ParentDao {

    private static final String COLUMNS =
            "id_acudiente, tipo_documento, numero_documento, primer_nombre, "
            + "segundo_nombre, primer_apellido, segundo_apellido, parentesco, "
            + "telefono_principal, telefono_secundario, correo_electronico, "
            + "direccion, ocupacion, es_responsable_financiero, "
            + "fecha_creacion, fecha_actualizacion";

    public int insert(Parent parent) {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, "
                + "primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, "
                + "parentesco, telefono_principal, telefono_secundario, "
                + "correo_electronico, direccion, ocupacion, es_responsable_financiero) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, parent);
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            parent.setIdParent(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert guardian", e);
            throw new DataAccessException("No se pudo registrar el acudiente.", e);
        }
    }

    public void update(Parent parent) {
        String sql = "UPDATE acudientes SET tipo_documento = ?, numero_documento = ?, "
                + "primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, "
                + "segundo_apellido = ?, parentesco = ?, telefono_principal = ?, "
                + "telefono_secundario = ?, correo_electronico = ?, direccion = ?, "
                + "ocupacion = ?, es_responsable_financiero = ? WHERE id_acudiente = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, parent);
            ps.setInt(14, parent.getIdParent());
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not update guardian", e);
            throw new DataAccessException("No se pudo actualizar el acudiente.", e);
        }
    }

    public boolean delete(int idParent) {
        String sql = "DELETE FROM acudientes WHERE id_acudiente = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idParent);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete guardian", e);
            throw new DataAccessException("No se pudo eliminar el acudiente.", e);
        }
    }

    public Parent findById(int idParent) {
        String sql = "SELECT " + COLUMNS + " FROM acudientes WHERE id_acudiente = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idParent);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find guardian", e);
            throw new DataAccessException("No se pudo consultar el acudiente.", e);
        }
    }

    public Parent findByDocument(String documentNumber) {
        String sql = "SELECT " + COLUMNS + " FROM acudientes WHERE numero_documento = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find guardian by document", e);
            throw new DataAccessException("No se pudo consultar el acudiente.", e);
        }
    }

    public List<Parent> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM acudientes ORDER BY primer_apellido, primer_nombre";
        List<Parent> parents = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                parents.add(map(rs));
            }
            return parents;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list guardians", e);
            throw new DataAccessException("No se pudieron listar los acudientes.", e);
        }
    }

    public List<Parent> findByStudent(int idStudent) {
        String sql = "SELECT a.id_acudiente, a.tipo_documento, a.numero_documento, "
                + "a.primer_nombre, a.segundo_nombre, a.primer_apellido, "
                + "a.segundo_apellido, a.parentesco, a.telefono_principal, "
                + "a.telefono_secundario, a.correo_electronico, a.direccion, "
                + "a.ocupacion, a.es_responsable_financiero, a.fecha_creacion, "
                + "a.fecha_actualizacion FROM acudientes a "
                + "INNER JOIN estudiante_acudiente ea ON ea.id_acudiente = a.id_acudiente "
                + "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC";
        List<Parent> parents = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    parents.add(map(rs));
                }
            }
            return parents;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list guardians of student", e);
            throw new DataAccessException("No se pudieron listar los acudientes del estudiante.", e);
        }
    }

    private void bind(PreparedStatement ps, Parent p) throws SQLException {
        ps.setString(1, p.getDocumentType());
        ps.setString(2, p.getDocumentNumber());
        ps.setString(3, p.getFirstName());
        ps.setString(4, p.getSecondName());
        ps.setString(5, p.getFirstLastName());
        ps.setString(6, p.getSecondLastName());
        ps.setString(7, p.getRelationship());
        ps.setString(8, p.getPhonePrimary());
        ps.setString(9, p.getPhoneSecondary());
        ps.setString(10, p.getEmail());
        ps.setString(11, p.getAddress());
        ps.setString(12, p.getOccupation());
        ps.setBoolean(13, p.isIsFinancialResponsible());
    }

    private Parent map(ResultSet rs) throws SQLException {
        Parent p = new Parent();
        p.setIdParent(rs.getInt("id_acudiente"));
        p.setDocumentType(rs.getString("tipo_documento"));
        p.setDocumentNumber(rs.getString("numero_documento"));
        p.setFirstName(rs.getString("primer_nombre"));
        p.setSecondName(rs.getString("segundo_nombre"));
        p.setFirstLastName(rs.getString("primer_apellido"));
        p.setSecondLastName(rs.getString("segundo_apellido"));
        p.setRelationship(rs.getString("parentesco"));
        p.setPhonePrimary(rs.getString("telefono_principal"));
        p.setPhoneSecondary(rs.getString("telefono_secundario"));
        p.setEmail(rs.getString("correo_electronico"));
        p.setAddress(rs.getString("direccion"));
        p.setOccupation(rs.getString("ocupacion"));
        p.setIsFinancialResponsible(rs.getBoolean("es_responsable_financiero"));
        p.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        p.setUpdateDate(DaoSupport.getTimestamp(rs, "fecha_actualizacion"));
        return p;
    }
}
