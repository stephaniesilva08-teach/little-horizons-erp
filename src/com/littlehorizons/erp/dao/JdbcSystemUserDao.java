package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AccessRole;
import com.littlehorizons.erp.model.SessionUser;
import com.littlehorizons.erp.util.ConnectionProvider;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation for local console accounts. */
public final class JdbcSystemUserDao implements SystemUserDao {

    private final ConnectionProvider connectionProvider;

    public JdbcSystemUserDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public long countUsers() {
        String sql = "SELECT COUNT(*) FROM usuarios_sistema";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not count system users", e);
            throw new DataAccessException("No se pudo consultar la configuración de usuarios. "
                    + "Aplique la migración de autenticación indicada en el README.", e);
        }
    }

    @Override
    public UserCredential findCredential(String username) {
        String sql = "SELECT id_usuario, nombre_usuario, nombre_visible, rol, esta_activo, "
                + "sal_contrasena, hash_contrasena, "
                + "(bloqueado_hasta > CURRENT_TIMESTAMP) AS esta_bloqueado "
                + "FROM usuarios_sistema WHERE nombre_usuario = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserCredential(rs.getInt("id_usuario"),
                        rs.getString("nombre_usuario"), rs.getString("nombre_visible"),
                        rs.getString("rol"), rs.getBoolean("esta_activo"),
                        rs.getBytes("sal_contrasena"), rs.getBytes("hash_contrasena"),
                        rs.getBoolean("esta_bloqueado"));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not load system user credentials", e);
            throw new DataAccessException("No se pudo validar el acceso.", e);
        }
    }

    @Override
    public int createFirstAdministrator(String username, String displayName,
                                        byte[] salt, byte[] passwordHash) {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockAuthenticationGuard(connection);
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT COUNT(*) FROM usuarios_sistema");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getLong(1) != 0) {
                        throw new IllegalStateException(
                                "La configuración inicial ya fue completada.");
                    }
                }
                int id = insertUser(connection, username, displayName,
                        AccessRole.ADMINISTRATOR, salt, passwordHash);
                connection.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not bootstrap administrator", e);
            throw new DataAccessException("No se pudo crear el administrador inicial.", e);
        }
    }

    @Override
    public int create(String username, String displayName, AccessRole role,
                      byte[] salt, byte[] passwordHash) {
        try (Connection connection = connectionProvider.getConnection()) {
            return insertUser(connection, username, displayName, role, salt, passwordHash);
        } catch (SQLException e) {
            LoggerUtil.error("Could not create system user", e);
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("Ese nombre de usuario ya existe.");
            }
            throw new DataAccessException("No se pudo crear la cuenta.", e);
        }
    }

    @Override
    public List<SessionUser> findAll() {
        String sql = "SELECT id_usuario, nombre_usuario, nombre_visible, rol, esta_activo "
                + "FROM usuarios_sistema ORDER BY nombre_usuario";
        List<SessionUser> users = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new SessionUser(rs.getInt("id_usuario"),
                        rs.getString("nombre_usuario"), rs.getString("nombre_visible"),
                        AccessRole.fromDatabaseValue(rs.getString("rol")),
                        rs.getBoolean("esta_activo")));
            }
            return users;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list system users", e);
            throw new DataAccessException("No se pudieron listar las cuentas.", e);
        }
    }

    @Override
    public void resetPassword(int idUser, byte[] salt, byte[] passwordHash) {
        String sql = "UPDATE usuarios_sistema SET sal_contrasena = ?, hash_contrasena = ?, "
                + "intentos_fallidos = 0, bloqueado_hasta = NULL "
                + "WHERE id_usuario = ? AND esta_activo = TRUE";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBytes(1, salt);
            ps.setBytes(2, passwordHash);
            ps.setInt(3, idUser);
            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("La cuenta no existe o está inactiva.");
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not reset system user password", e);
            throw new DataAccessException("No se pudo restablecer la contraseña.", e);
        }
    }

    @Override
    public void activate(int idUser) {
        String sql = "UPDATE usuarios_sistema SET esta_activo = TRUE, "
                + "intentos_fallidos = 0, bloqueado_hasta = NULL "
                + "WHERE id_usuario = ? AND esta_activo = FALSE";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("La cuenta no existe o ya está activa.");
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not reactivate system user", e);
            throw new DataAccessException("No se pudo reactivar la cuenta.", e);
        }
    }

    @Override
    public boolean registerFailedAttempt(int idUser) {
        String sql = "UPDATE usuarios_sistema SET bloqueado_hasta = CASE "
                + "WHEN intentos_fallidos >= 4 THEN DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 15 MINUTE) "
                + "ELSE bloqueado_hasta END, intentos_fallidos = CASE "
                + "WHEN intentos_fallidos >= 4 THEN 0 ELSE intentos_fallidos + 1 END "
                + "WHERE id_usuario = ? AND esta_activo = TRUE "
                + "AND (bloqueado_hasta IS NULL OR bloqueado_hasta <= CURRENT_TIMESTAMP)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LoggerUtil.error("Could not record failed login", e);
            throw new DataAccessException("No se pudo registrar el intento de acceso.", e);
        }
    }

    @Override
    public void resetFailedAttempts(int idUser) {
        String sql = "UPDATE usuarios_sistema SET intentos_fallidos = 0, "
                + "bloqueado_hasta = NULL WHERE id_usuario = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Could not reset failed login attempts", e);
            throw new DataAccessException("No se pudo completar el inicio de sesión.", e);
        }
    }

    @Override
    public void deactivate(int idUser, int currentUserId) {
        if (idUser == currentUserId) {
            throw new IllegalArgumentException("No puede desactivar su propia cuenta.");
        }
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockAuthenticationGuard(connection);
                String role = null;
                boolean active = false;
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT rol, esta_activo FROM usuarios_sistema WHERE id_usuario = ? FOR UPDATE")) {
                    ps.setInt(1, idUser);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            role = rs.getString("rol");
                            active = rs.getBoolean("esta_activo");
                        }
                    }
                }
                if (role == null || !active) {
                    throw new IllegalArgumentException("La cuenta no existe o ya está inactiva.");
                }
                if (AccessRole.ADMINISTRATOR.databaseValue().equals(role)) {
                    try (PreparedStatement ps = connection.prepareStatement(
                            "SELECT COUNT(*) FROM usuarios_sistema WHERE rol = 'ADMINISTRADOR' "
                            + "AND esta_activo = TRUE");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) <= 1) {
                            throw new IllegalArgumentException(
                                    "Debe quedar al menos un administrador activo.");
                        }
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE usuarios_sistema SET esta_activo = FALSE "
                        + "WHERE id_usuario = ? AND esta_activo = TRUE")) {
                    ps.setInt(1, idUser);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalArgumentException("La cuenta ya no está activa.");
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not deactivate system user", e);
            throw new DataAccessException("No se pudo desactivar la cuenta.", e);
        }
    }

    private void lockAuthenticationGuard(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id_control FROM control_autenticacion WHERE id_control = 1 FOR UPDATE");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Authentication guard row is missing.");
            }
        }
    }

    private int insertUser(Connection connection, String username, String displayName,
                           AccessRole role, byte[] salt, byte[] passwordHash)
            throws SQLException {
        String sql = "INSERT INTO usuarios_sistema (nombre_usuario, nombre_visible, rol, "
                + "sal_contrasena, hash_contrasena) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, displayName);
            ps.setString(3, role.databaseValue());
            ps.setBytes(4, salt);
            ps.setBytes(5, passwordHash);
            ps.executeUpdate();
            return DaoSupport.generatedKey(ps);
        }
    }
}
