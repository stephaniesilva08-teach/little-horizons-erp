package com.littlehorizons.erp.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Supplies database connections to the persistence layer.
 *
 * <p>DAOs depend on this abstraction instead of a concrete factory, so tests
 * can provide lightweight fakes and the application can switch between direct
 * connections and a pool without touching the DAOs.</p>
 */
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;
}
