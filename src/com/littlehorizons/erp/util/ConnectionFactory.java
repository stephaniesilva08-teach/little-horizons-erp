package com.littlehorizons.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Singleton connection factory for the ERP database.
 *
 * <p>The database name is always {@code erp_little_horizons}. Connection
 * settings are read from the local, git-ignored .env file, with environment
 * variables as fallback. Credentials are kept out of tracked source files.</p>
 */
public final class ConnectionFactory implements ConnectionProvider {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/erp_little_horizons"
            + "?sslMode=VERIFY_IDENTITY&serverTimezone=UTC"
            + "&characterEncoding=UTF-8";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final Properties LOCAL_CONFIG = loadLocalConfig();

    private final String url;
    private final String user;
    private final String password;

    private ConnectionFactory() {
        this.url = configOrDefault("ERP_DB_URL", DEFAULT_URL);
        this.user = requireConfig("ERP_DB_USER");
        this.password = requireConfig("ERP_DB_PASSWORD");
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
        if (isBlank(configValue("ERP_DB_USER"))) {
            missing.add("ERP_DB_USER");
        }
        if (isBlank(configValue("ERP_DB_PASSWORD"))) {
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

    public String getUser() {
        return user;
    }

    private static String requireConfig(String name) {
        String value = configValue(name);
        if (isBlank(value)) {
            throw new IllegalStateException(
                    "Set " + name + " in the local .env file or environment.");
        }
        return value;
    }

    private static String configOrDefault(String name, String fallback) {
        String value = configValue(name);
        return isBlank(value) ? fallback : value;
    }

    private static String configValue(String name) {
        String localValue = LOCAL_CONFIG.getProperty(name);
        if (!isBlank(localValue)) {
            return localValue.trim();
        }
        return System.getenv(name);
    }

    private static Properties loadLocalConfig() {
        Properties properties = new Properties();
        Path envFile = Path.of(".env");
        if (Files.isRegularFile(envFile)) {
            try (Reader reader = Files.newBufferedReader(envFile)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read local .env file.", e);
            }
        }
        return properties;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
