package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.util.ConnectionFactory;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for academic grades (grados).
 */
public class GradeDao {

    public List<Grade> findAll() {
        String sql = "SELECT id_grado, nombre_grado, nivel_numerico, fecha_creacion "
                + "FROM grados ORDER BY nivel_numerico";
        List<Grade> grades = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Grade grade = new Grade();
                grade.setIdGrade(rs.getInt("id_grado"));
                grade.setName(rs.getString("nombre_grado"));
                grade.setLevelNumber(rs.getInt("nivel_numerico"));
                grade.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
                grades.add(grade);
            }
            return grades;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list grades", e);
            throw new DataAccessException("No se pudieron listar los grados.", e);
        }
    }

    public Grade findById(int idGrade) {
        String sql = "SELECT id_grado, nombre_grado, nivel_numerico, fecha_creacion "
                + "FROM grados WHERE id_grado = ?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idGrade);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Grade grade = new Grade();
                grade.setIdGrade(rs.getInt("id_grado"));
                grade.setName(rs.getString("nombre_grado"));
                grade.setLevelNumber(rs.getInt("nivel_numerico"));
                grade.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
                return grade;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find grade", e);
            throw new DataAccessException("No se pudo consultar el grado.", e);
        }
    }
}
