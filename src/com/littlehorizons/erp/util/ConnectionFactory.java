package com.littlehorizons.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton connection factory for the ERP database.
 *
 * <p>The database name is always {@code erp_little_horizons}. Connection
 * settings may be overridden with the ERP_DB_URL, ERP_DB_USER and
 * ERP_DB_PASSWORD environment variables.</p>
 */
public final class ConnectionFactory {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/erp_little_horizons"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            + "&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "nathalia123";
    private static final String DEFAULT_PASSWORD = "1234";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final ConnectionFactory INSTANCE = new ConnectionFactory();

    private final String url;
    private final String user;
    private final String password;

    private ConnectionFactory() {
        this.url = envOrDefault("ERP_DB_URL", DEFAULT_URL);
        this.user = envOrDefault("ERP_DB_USER", DEFAULT_USER);
        this.password = envOrDefault("ERP_DB_PASSWORD", DEFAULT_PASSWORD);
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found: " + DRIVER, e);
        }
    }

    public static ConnectionFactory getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    /**
     * Indicates whether the built-in development credentials are still in use.
     * Production deployments must override them with environment variables.
     */
    public boolean isUsingDefaultCredentials() {
        return DEFAULT_USER.equals(user) && DEFAULT_PASSWORD.equals(password);
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
