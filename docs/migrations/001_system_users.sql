-- Apply once to an existing erp_little_horizons database.
USE erp_little_horizons;

CREATE TABLE IF NOT EXISTS control_autenticacion (
    id_control TINYINT PRIMARY KEY,
    CHECK (id_control = 1)
) ENGINE=InnoDB COMMENT='Fila de bloqueo para cambios concurrentes de cuentas';

INSERT IGNORE INTO control_autenticacion (id_control) VALUES (1);

CREATE TABLE IF NOT EXISTS usuarios_sistema (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(40) NOT NULL UNIQUE,
    nombre_visible VARCHAR(100) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'SECRETARIA', 'DOCENTE', 'FINANZAS', 'BIBLIOTECA') NOT NULL,
    sal_contrasena BINARY(16) NOT NULL,
    hash_contrasena BINARY(32) NOT NULL,
    esta_activo BOOLEAN NOT NULL DEFAULT TRUE,
    intentos_fallidos TINYINT UNSIGNED NOT NULL DEFAULT 0,
    bloqueado_hasta DATETIME NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CHECK (intentos_fallidos <= 4),
    INDEX idx_usuario_activo (nombre_usuario, esta_activo)
) ENGINE=InnoDB COMMENT='Cuentas locales y permisos de la aplicación de consola';
