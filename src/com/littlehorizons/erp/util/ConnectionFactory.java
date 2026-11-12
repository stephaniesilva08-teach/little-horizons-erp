package com.littlehorizons.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton connection factory for the ERP database.
 *
 * <p>The database name is always {@code erp_little_horizons}. Connection
 * settings are read from the ERP_DB_URL, ERP_DB_USER and ERP_DB_PASSWORD
 * environment variables. User and password are mandatory: the application
 * must never ship with embedded database credentials.</p>
 */
public final class ConnectionFactory implements ConnectionProvider {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/erp_little_horizons"
            + "?sslMode=PREFERRED&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            + "&characterEncoding=UTF-8";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String url;
    private final String user;
    private final String password;

    private ConnectionFactory() {
        this.url = envOrDefault("ERP_DB_URL", DEFAULT_URL);
        this.user = requireEnv("ERP_DB_USER");
        this.password = requireEnv("ERP_DB_PASSWORD");
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found: " + DRIVER, e);
        }
    }

    /**
     * Lazy holder so configuration can be validated before the singleton is
     * created.
     */
    private static final class Holder {
        private static final ConnectionFactory INSTANCE = new ConnectionFactory();
    }

    public static ConnectionFactory getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Returns a user-facing message when the mandatory credentials are missing,
     * or {@code null} when the configuration is complete. This method does not
     * initialize the singleton, so it is safe to call at startup.
     */
    public static String missingConfiguration() {
        List<String> missing = new ArrayList<>();
        if (isBlank(System.getenv("ERP_DB_USER"))) {
            missing.add("ERP_DB_USER");
        }
        if (isBlank(System.getenv("ERP_DB_PASSWORD"))) {
            missing.add("ERP_DB_PASSWORD");
        }
        if (missing.isEmpty()) {
            return null;
        }
        return "Faltan variables de entorno obligatorias: "
                + String.join(", ", missing) + ".";
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

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (isBlank(value)) {
            throw new IllegalStateException(
                    "Environment variable " + name + " is required.");
        }
        return value;
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return isBlank(value) ? fallback : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
