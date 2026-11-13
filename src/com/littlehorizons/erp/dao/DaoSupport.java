package com.littlehorizons.erp.dao;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Small JDBC helpers shared by every DAO.
 */
public final class DaoSupport {

    private DaoSupport() {
    }

    public static int getInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? 0 : value;
    }

    public static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    public static BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        return rs.getBigDecimal(column);
    }

    public static boolean getBoolean(ResultSet rs, String column) throws SQLException {
        return rs.getBoolean(column);
    }

    public static String getString(ResultSet rs, String column) throws SQLException {
        return rs.getString(column);
    }

    public static String getDate(ResultSet rs, String column) throws SQLException {
        Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate().toString();
    }

    public static String getTimestamp(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime().toString();
    }

    public static void setDate(PreparedStatement ps, int index, String date)
            throws SQLException {
        if (date == null || date.isBlank()) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    public static int generatedKey(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            return keys.next() ? keys.getInt(1) : 0;
        }
    }

    public static void requireUpdated(int rows, String entity) {
        if (rows == 0) {
            throw new IllegalArgumentException("No se encontró " + entity
                    + " para actualizar.");
        }
    }
}
