-- Anexo: Script de Base de Datos

-- A continuación se presenta el script completo DDL de MySQL para crear todas las tablas necesarias. 
-- Este script debe ejecutarse en la base de datos `erp_little_horizons`.
-- Si encuentra detalles que modificar o agregar, puede editar el script siempre y cuando pueda justificar sus cambios.


-- ============================================
-- Little Horizons ERP - Esquema de Base de Datos
-- Sistema de Gestión Educativa
-- Versión: 2.0 (Corregida y Mejorada)
-- Motor: MySQL 8.0 o superior (REQUERIDO)
-- ============================================

DROP DATABASE IF EXISTS erp_little_horizons;
CREATE DATABASE erp_little_horizons CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_little_horizons;

-- ============================================
-- 1. MÓDULO DE ESTUDIANTES Y ACUDIENTES
-- ============================================

-- Tabla de grados académicos
CREATE TABLE grados (
    id_grado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_grado VARCHAR(50) NOT NULL UNIQUE COMMENT 'Ej: Preescolar, Primero, Segundo, ... Once',
    nivel_numerico INT NOT NULL UNIQUE COMMENT 'Nivel numérico para ordenamiento (0 para preescolar, 1-11 para grados)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Catálogo de grados académicos del colegio';

-- Tabla de estudiantes
CREATE TABLE estudiantes (
    id_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('TI', 'CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'TI',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    genero ENUM('M', 'F', 'Otro') NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    correo_electronico VARCHAR(100),
    tipo_sangre VARCHAR(5),
    alergias TEXT,
    condiciones_medicas TEXT,
    fecha_ingreso DATE NOT NULL,
    estado ENUM('Activo', 'Graduado', 'Retirado', 'Suspendido') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido, primer_nombre)
) ENGINE=InnoDB COMMENT='Información completa de estudiantes';

-- Tabla de acudientes/tutores legales
CREATE TABLE acudientes (
    id_acudiente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    parentesco VARCHAR(50) NOT NULL COMMENT 'Padre, Madre, Abuelo, Tío, Tutor Legal, etc.',
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    correo_electronico VARCHAR(100),
    direccion VARCHAR(255),
    ocupacion VARCHAR(100),
    es_responsable_financiero BOOLEAN DEFAULT TRUE COMMENT 'Indica si es responsable de pagos',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Acudientes y tutores legales';

-- Relación estudiantes-acudientes (muchos a muchos)
CREATE TABLE estudiante_acudiente (
    id_estudiante INT NOT NULL,
    id_acudiente INT NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE COMMENT 'Acudiente principal para comunicaciones',
    es_contacto_emergencia BOOLEAN DEFAULT TRUE COMMENT 'Disponible para emergencias',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_estudiante, id_acudiente),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_acudiente (id_acudiente)
) ENGINE=InnoDB COMMENT='Relación entre estudiantes y sus acudientes';

-- Tabla de matrículas (historial de grados por año)
CREATE TABLE matriculas (
    id_matricula INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_grado INT NOT NULL,
    año_academico INT NOT NULL COMMENT 'Año académico (ej: 2024)',
    estado_matricula ENUM('Activa', 'Completada', 'Retirada') DEFAULT 'Activa',
    fecha_matricula DATE NOT NULL,
    fecha_retiro DATE,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    UNIQUE KEY matricula_unica (id_estudiante, año_academico),
    INDEX idx_año_grado (año_academico, id_grado),
    INDEX idx_estado (estado_matricula)
) ENGINE=InnoDB COMMENT='Historial de matrículas de estudiantes por año y grado';

-- ============================================
-- 2. MÓDULO ACADÉMICO
-- ============================================

-- Tabla de materias/asignaturas
CREATE TABLE materias (
    id_materia INT AUTO_INCREMENT PRIMARY KEY,
    codigo_materia VARCHAR(20) NOT NULL UNIQUE COMMENT 'Código único de la materia (ej: MAT-5, ESP-3)',
    nombre_materia VARCHAR(100) NOT NULL,
    descripcion TEXT,
    id_grado INT NOT NULL COMMENT 'Grado al que pertenece la materia',
    horas_semanales INT DEFAULT 2 COMMENT 'Horas semanales asignadas',
    esta_activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    INDEX idx_grado (id_grado),
    INDEX idx_activa (esta_activa),
    INDEX idx_codigo (codigo_materia)
) ENGINE=InnoDB COMMENT='Catálogo de materias por grado';

-- Tabla de profesores
CREATE TABLE profesores (
    id_profesor INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    especializacion VARCHAR(100) COMMENT 'Área de especialización (Matemáticas, Español, etc.)',
    fecha_contratacion DATE NOT NULL,
    estado ENUM('Activo', 'Inactivo', 'Licencia') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Información de profesores';

-- Asignación de materias a profesores
CREATE TABLE asignaciones_profesor (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_profesor INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    seccion VARCHAR(10) COMMENT 'Sección o grupo (A, B, C)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY asignacion_unica (id_profesor, id_materia, año_academico, seccion),
    INDEX idx_profesor_año (id_profesor, año_academico),
    INDEX idx_materia_año (id_materia, año_academico)
) ENGINE=InnoDB COMMENT='Asignación de profesores a materias por año académico';

-- Horarios de clase por asignación (día y franja horaria)
CREATE TABLE horarios (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_asignacion INT NOT NULL,
    dia_semana ENUM('Lunes','Martes','Miércoles','Jueves','Viernes','Sábado') NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_asignacion) REFERENCES asignaciones_profesor(id_asignacion) ON DELETE CASCADE,
    CHECK (hora_fin > hora_inicio),
    INDEX idx_asignacion (id_asignacion),
    INDEX idx_dia_hora (dia_semana, hora_inicio, hora_fin)
) ENGINE=InnoDB COMMENT='Horarios semanales de las asignaciones profesor-materia';

-- Inscripción de estudiantes en materias
CREATE TABLE inscripciones_materia (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV') NOT NULL COMMENT 'Período académico',
    seccion VARCHAR(10) COMMENT 'Sección o grupo asignado',
    fecha_inscripcion DATE NOT NULL,
    estado ENUM('Activa', 'Retirada', 'Completada') DEFAULT 'Activa',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY inscripcion_unica (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico)
) ENGINE=InnoDB COMMENT='Inscripción de estudiantes en materias específicas';

-- Tabla de registros académicos (calificaciones/notas)
CREATE TABLE registros_academicos (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    id_profesor INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Final') NOT NULL,
    valor_nota DECIMAL(2,1) NOT NULL COMMENT 'Nota de 0.0 a 5.0 según estándar colombiano',
    observaciones TEXT,
    fecha_registro DATE NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor),
    CHECK (valor_nota >= 0.0 AND valor_nota <= 5.0),
    UNIQUE KEY registro_unico (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico),
    INDEX idx_valor_nota (valor_nota)
) ENGINE=InnoDB COMMENT='Registro de calificaciones de estudiantes';

-- Tabla de asistencia
CREATE TABLE asistencias (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    fecha_asistencia DATE NOT NULL,
    estado_asistencia ENUM('Presente', 'Ausente', 'Tardanza', 'Excusa Médica') NOT NULL,
    observaciones TEXT,
    registrado_por INT COMMENT 'ID del profesor que registró la asistencia',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (registrado_por) REFERENCES profesores(id_profesor),
    UNIQUE KEY asistencia_unica (id_estudiante, id_materia, fecha_asistencia),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_asistencia),
    INDEX idx_materia_fecha (id_materia, fecha_asistencia),
    INDEX idx_estado (estado_asistencia)
) ENGINE=InnoDB COMMENT='Registro de asistencia diaria de estudiantes';

-- ============================================
-- 3. MÓDULO DE ACTIVIDADES EXTRACURRICULARES
-- ============================================

-- Tabla de clubes
CREATE TABLE clubes (
    id_club INT AUTO_INCREMENT PRIMARY KEY,
    nombre_club VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_profesor_supervisor INT COMMENT 'Profesor encargado del club',
    capacidad_maxima INT DEFAULT 30 COMMENT 'Cupo máximo de estudiantes',
    CHECK (capacidad_maxima > 0),
    horario_reunion VARCHAR(100) COMMENT 'Días y horario de reunión (ej: Martes 3-5 PM)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor_supervisor) REFERENCES profesores(id_profesor),
    INDEX idx_activo (esta_activo),
    INDEX idx_nombre (nombre_club)
) ENGINE=InnoDB COMMENT='Catálogo de clubes extracurriculares';

-- Membresías en clubes
CREATE TABLE membresias_club (
    id_membresia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_club INT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    estado_membresia ENUM('Activa', 'Retirada') DEFAULT 'Activa',
    fecha_retiro DATE,
    motivo_retiro TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_club) REFERENCES clubes(id_club) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_club_estado (id_club, estado_membresia),
    UNIQUE KEY membresia_unica (id_estudiante, id_club)
) ENGINE=InnoDB COMMENT='Membresías de estudiantes en clubes';

-- Tabla de eventos
CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_evento VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_evento ENUM('Académico', 'Cultural', 'Deportivo', 'Social', 'Científico') NOT NULL,
    fecha_evento DATE NOT NULL,
    lugar VARCHAR(150),
    tiene_competencia BOOLEAN DEFAULT FALSE COMMENT 'Indica si el evento tiene competencia con posiciones',
    id_organizador INT COMMENT 'Profesor organizador del evento',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_organizador) REFERENCES profesores(id_profesor),
    INDEX idx_fecha (fecha_evento),
    INDEX idx_tipo (tipo_evento),
    INDEX idx_activo (esta_activo)
) ENGINE=InnoDB COMMENT='Catálogo de eventos del colegio';

-- Participantes en eventos
CREATE TABLE participantes_evento (
    id_participacion INT AUTO_INCREMENT PRIMARY KEY,
    id_evento INT NOT NULL,
    id_estudiante INT NOT NULL,
    rol_participacion ENUM('Participante', 'Competidor', 'Organizador', 'Asistente') DEFAULT 'Participante',
    posicion_obtenida VARCHAR(20) COMMENT 'Posición obtenida si hay competencia (1er Lugar, 2do Lugar, 3er Lugar, Participante)',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    UNIQUE KEY participacion_unica (id_evento, id_estudiante),
    INDEX idx_evento (id_evento),
    INDEX idx_estudiante (id_estudiante)
) ENGINE=InnoDB COMMENT='Registro de participación de estudiantes en eventos';

-- ============================================
-- 4. MÓDULO FINANCIERO
-- ============================================

-- Conceptos de pago (catálogo)
CREATE TABLE conceptos_pago (
    id_concepto INT AUTO_INCREMENT PRIMARY KEY,
    codigo_concepto VARCHAR(20) NOT NULL UNIQUE,
    nombre_concepto VARCHAR(100) NOT NULL,
    descripcion TEXT,
    monto_base DECIMAL(10,2) NOT NULL COMMENT 'Valor base del concepto en pesos colombianos',
    CHECK (monto_base >= 0),
    es_recurrente BOOLEAN DEFAULT FALSE COMMENT 'Indica si es un pago recurrente (ej: pensión mensual)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activo (esta_activo),
    INDEX idx_codigo (codigo_concepto)
) ENGINE=InnoDB COMMENT='Catálogo de conceptos de pago del colegio';

-- Valores personalizados por estudiante (becas, descuentos por hermanos, etc.)
CREATE TABLE conceptos_estudiante (
    id_concepto_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_concepto INT NOT NULL,
    monto_personalizado DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(150),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto) ON DELETE CASCADE,
    CHECK (monto_personalizado >= 0),
    UNIQUE KEY concepto_estudiante_unico (id_estudiante, id_concepto)
) ENGINE=InnoDB COMMENT='Ajustes de valor de conceptos para estudiantes específicos';

-- Tabla de pagos
CREATE TABLE pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_acudiente INT COMMENT 'Acudiente que realizó el pago',
    id_concepto INT NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL,
    CHECK (monto_pagado >= 0),
    fecha_pago DATE NOT NULL,
    metodo_pago ENUM('Efectivo', 'Transferencia', 'Tarjeta Débito', 'Tarjeta Crédito', 'Cheque') NOT NULL,
    numero_recibo VARCHAR(50) UNIQUE,
    año_academico INT COMMENT 'Año al que aplica el pago',
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Anual') COMMENT 'Período al que aplica el pago',
    notas TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE RESTRICT,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente),
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_pago),
    INDEX idx_concepto (id_concepto),
    INDEX idx_fecha (fecha_pago),
    INDEX idx_recibo (numero_recibo)
) ENGINE=InnoDB COMMENT='Registro de todos los pagos realizados';

-- Cargos u obligaciones generadas para cada estudiante (base para cartera y morosos)
CREATE TABLE cargos_estudiante (
    id_cargo INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_concepto INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I','II','III','IV','Anual') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado ENUM('Pendiente','Pagado','Anulado') DEFAULT 'Pendiente',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE RESTRICT,
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto),
    CHECK (monto >= 0),
    INDEX idx_estudiante_estado (id_estudiante, estado),
    INDEX idx_vencimiento (fecha_vencimiento)
) ENGINE=InnoDB COMMENT='Cargos u obligaciones de pago por estudiante';

-- Tabla de facturas electrónicas
CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) NOT NULL UNIQUE COMMENT 'Número consecutivo de factura',
    id_pago INT NOT NULL COMMENT 'Pago asociado a esta factura',
    fecha_emision DATE NOT NULL,
    cufe VARCHAR(100) COMMENT 'Código Único de Factura Electrónica (simulado)',
    nombre_archivo_txt VARCHAR(255) COMMENT 'Nombre del archivo TXT generado',
    ruta_archivo VARCHAR(500) COMMENT 'Ruta completa donde se guardó el archivo TXT',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pago) REFERENCES pagos(id_pago) ON DELETE RESTRICT,
    UNIQUE KEY factura_pago_unica (id_pago),
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_fecha_emision (fecha_emision),
    INDEX idx_pago (id_pago)
) ENGINE=InnoDB COMMENT='Registro de facturas electrónicas generadas';

-- Secuencias consecutivas por año para recibos y facturas
CREATE TABLE secuencias_financieras (
    nombre_secuencia VARCHAR(20) NOT NULL COMMENT 'recibo o factura',
    año INT NOT NULL,
    ultimo_valor INT NOT NULL DEFAULT 0,
    PRIMARY KEY (nombre_secuencia, año),
    CHECK (ultimo_valor >= 0)
) ENGINE=InnoDB COMMENT='Consecutivos seguros para recibos y facturas';

-- ============================================
-- 5. MÓDULO DE GESTIÓN DE RECURSOS
-- ============================================

-- Categorías de recursos
CREATE TABLE categorias_recurso (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Categorías de recursos (Libro, Equipo de Laboratorio, etc.)';

-- Tabla de recursos del colegio
CREATE TABLE recursos (
    id_recurso INT AUTO_INCREMENT PRIMARY KEY,
    codigo_recurso VARCHAR(50) NOT NULL UNIQUE COMMENT 'Código de inventario único',
    nombre_recurso VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    descripcion TEXT,
    fecha_adquisicion DATE,
    ubicacion_fisica VARCHAR(100) COMMENT 'Ubicación física (Biblioteca, Laboratorio 1, etc.)',
    estado_recurso ENUM('Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja') DEFAULT 'Disponible',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES categorias_recurso(id_categoria),
    INDEX idx_codigo (codigo_recurso),
    INDEX idx_estado (estado_recurso),
    INDEX idx_categoria (id_categoria),
    INDEX idx_nombre (nombre_recurso)
) ENGINE=InnoDB COMMENT='Inventario de recursos prestables del colegio';

-- Tabla de préstamos de recursos a ESTUDIANTES
CREATE TABLE prestamos_estudiante (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_estudiante INT NOT NULL COMMENT 'Estudiante que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a estudiantes';

-- Tabla de préstamos de recursos a PROFESORES
CREATE TABLE prestamos_profesor (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_profesor INT NOT NULL COMMENT 'Profesor que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_profesor (id_profesor),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a profesores';

-- ============================================
-- DML: coherent sample data
-- Minimum 30 rows in each main table:
-- estudiantes, acudientes, profesores, materias,
-- registros_academicos, pagos, clubes and recursos.
-- ============================================

INSERT INTO grados (id_grado, nombre_grado, nivel_numerico) VALUES
(1, 'Preescolar', 0),
(2, 'Primero', 1),
(3, 'Segundo', 2),
(4, 'Tercero', 3),
(5, 'Cuarto', 4),
(6, 'Quinto', 5),
(7, 'Sexto', 6),
(8, 'Séptimo', 7),
(9, 'Octavo', 8),
(10, 'Noveno', 9),
(11, 'Décimo', 10),
(12, 'Once', 11);

INSERT INTO categorias_recurso (id_categoria, nombre_categoria, descripcion) VALUES
(1, 'Libro', 'Libros de la biblioteca escolar'),
(2, 'Equipo de Laboratorio', 'Equipos científicos y de laboratorio'),
(3, 'Equipo Deportivo', 'Equipos y materiales deportivos'),
(4, 'Dispositivo Tecnológico', 'Tablets, calculadoras y portátiles'),
(5, 'Material Didáctico', 'Materiales educativos diversos');

INSERT INTO conceptos_pago (id_concepto, codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente) VALUES
(1, 'MAT', 'Matrícula', 'Pago anual de matrícula', 500000, FALSE),
(2, 'PENSION', 'Pensión Mensual', 'Pago mensual de pensión', 350000, TRUE),
(3, 'UNIFORME', 'Uniforme', 'Uniforme escolar completo', 250000, FALSE),
(4, 'TRANSPORTE', 'Transporte Escolar', 'Servicio de transporte mensual', 150000, TRUE),
(5, 'ALIMENTACION', 'Servicio de Alimentación', 'Almuerzo escolar mensual', 120000, TRUE),
(6, 'ACTIVIDAD', 'Actividad Especial', 'Salida pedagógica o actividad especial', 50000, FALSE),
(7, 'CERTIFICADO', 'Certificado', 'Certificado de estudio o constancia', 15000, FALSE);

INSERT INTO estudiantes (id_estudiante, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) VALUES
(1, 'TI', '100000001', 'Sofía', 'Valentina', 'García', 'Rodríguez', '2009-02-02', 'F', 'Calle 11 # 21-31, Bogotá', '3001000001', 'student1@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(2, 'TI', '100000002', 'Mateo', 'Andrés', 'Rodríguez', 'Martínez', '2010-03-03', 'M', 'Calle 12 # 22-32, Bogotá', '3001000002', 'student2@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(3, 'TI', '100000003', 'Valentina', '', 'Martínez', 'López', '2011-04-04', 'F', 'Calle 13 # 23-33, Bogotá', '3001000003', 'student3@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(4, 'TI', '100000004', 'Santiago', 'Felipe', 'López', 'Gómez', '2012-05-05', 'M', 'Calle 14 # 24-34, Bogotá', '3001000004', 'student4@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(5, 'TI', '100000005', 'Isabella', '', 'Gómez', 'Díaz', '2013-06-06', 'F', 'Calle 15 # 25-35, Bogotá', '3001000005', 'student5@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(6, 'TI', '100000006', 'Sebastián', 'David', 'Díaz', 'Pérez', '2014-07-07', 'M', 'Calle 16 # 26-36, Bogotá', '3001000006', 'student6@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(7, 'TI', '100000007', 'Camila', 'Andrea', 'Pérez', 'Sánchez', '2015-08-08', 'F', 'Calle 17 # 27-37, Bogotá', '3001000007', 'student7@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(8, 'TI', '100000008', 'Alejandro', 'José', 'Sánchez', 'Ramírez', '2016-09-09', 'M', 'Calle 18 # 28-38, Bogotá', '3001000008', 'student8@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(9, 'TI', '100000009', 'Mariana', '', 'Ramírez', 'Torres', '2017-10-10', 'F', 'Calle 19 # 29-39, Bogotá', '3001000009', 'student9@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(10, 'TI', '100000010', 'Daniel', 'Esteban', 'Torres', 'Flores', '2008-11-11', 'M', 'Calle 20 # 30-40, Bogotá', '3001000010', 'student10@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(11, 'TI', '100000011', 'Lucía', '', 'Flores', 'Rivera', '2009-12-12', 'F', 'Calle 21 # 31-41, Bogotá', '3001000011', 'student11@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(12, 'TI', '100000012', 'Nicolás', 'Alejandro', 'Rivera', 'Vargas', '2010-01-13', 'M', 'Calle 22 # 32-42, Bogotá', '3001000012', 'student12@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(13, 'TI', '100000013', 'Gabriela', '', 'Vargas', 'Castro', '2011-02-14', 'F', 'Calle 23 # 33-43, Bogotá', '3001000013', 'student13@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(14, 'TI', '100000014', 'Samuel', 'David', 'Castro', 'Ortiz', '2012-03-15', 'M', 'Calle 24 # 34-44, Bogotá', '3001000014', 'student14@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(15, 'TI', '100000015', 'Emilia', '', 'Ortiz', 'Jiménez', '2013-04-16', 'F', 'Calle 25 # 35-45, Bogotá', '3001000015', 'student15@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(16, 'TI', '100000016', 'Tomás', 'Ignacio', 'Jiménez', 'Moreno', '2014-05-17', 'M', 'Calle 26 # 36-46, Bogotá', '3001000016', 'student16@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(17, 'TI', '100000017', 'Antonella', '', 'Moreno', 'Muñoz', '2015-06-18', 'F', 'Calle 27 # 37-47, Bogotá', '3001000017', 'student17@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(18, 'TI', '100000018', 'Julián', 'Camilo', 'Muñoz', 'Rojas', '2016-07-19', 'M', 'Calle 28 # 38-48, Bogotá', '3001000018', 'student18@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(19, 'TI', '100000019', 'Renata', '', 'Rojas', 'Herrera', '2017-08-20', 'F', 'Calle 29 # 39-49, Bogotá', '3001000019', 'student19@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(20, 'TI', '100000020', 'Andrés', 'Mauricio', 'Herrera', 'Medina', '2008-09-21', 'M', 'Calle 30 # 40-50, Bogotá', '3001000020', 'student20@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(21, 'TI', '100000021', 'Sara', '', 'Medina', 'Aguilar', '2009-10-22', 'F', 'Calle 31 # 41-51, Bogotá', '3001000021', 'student21@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(22, 'TI', '100000022', 'Felipe', 'Andrés', 'Aguilar', 'Silva', '2010-11-23', 'M', 'Calle 32 # 42-52, Bogotá', '3001000022', 'student22@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(23, 'TI', '100000023', 'Salomé', '', 'Silva', 'Navarro', '2011-12-24', 'F', 'Calle 33 # 43-53, Bogotá', '3001000023', 'student23@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(24, 'TI', '100000024', 'Diego', 'Alejandro', 'Navarro', 'Molina', '2012-01-25', 'M', 'Calle 34 # 44-54, Bogotá', '3001000024', 'student24@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(25, 'TI', '100000025', 'Luciana', '', 'Molina', 'Suárez', '2013-02-26', 'F', 'Calle 35 # 45-55, Bogotá', '3001000025', 'student25@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(26, 'TI', '100000026', 'Martín', 'Emilio', 'Suárez', 'Ortega', '2014-03-27', 'M', 'Calle 36 # 46-56, Bogotá', '3001000026', 'student26@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(27, 'TI', '100000027', 'Ximena', '', 'Ortega', 'Delgado', '2015-04-01', 'F', 'Calle 37 # 47-57, Bogotá', '3001000027', 'student27@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(28, 'TI', '100000028', 'Emmanuel', 'David', 'Delgado', 'Reyes', '2016-05-02', 'M', 'Calle 38 # 48-58, Bogotá', '3001000028', 'student28@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(29, 'TI', '100000029', 'Paula', 'Andrea', 'Reyes', 'Cruz', '2017-06-03', 'F', 'Calle 39 # 49-59, Bogotá', '3001000029', 'student29@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo'),
(30, 'TI', '100000030', 'Joaquín', 'Esteban', 'Cruz', 'García', '2008-07-04', 'M', 'Calle 40 # 50-60, Bogotá', '3001000030', 'student30@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2024-01-15', 'Activo');

INSERT INTO acudientes (id_acudiente, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, correo_electronico, direccion, ocupacion, es_responsable_financiero) VALUES
(1, 'CC', '520000001', 'Carlos', 'Andrés', 'García', 'López', 'Padre', '3102000001', '6013000001', 'parent1@mail.com', 'Carrera 6 # 11-21, Bogotá', 'Docente', TRUE),
(2, 'CC', '520000002', 'María', 'Fernanda', 'Rodríguez', 'Gómez', 'Madre', '3102000002', '6013000002', 'parent2@mail.com', 'Carrera 7 # 12-22, Bogotá', 'Médico', TRUE),
(3, 'CC', '520000003', 'Luis', 'Alberto', 'Martínez', 'Díaz', 'Padre', '3102000003', '6013000003', 'parent3@mail.com', 'Carrera 8 # 13-23, Bogotá', 'Comerciante', TRUE),
(4, 'CC', '520000004', 'Ana', 'Lucía', 'López', 'Pérez', 'Madre', '3102000004', '6013000004', 'parent4@mail.com', 'Carrera 9 # 14-24, Bogotá', 'Abogado', FALSE),
(5, 'CC', '520000005', 'Jorge', 'Enrique', 'Gómez', 'Sánchez', 'Padre', '3102000005', '6013000005', 'parent5@mail.com', 'Carrera 10 # 15-25, Bogotá', 'Contador', TRUE),
(6, 'CC', '520000006', 'Claudia', 'Patricia', 'Díaz', 'Ramírez', 'Madre', '3102000006', '6013000006', 'parent6@mail.com', 'Carrera 11 # 16-26, Bogotá', 'Ingeniero', TRUE),
(7, 'CC', '520000007', 'Ricardo', 'Alfonso', 'Pérez', 'Torres', 'Padre', '3102000007', '6013000007', 'parent7@mail.com', 'Carrera 12 # 17-27, Bogotá', 'Docente', TRUE),
(8, 'CC', '520000008', 'Sandra', 'Milena', 'Sánchez', 'Flores', 'Madre', '3102000008', '6013000008', 'parent8@mail.com', 'Carrera 13 # 18-28, Bogotá', 'Médico', FALSE),
(9, 'CC', '520000009', 'Fernando', 'José', 'Ramírez', 'Rivera', 'Padre', '3102000009', '6013000009', 'parent9@mail.com', 'Carrera 14 # 19-29, Bogotá', 'Comerciante', TRUE),
(10, 'CC', '520000010', 'Mónica', 'Alejandra', 'Torres', 'Vargas', 'Madre', '3102000010', '6013000010', 'parent10@mail.com', 'Carrera 15 # 20-30, Bogotá', 'Abogado', TRUE),
(11, 'CC', '520000011', 'Gustavo', 'Adolfo', 'Flores', 'Castro', 'Padre', '3102000011', '6013000011', 'parent11@mail.com', 'Carrera 16 # 21-31, Bogotá', 'Contador', TRUE),
(12, 'CC', '520000012', 'Diana', 'Carolina', 'Rivera', 'Ortiz', 'Madre', '3102000012', '6013000012', 'parent12@mail.com', 'Carrera 17 # 22-32, Bogotá', 'Ingeniero', FALSE),
(13, 'CC', '520000013', 'Hernán', 'Darío', 'Vargas', 'Jiménez', 'Padre', '3102000013', '6013000013', 'parent13@mail.com', 'Carrera 18 # 23-33, Bogotá', 'Docente', TRUE),
(14, 'CC', '520000014', 'Paola', 'Andrea', 'Castro', 'Moreno', 'Madre', '3102000014', '6013000014', 'parent14@mail.com', 'Carrera 19 # 24-34, Bogotá', 'Médico', TRUE),
(15, 'CC', '520000015', 'Óscar', 'Iván', 'Ortiz', 'Muñoz', 'Padre', '3102000015', '6013000015', 'parent15@mail.com', 'Carrera 20 # 25-35, Bogotá', 'Comerciante', TRUE),
(16, 'CC', '520000016', 'Liliana', 'Marcela', 'Jiménez', 'Rojas', 'Madre', '3102000016', '6013000016', 'parent16@mail.com', 'Carrera 21 # 26-36, Bogotá', 'Abogado', FALSE),
(17, 'CC', '520000017', 'Mauricio', 'Eduardo', 'Moreno', 'Herrera', 'Padre', '3102000017', '6013000017', 'parent17@mail.com', 'Carrera 22 # 27-37, Bogotá', 'Contador', TRUE),
(18, 'CC', '520000018', 'Natalia', 'Andrea', 'Muñoz', 'Medina', 'Madre', '3102000018', '6013000018', 'parent18@mail.com', 'Carrera 23 # 28-38, Bogotá', 'Ingeniero', TRUE),
(19, 'CC', '520000019', 'Alberto', 'Mario', 'Rojas', 'Aguilar', 'Padre', '3102000019', '6013000019', 'parent19@mail.com', 'Carrera 24 # 29-39, Bogotá', 'Docente', TRUE),
(20, 'CC', '520000020', 'Catalina', 'Isabel', 'Herrera', 'Silva', 'Madre', '3102000020', '6013000020', 'parent20@mail.com', 'Carrera 25 # 30-40, Bogotá', 'Médico', FALSE),
(21, 'CC', '520000021', 'Diego', 'Fernando', 'Medina', 'Navarro', 'Padre', '3102000021', '6013000021', 'parent21@mail.com', 'Carrera 26 # 31-41, Bogotá', 'Comerciante', TRUE),
(22, 'CC', '520000022', 'Juliana', 'Marcela', 'Aguilar', 'Molina', 'Madre', '3102000022', '6013000022', 'parent22@mail.com', 'Carrera 27 # 32-42, Bogotá', 'Abogado', TRUE),
(23, 'CC', '520000023', 'Camilo', 'Ernesto', 'Silva', 'Suárez', 'Padre', '3102000023', '6013000023', 'parent23@mail.com', 'Carrera 28 # 33-43, Bogotá', 'Contador', TRUE),
(24, 'CC', '520000024', 'Alejandra', 'María', 'Navarro', 'Ortega', 'Madre', '3102000024', '6013000024', 'parent24@mail.com', 'Carrera 29 # 34-44, Bogotá', 'Ingeniero', FALSE),
(25, 'CC', '520000025', 'Sergio', 'Andrés', 'Molina', 'Delgado', 'Padre', '3102000025', '6013000025', 'parent25@mail.com', 'Carrera 30 # 35-45, Bogotá', 'Docente', TRUE),
(26, 'CC', '520000026', 'Carolina', 'Beatriz', 'Suárez', 'Reyes', 'Madre', '3102000026', '6013000026', 'parent26@mail.com', 'Carrera 31 # 36-46, Bogotá', 'Médico', TRUE),
(27, 'CC', '520000027', 'Rodrigo', 'Alfonso', 'Ortega', 'Cruz', 'Padre', '3102000027', '6013000027', 'parent27@mail.com', 'Carrera 32 # 37-47, Bogotá', 'Comerciante', TRUE),
(28, 'CC', '520000028', 'Tatiana', 'Jimena', 'Delgado', 'García', 'Madre', '3102000028', '6013000028', 'parent28@mail.com', 'Carrera 33 # 38-48, Bogotá', 'Abogado', FALSE),
(29, 'CC', '520000029', 'Andrés', 'Felipe', 'Reyes', 'Rodríguez', 'Padre', '3102000029', '6013000029', 'parent29@mail.com', 'Carrera 34 # 39-49, Bogotá', 'Contador', TRUE),
(30, 'CC', '520000030', 'Verónica', 'Lucía', 'Cruz', 'Martínez', 'Madre', '3102000030', '6013000030', 'parent30@mail.com', 'Carrera 35 # 40-50, Bogotá', 'Ingeniero', TRUE);

INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) VALUES
(1, 1, TRUE, TRUE),
(2, 2, TRUE, TRUE),
(3, 3, TRUE, TRUE),
(4, 4, TRUE, TRUE),
(5, 5, TRUE, TRUE),
(6, 6, TRUE, TRUE),
(7, 7, TRUE, TRUE),
(8, 8, TRUE, TRUE),
(9, 9, TRUE, TRUE),
(10, 10, TRUE, TRUE),
(11, 11, TRUE, TRUE),
(12, 12, TRUE, TRUE),
(13, 13, TRUE, TRUE),
(14, 14, TRUE, TRUE),
(15, 15, TRUE, TRUE),
(16, 16, TRUE, TRUE),
(17, 17, TRUE, TRUE),
(18, 18, TRUE, TRUE),
(19, 19, TRUE, TRUE),
(20, 20, TRUE, TRUE),
(21, 21, TRUE, TRUE),
(22, 22, TRUE, TRUE),
(23, 23, TRUE, TRUE),
(24, 24, TRUE, TRUE),
(25, 25, TRUE, TRUE),
(26, 26, TRUE, TRUE),
(27, 27, TRUE, TRUE),
(28, 28, TRUE, TRUE),
(29, 29, TRUE, TRUE),
(30, 30, TRUE, TRUE);

INSERT INTO profesores (id_profesor, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) VALUES
(1, 'CC', '790000001', 'María', 'Isabel', 'Gómez', 'Rojas', 'teacher1@littlehorizons.edu.co', '3203000001', 'Matemáticas', '2016-02-01', 'Activo'),
(2, 'CC', '790000002', 'Carlos', 'Alberto', 'Pérez', 'Luna', 'teacher2@littlehorizons.edu.co', '3203000002', 'Español', '2017-02-01', 'Activo'),
(3, 'CC', '790000003', 'Laura', 'Vanessa', 'Rodríguez', 'Mora', 'teacher3@littlehorizons.edu.co', '3203000003', 'Inglés', '2018-02-01', 'Activo'),
(4, 'CC', '790000004', 'Andrés', 'Felipe', 'Martínez', 'Castro', 'teacher4@littlehorizons.edu.co', '3203000004', 'Ciencias Naturales', '2019-02-01', 'Activo'),
(5, 'CC', '790000005', 'Sandra', 'Milena', 'López', 'Vega', 'teacher5@littlehorizons.edu.co', '3203000005', 'Ciencias Sociales', '2020-02-01', 'Activo'),
(6, 'CC', '790000006', 'Julián', 'Esteban', 'Díaz', 'Pardo', 'teacher6@littlehorizons.edu.co', '3203000006', 'Educación Física', '2021-02-01', 'Activo'),
(7, 'CC', '790000007', 'Paula', 'Andrea', 'Sánchez', 'Ríos', 'teacher7@littlehorizons.edu.co', '3203000007', 'Artes', '2022-02-01', 'Activo'),
(8, 'CC', '790000008', 'Ricardo', 'Alfonso', 'Ramírez', 'Soto', 'teacher8@littlehorizons.edu.co', '3203000008', 'Tecnología', '2023-02-01', 'Activo'),
(9, 'CC', '790000009', 'Diana', 'Carolina', 'Torres', 'Mejía', 'teacher9@littlehorizons.edu.co', '3203000009', 'Filosofía', '2024-02-01', 'Activo'),
(10, 'CC', '790000010', 'Gustavo', 'Adolfo', 'Flores', 'Niño', 'teacher10@littlehorizons.edu.co', '3203000010', 'Química', '2015-02-01', 'Activo'),
(11, 'CC', '790000011', 'Natalia', 'Andrea', 'Rivera', 'Salazar', 'teacher11@littlehorizons.edu.co', '3203000011', 'Física', '2016-02-01', 'Activo'),
(12, 'CC', '790000012', 'Hernán', 'Darío', 'Vargas', 'Cortés', 'teacher12@littlehorizons.edu.co', '3203000012', 'Biología', '2017-02-01', 'Activo'),
(13, 'CC', '790000013', 'Catalina', 'Isabel', 'Castro', 'Peña', 'teacher13@littlehorizons.edu.co', '3203000013', 'Informática', '2018-02-01', 'Activo'),
(14, 'CC', '790000014', 'Mauricio', 'Eduardo', 'Ortiz', 'Quintero', 'teacher14@littlehorizons.edu.co', '3203000014', 'Música', '2019-02-01', 'Activo'),
(15, 'CC', '790000015', 'Liliana', 'Marcela', 'Jiménez', 'Acosta', 'teacher15@littlehorizons.edu.co', '3203000015', 'Danza', '2020-02-01', 'Activo'),
(16, 'CC', '790000016', 'Óscar', 'Iván', 'Moreno', 'Vargas', 'teacher16@littlehorizons.edu.co', '3203000016', 'Teatro', '2021-02-01', 'Activo'),
(17, 'CC', '790000017', 'Claudia', 'Patricia', 'Muñoz', 'Suárez', 'teacher17@littlehorizons.edu.co', '3203000017', 'Religión', '2022-02-01', 'Activo'),
(18, 'CC', '790000018', 'Fernando', 'José', 'Rojas', 'Cifuentes', 'teacher18@littlehorizons.edu.co', '3203000018', 'Ética', '2023-02-01', 'Activo'),
(19, 'CC', '790000019', 'Mónica', 'Alejandra', 'Herrera', 'Duarte', 'teacher19@littlehorizons.edu.co', '3203000019', 'Cátedra de Paz', '2024-02-01', 'Activo'),
(20, 'CC', '790000020', 'Alberto', 'Mario', 'Medina', 'Franco', 'teacher20@littlehorizons.edu.co', '3203000020', 'Emprendimiento', '2015-02-01', 'Activo'),
(21, 'CC', '790000021', 'Verónica', 'Lucía', 'Aguilar', 'Guerrero', 'teacher21@littlehorizons.edu.co', '3203000021', 'Lectura Crítica', '2016-02-01', 'Activo'),
(22, 'CC', '790000022', 'Sergio', 'Andrés', 'Silva', 'Mosquera', 'teacher22@littlehorizons.edu.co', '3203000022', 'Estadística', '2017-02-01', 'Activo'),
(23, 'CC', '790000023', 'Tatiana', 'Jimena', 'Navarro', 'Ospina', 'teacher23@littlehorizons.edu.co', '3203000023', 'Geometría', '2018-02-01', 'Activo'),
(24, 'CC', '790000024', 'Camilo', 'Ernesto', 'Molina', 'Rincón', 'teacher24@littlehorizons.edu.co', '3203000024', 'Álgebra', '2019-02-01', 'Activo'),
(25, 'CC', '790000025', 'Alejandra', 'María', 'Suárez', 'Tovar', 'teacher25@littlehorizons.edu.co', '3203000025', 'Cálculo', '2020-02-01', 'Activo'),
(26, 'CC', '790000026', 'Rodrigo', 'Alfonso', 'Ortega', 'Cárdenas', 'teacher26@littlehorizons.edu.co', '3203000026', 'Laboratorio', '2021-02-01', 'Activo'),
(27, 'CC', '790000027', 'Carolina', 'Beatriz', 'Delgado', 'Villamil', 'teacher27@littlehorizons.edu.co', '3203000027', 'Robótica', '2022-02-01', 'Activo'),
(28, 'CC', '790000028', 'Andrés', 'Felipe', 'Reyes', 'Pineda', 'teacher28@littlehorizons.edu.co', '3203000028', 'Programación', '2023-02-01', 'Activo'),
(29, 'CC', '790000029', 'Juliana', 'Marcela', 'Cruz', 'Arango', 'teacher29@littlehorizons.edu.co', '3203000029', 'Diseño', '2024-02-01', 'Activo'),
(30, 'CC', '790000030', 'Diego', 'Fernando', 'García', 'Londono', 'teacher30@littlehorizons.edu.co', '3203000030', 'Comunicación', '2015-02-01', 'Activo');

INSERT INTO materias (id_materia, codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) VALUES
(1, 'MAT-01', 'Matemáticas', 'Asignatura de Matemáticas para el grado correspondiente', 1, 5, TRUE),
(2, 'ESP-01', 'Español', 'Asignatura de Español para el grado correspondiente', 1, 4, TRUE),
(3, 'ING-02', 'Inglés', 'Asignatura de Inglés para el grado correspondiente', 2, 3, TRUE),
(4, 'CN-02', 'Ciencias Naturales', 'Asignatura de Ciencias Naturales para el grado correspondiente', 2, 4, TRUE),
(5, 'CS-03', 'Ciencias Sociales', 'Asignatura de Ciencias Sociales para el grado correspondiente', 3, 4, TRUE),
(6, 'EF-03', 'Educación Física', 'Asignatura de Educación Física para el grado correspondiente', 3, 2, TRUE),
(7, 'ART-04', 'Artes', 'Asignatura de Artes para el grado correspondiente', 4, 2, TRUE),
(8, 'TEC-04', 'Tecnología', 'Asignatura de Tecnología para el grado correspondiente', 4, 2, TRUE),
(9, 'FIL-05', 'Filosofía', 'Asignatura de Filosofía para el grado correspondiente', 5, 2, TRUE),
(10, 'QUI-05', 'Química', 'Asignatura de Química para el grado correspondiente', 5, 3, TRUE),
(11, 'FIS-06', 'Física', 'Asignatura de Física para el grado correspondiente', 6, 3, TRUE),
(12, 'BIO-06', 'Biología', 'Asignatura de Biología para el grado correspondiente', 6, 3, TRUE),
(13, 'INF-07', 'Informática', 'Asignatura de Informática para el grado correspondiente', 7, 2, TRUE),
(14, 'MUS-07', 'Música', 'Asignatura de Música para el grado correspondiente', 7, 2, TRUE),
(15, 'DAN-08', 'Danza', 'Asignatura de Danza para el grado correspondiente', 8, 2, TRUE),
(16, 'TEA-08', 'Teatro', 'Asignatura de Teatro para el grado correspondiente', 8, 2, TRUE),
(17, 'REL-09', 'Religión', 'Asignatura de Religión para el grado correspondiente', 9, 2, TRUE),
(18, 'ETI-09', 'Ética', 'Asignatura de Ética para el grado correspondiente', 9, 2, TRUE),
(19, 'CPA-10', 'Cátedra de Paz', 'Asignatura de Cátedra de Paz para el grado correspondiente', 10, 2, TRUE),
(20, 'EMP-10', 'Emprendimiento', 'Asignatura de Emprendimiento para el grado correspondiente', 10, 2, TRUE),
(21, 'LEC-11', 'Lectura Crítica', 'Asignatura de Lectura Crítica para el grado correspondiente', 11, 3, TRUE),
(22, 'EST-11', 'Estadística', 'Asignatura de Estadística para el grado correspondiente', 11, 3, TRUE),
(23, 'GEO-12', 'Geometría', 'Asignatura de Geometría para el grado correspondiente', 12, 3, TRUE),
(24, 'ALG-12', 'Álgebra', 'Asignatura de Álgebra para el grado correspondiente', 12, 3, TRUE),
(25, 'CAL-12', 'Cálculo', 'Asignatura de Cálculo para el grado correspondiente', 12, 3, TRUE),
(26, 'LAB-11', 'Laboratorio', 'Asignatura de Laboratorio para el grado correspondiente', 11, 2, TRUE),
(27, 'ROB-10', 'Robótica', 'Asignatura de Robótica para el grado correspondiente', 10, 2, TRUE),
(28, 'PRO-11', 'Programación', 'Asignatura de Programación para el grado correspondiente', 11, 3, TRUE),
(29, 'DIS-09', 'Diseño', 'Asignatura de Diseño para el grado correspondiente', 9, 2, TRUE),
(30, 'COM-08', 'Comunicación', 'Asignatura de Comunicación para el grado correspondiente', 8, 2, TRUE);

INSERT INTO matriculas (id_matricula, id_estudiante, id_grado, año_academico, estado_matricula, fecha_matricula, observaciones) VALUES
(1, 1, 1, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(2, 2, 2, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(3, 3, 3, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(4, 4, 4, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(5, 5, 5, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(6, 6, 6, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(7, 7, 7, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(8, 8, 8, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(9, 9, 9, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(10, 10, 10, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(11, 11, 11, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(12, 12, 12, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(13, 13, 1, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(14, 14, 2, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(15, 15, 3, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(16, 16, 4, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(17, 17, 5, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(18, 18, 6, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(19, 19, 7, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(20, 20, 8, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(21, 21, 9, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(22, 22, 10, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(23, 23, 11, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(24, 24, 12, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(25, 25, 1, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(26, 26, 2, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(27, 27, 3, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(28, 28, 4, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(29, 29, 5, 2026, 'Activa', '2026-01-15', 'Matrícula regular'),
(30, 30, 6, 2026, 'Activa', '2026-01-15', 'Matrícula regular');

INSERT INTO asignaciones_profesor (id_asignacion, id_profesor, id_materia, año_academico, seccion) VALUES
(1, 1, 1, 2026, 'B'),
(2, 2, 3, 2026, 'A'),
(3, 3, 5, 2026, 'B'),
(4, 4, 7, 2026, 'A'),
(5, 5, 9, 2026, 'B'),
(6, 6, 11, 2026, 'A'),
(7, 7, 13, 2026, 'B'),
(8, 8, 15, 2026, 'A'),
(9, 9, 17, 2026, 'B'),
(10, 10, 19, 2026, 'A'),
(11, 11, 21, 2026, 'B'),
(12, 12, 23, 2026, 'A'),
(13, 13, 1, 2026, 'B'),
(14, 14, 3, 2026, 'A'),
(15, 15, 5, 2026, 'B'),
(16, 16, 7, 2026, 'A'),
(17, 17, 9, 2026, 'B'),
(18, 18, 11, 2026, 'A'),
(19, 19, 13, 2026, 'B'),
(20, 20, 15, 2026, 'A'),
(21, 21, 17, 2026, 'B'),
(22, 22, 19, 2026, 'A'),
(23, 23, 21, 2026, 'B'),
(24, 24, 23, 2026, 'A'),
(25, 25, 1, 2026, 'B'),
(26, 26, 3, 2026, 'A'),
(27, 27, 5, 2026, 'B'),
(28, 28, 7, 2026, 'A'),
(29, 29, 9, 2026, 'B'),
(30, 30, 11, 2026, 'A');

INSERT INTO horarios (id_horario, id_asignacion, dia_semana, hora_inicio, hora_fin) VALUES
(1, 1, 'Lunes', '08:00:00', '10:00:00'),
(2, 2, 'Martes', '09:00:00', '11:00:00'),
(3, 3, 'Miércoles', '10:00:00', '12:00:00'),
(4, 4, 'Jueves', '11:00:00', '13:00:00'),
(5, 5, 'Viernes', '07:00:00', '09:00:00'),
(6, 6, 'Lunes', '08:00:00', '10:00:00'),
(7, 7, 'Martes', '09:00:00', '11:00:00'),
(8, 8, 'Miércoles', '10:00:00', '12:00:00'),
(9, 9, 'Jueves', '11:00:00', '13:00:00'),
(10, 10, 'Viernes', '07:00:00', '09:00:00'),
(11, 11, 'Lunes', '08:00:00', '10:00:00'),
(12, 12, 'Martes', '09:00:00', '11:00:00'),
(13, 13, 'Miércoles', '10:00:00', '12:00:00'),
(14, 14, 'Jueves', '11:00:00', '13:00:00'),
(15, 15, 'Viernes', '07:00:00', '09:00:00'),
(16, 16, 'Lunes', '08:00:00', '10:00:00'),
(17, 17, 'Martes', '09:00:00', '11:00:00'),
(18, 18, 'Miércoles', '10:00:00', '12:00:00'),
(19, 19, 'Jueves', '11:00:00', '13:00:00'),
(20, 20, 'Viernes', '07:00:00', '09:00:00'),
(21, 21, 'Lunes', '08:00:00', '10:00:00'),
(22, 22, 'Martes', '09:00:00', '11:00:00'),
(23, 23, 'Miércoles', '10:00:00', '12:00:00'),
(24, 24, 'Jueves', '11:00:00', '13:00:00'),
(25, 25, 'Viernes', '07:00:00', '09:00:00'),
(26, 26, 'Lunes', '08:00:00', '10:00:00'),
(27, 27, 'Martes', '09:00:00', '11:00:00'),
(28, 28, 'Miércoles', '10:00:00', '12:00:00'),
(29, 29, 'Jueves', '11:00:00', '13:00:00'),
(30, 30, 'Viernes', '07:00:00', '09:00:00');

INSERT INTO inscripciones_materia (id_inscripcion, id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) VALUES
(1, 1, 1, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(2, 2, 3, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(3, 3, 5, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(4, 4, 7, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(5, 5, 9, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(6, 6, 11, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(7, 7, 13, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(8, 8, 15, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(9, 9, 17, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(10, 10, 19, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(11, 11, 21, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(12, 12, 23, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(13, 13, 1, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(14, 14, 3, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(15, 15, 5, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(16, 16, 7, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(17, 17, 9, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(18, 18, 11, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(19, 19, 13, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(20, 20, 15, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(21, 21, 17, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(22, 22, 19, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(23, 23, 21, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(24, 24, 23, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(25, 25, 1, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(26, 26, 3, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(27, 27, 5, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(28, 28, 7, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(29, 29, 9, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(30, 30, 11, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(31, 1, 1, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(32, 2, 3, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(33, 3, 5, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(34, 4, 7, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(35, 5, 9, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(36, 6, 11, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(37, 7, 13, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(38, 8, 15, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(39, 9, 17, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(40, 10, 19, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(41, 11, 21, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(42, 12, 23, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(43, 13, 1, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(44, 14, 3, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(45, 15, 5, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(46, 16, 7, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(47, 17, 9, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(48, 18, 11, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(49, 19, 13, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(50, 20, 15, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(51, 21, 17, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(52, 22, 19, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(53, 23, 21, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(54, 24, 23, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(55, 25, 1, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(56, 26, 3, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(57, 27, 5, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(58, 28, 7, 2026, 'II', 'A', '2026-06-15', 'Activa'),
(59, 29, 9, 2026, 'II', 'B', '2026-06-15', 'Activa'),
(60, 30, 11, 2026, 'II', 'A', '2026-06-15', 'Activa');

INSERT INTO registros_academicos (id_registro, id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) VALUES
(1, 1, 1, 1, 2026, 'I', 3.5, 'Desempeño del primer período', '2026-04-15'),
(31, 1, 1, 1, 2026, 'II', 3.7, 'Desempeño del segundo período', '2026-08-15'),
(2, 2, 3, 2, 2026, 'I', 4.0, 'Desempeño del primer período', '2026-04-15'),
(32, 2, 3, 2, 2026, 'II', 4.2, 'Desempeño del segundo período', '2026-08-15'),
(3, 3, 5, 3, 2026, 'I', 4.5, 'Desempeño del primer período', '2026-04-15'),
(33, 3, 5, 3, 2026, 'II', 4.7, 'Desempeño del segundo período', '2026-08-15'),
(4, 4, 7, 4, 2026, 'I', 3.8, 'Desempeño del primer período', '2026-04-15'),
(34, 4, 7, 4, 2026, 'II', 4.0, 'Desempeño del segundo período', '2026-08-15'),
(5, 5, 9, 5, 2026, 'I', 4.2, 'Desempeño del primer período', '2026-04-15'),
(35, 5, 9, 5, 2026, 'II', 4.4, 'Desempeño del segundo período', '2026-08-15'),
(6, 6, 11, 6, 2026, 'I', 5.0, 'Desempeño del primer período', '2026-04-15'),
(36, 6, 11, 6, 2026, 'II', 5.0, 'Desempeño del segundo período', '2026-08-15'),
(7, 7, 13, 7, 2026, 'I', 2.8, 'Desempeño del primer período', '2026-04-15'),
(37, 7, 13, 7, 2026, 'II', 3.0, 'Desempeño del segundo período', '2026-08-15'),
(8, 8, 15, 8, 2026, 'I', 3.0, 'Desempeño del primer período', '2026-04-15'),
(38, 8, 15, 8, 2026, 'II', 3.2, 'Desempeño del segundo período', '2026-08-15'),
(9, 9, 17, 9, 2026, 'I', 4.7, 'Desempeño del primer período', '2026-04-15'),
(39, 9, 17, 9, 2026, 'II', 4.9, 'Desempeño del segundo período', '2026-08-15'),
(10, 10, 19, 10, 2026, 'I', 4.9, 'Desempeño del primer período', '2026-04-15'),
(40, 10, 19, 10, 2026, 'II', 5.0, 'Desempeño del segundo período', '2026-08-15'),
(11, 11, 21, 11, 2026, 'I', 3.2, 'Desempeño del primer período', '2026-04-15'),
(41, 11, 21, 11, 2026, 'II', 3.4, 'Desempeño del segundo período', '2026-08-15'),
(12, 12, 23, 12, 2026, 'I', 4.1, 'Desempeño del primer período', '2026-04-15'),
(42, 12, 23, 12, 2026, 'II', 4.3, 'Desempeño del segundo período', '2026-08-15'),
(13, 13, 1, 13, 2026, 'I', 4.6, 'Desempeño del primer período', '2026-04-15'),
(43, 13, 1, 13, 2026, 'II', 4.8, 'Desempeño del segundo período', '2026-08-15'),
(14, 14, 3, 14, 2026, 'I', 3.9, 'Desempeño del primer período', '2026-04-15'),
(44, 14, 3, 14, 2026, 'II', 4.1, 'Desempeño del segundo período', '2026-08-15'),
(15, 15, 5, 15, 2026, 'I', 4.3, 'Desempeño del primer período', '2026-04-15'),
(45, 15, 5, 15, 2026, 'II', 4.5, 'Desempeño del segundo período', '2026-08-15'),
(16, 16, 7, 16, 2026, 'I', 4.8, 'Desempeño del primer período', '2026-04-15'),
(46, 16, 7, 16, 2026, 'II', 5.0, 'Desempeño del segundo período', '2026-08-15'),
(17, 17, 9, 17, 2026, 'I', 3.4, 'Desempeño del primer período', '2026-04-15'),
(47, 17, 9, 17, 2026, 'II', 3.6, 'Desempeño del segundo período', '2026-08-15'),
(18, 18, 11, 18, 2026, 'I', 3.7, 'Desempeño del primer período', '2026-04-15'),
(48, 18, 11, 18, 2026, 'II', 3.9, 'Desempeño del segundo período', '2026-08-15'),
(19, 19, 13, 19, 2026, 'I', 4.4, 'Desempeño del primer período', '2026-04-15'),
(49, 19, 13, 19, 2026, 'II', 4.6, 'Desempeño del segundo período', '2026-08-15'),
(20, 20, 15, 20, 2026, 'I', 5.0, 'Desempeño del primer período', '2026-04-15'),
(50, 20, 15, 20, 2026, 'II', 5.0, 'Desempeño del segundo período', '2026-08-15'),
(21, 21, 17, 21, 2026, 'I', 2.9, 'Desempeño del primer período', '2026-04-15'),
(51, 21, 17, 21, 2026, 'II', 3.1, 'Desempeño del segundo período', '2026-08-15'),
(22, 22, 19, 22, 2026, 'I', 3.6, 'Desempeño del primer período', '2026-04-15'),
(52, 22, 19, 22, 2026, 'II', 3.8, 'Desempeño del segundo período', '2026-08-15'),
(23, 23, 21, 23, 2026, 'I', 4.2, 'Desempeño del primer período', '2026-04-15'),
(53, 23, 21, 23, 2026, 'II', 4.4, 'Desempeño del segundo período', '2026-08-15'),
(24, 24, 23, 24, 2026, 'I', 4.0, 'Desempeño del primer período', '2026-04-15'),
(54, 24, 23, 24, 2026, 'II', 4.2, 'Desempeño del segundo período', '2026-08-15'),
(25, 25, 1, 25, 2026, 'I', 4.5, 'Desempeño del primer período', '2026-04-15'),
(55, 25, 1, 25, 2026, 'II', 4.7, 'Desempeño del segundo período', '2026-08-15'),
(26, 26, 3, 26, 2026, 'I', 3.3, 'Desempeño del primer período', '2026-04-15'),
(56, 26, 3, 26, 2026, 'II', 3.5, 'Desempeño del segundo período', '2026-08-15'),
(27, 27, 5, 27, 2026, 'I', 4.7, 'Desempeño del primer período', '2026-04-15'),
(57, 27, 5, 27, 2026, 'II', 4.9, 'Desempeño del segundo período', '2026-08-15'),
(28, 28, 7, 28, 2026, 'I', 3.8, 'Desempeño del primer período', '2026-04-15'),
(58, 28, 7, 28, 2026, 'II', 4.0, 'Desempeño del segundo período', '2026-08-15'),
(29, 29, 9, 29, 2026, 'I', 4.1, 'Desempeño del primer período', '2026-04-15'),
(59, 29, 9, 29, 2026, 'II', 4.3, 'Desempeño del segundo período', '2026-08-15'),
(30, 30, 11, 30, 2026, 'I', 4.9, 'Desempeño del primer período', '2026-04-15'),
(60, 30, 11, 30, 2026, 'II', 5.0, 'Desempeño del segundo período', '2026-08-15');

INSERT INTO asistencias (id_asistencia, id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, registrado_por) VALUES
(1, 1, 1, '2026-10-02', 'Presente', 'Registro diario', 1),
(2, 2, 3, '2026-10-03', 'Presente', 'Registro diario', 2),
(3, 3, 5, '2026-10-04', 'Ausente', 'Registro diario', 3),
(4, 4, 7, '2026-10-05', 'Tardanza', 'Registro diario', 4),
(5, 5, 9, '2026-10-06', 'Presente', 'Registro diario', 5),
(6, 6, 11, '2026-10-07', 'Excusa Médica', 'Registro diario', 6),
(7, 7, 13, '2026-10-08', 'Presente', 'Registro diario', 7),
(8, 8, 15, '2026-10-09', 'Presente', 'Registro diario', 8),
(9, 9, 17, '2026-10-10', 'Ausente', 'Registro diario', 9),
(10, 10, 19, '2026-10-11', 'Tardanza', 'Registro diario', 10),
(11, 11, 21, '2026-10-12', 'Presente', 'Registro diario', 11),
(12, 12, 23, '2026-10-13', 'Excusa Médica', 'Registro diario', 12),
(13, 13, 1, '2026-10-14', 'Presente', 'Registro diario', 13),
(14, 14, 3, '2026-10-15', 'Presente', 'Registro diario', 14),
(15, 15, 5, '2026-10-16', 'Ausente', 'Registro diario', 15),
(16, 16, 7, '2026-10-17', 'Tardanza', 'Registro diario', 16),
(17, 17, 9, '2026-10-18', 'Presente', 'Registro diario', 17),
(18, 18, 11, '2026-10-19', 'Excusa Médica', 'Registro diario', 18),
(19, 19, 13, '2026-10-20', 'Presente', 'Registro diario', 19),
(20, 20, 15, '2026-10-21', 'Presente', 'Registro diario', 20),
(21, 21, 17, '2026-10-22', 'Ausente', 'Registro diario', 21),
(22, 22, 19, '2026-10-23', 'Tardanza', 'Registro diario', 22),
(23, 23, 21, '2026-10-24', 'Presente', 'Registro diario', 23),
(24, 24, 23, '2026-10-25', 'Excusa Médica', 'Registro diario', 24),
(25, 25, 1, '2026-10-26', 'Presente', 'Registro diario', 25),
(26, 26, 3, '2026-10-27', 'Presente', 'Registro diario', 26),
(27, 27, 5, '2026-10-28', 'Ausente', 'Registro diario', 27),
(28, 28, 7, '2026-10-01', 'Tardanza', 'Registro diario', 28),
(29, 29, 9, '2026-10-02', 'Presente', 'Registro diario', 29),
(30, 30, 11, '2026-10-03', 'Excusa Médica', 'Registro diario', 30),
(31, 1, 1, '2026-11-02', 'Ausente', 'Inasistencia recurrente', 1),
(32, 1, 1, '2026-11-03', 'Ausente', 'Inasistencia recurrente', 1),
(33, 1, 1, '2026-11-04', 'Ausente', 'Inasistencia recurrente', 1),
(34, 1, 1, '2026-11-05', 'Ausente', 'Inasistencia recurrente', 1),
(35, 1, 1, '2026-11-06', 'Ausente', 'Inasistencia recurrente', 1);

INSERT INTO clubes (id_club, nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) VALUES
(1, 'Club de Ciencias', 'Espacio extracurricular: Club de Ciencias', 1, 21, 'Lunes 15:00-17:00', TRUE),
(2, 'Club de Artes', 'Espacio extracurricular: Club de Artes', 2, 22, 'Martes 15:00-17:00', TRUE),
(3, 'Club de Deportes', 'Espacio extracurricular: Club de Deportes', 3, 23, 'Miércoles 15:00-17:00', TRUE),
(4, 'Club de Robótica', 'Espacio extracurricular: Club de Robótica', 4, 24, 'Jueves 15:00-17:00', TRUE),
(5, 'Club de Música', 'Espacio extracurricular: Club de Música', 5, 25, 'Viernes 15:00-17:00', TRUE),
(6, 'Club de Teatro', 'Espacio extracurricular: Club de Teatro', 6, 26, 'Lunes 15:00-17:00', TRUE),
(7, 'Club de Danza', 'Espacio extracurricular: Club de Danza', 7, 27, 'Martes 15:00-17:00', TRUE),
(8, 'Club de Lectura', 'Espacio extracurricular: Club de Lectura', 8, 28, 'Miércoles 15:00-17:00', TRUE),
(9, 'Club de Ajedrez', 'Espacio extracurricular: Club de Ajedrez', 9, 29, 'Jueves 15:00-17:00', TRUE),
(10, 'Club de Fotografía', 'Espacio extracurricular: Club de Fotografía', 10, 30, 'Viernes 15:00-17:00', TRUE),
(11, 'Club de Astronomía', 'Espacio extracurricular: Club de Astronomía', 11, 31, 'Lunes 15:00-17:00', TRUE),
(12, 'Club de Debate', 'Espacio extracurricular: Club de Debate', 12, 32, 'Martes 15:00-17:00', TRUE),
(13, 'Club de Emprendimiento', 'Espacio extracurricular: Club de Emprendimiento', 13, 33, 'Miércoles 15:00-17:00', TRUE),
(14, 'Club de Programación', 'Espacio extracurricular: Club de Programación', 14, 34, 'Jueves 15:00-17:00', TRUE),
(15, 'Club de Idiomas', 'Espacio extracurricular: Club de Idiomas', 15, 20, 'Viernes 15:00-17:00', TRUE),
(16, 'Club de Medio Ambiente', 'Espacio extracurricular: Club de Medio Ambiente', 16, 21, 'Lunes 15:00-17:00', TRUE),
(17, 'Club de Cocina', 'Espacio extracurricular: Club de Cocina', 17, 22, 'Martes 15:00-17:00', TRUE),
(18, 'Club de Matemáticas', 'Espacio extracurricular: Club de Matemáticas', 18, 23, 'Miércoles 15:00-17:00', TRUE),
(19, 'Club de Historia', 'Espacio extracurricular: Club de Historia', 19, 24, 'Jueves 15:00-17:00', TRUE),
(20, 'Club de Cine', 'Espacio extracurricular: Club de Cine', 20, 25, 'Viernes 15:00-17:00', TRUE),
(21, 'Club de Periodismo', 'Espacio extracurricular: Club de Periodismo', 21, 26, 'Lunes 15:00-17:00', TRUE),
(22, 'Club de Voluntariado', 'Espacio extracurricular: Club de Voluntariado', 22, 27, 'Martes 15:00-17:00', TRUE),
(23, 'Club de Sostenibilidad', 'Espacio extracurricular: Club de Sostenibilidad', 23, 28, 'Miércoles 15:00-17:00', TRUE),
(24, 'Club de Innovación', 'Espacio extracurricular: Club de Innovación', 24, 29, 'Jueves 15:00-17:00', TRUE),
(25, 'Club de Atletismo', 'Espacio extracurricular: Club de Atletismo', 25, 30, 'Viernes 15:00-17:00', TRUE),
(26, 'Club de Baloncesto', 'Espacio extracurricular: Club de Baloncesto', 26, 31, 'Lunes 15:00-17:00', TRUE),
(27, 'Club de Fútbol', 'Espacio extracurricular: Club de Fútbol', 27, 32, 'Martes 15:00-17:00', TRUE),
(28, 'Club de Voleibol', 'Espacio extracurricular: Club de Voleibol', 28, 33, 'Miércoles 15:00-17:00', TRUE),
(29, 'Club de Natación', 'Espacio extracurricular: Club de Natación', 29, 34, 'Jueves 15:00-17:00', TRUE),
(30, 'Club de Bienestar', 'Espacio extracurricular: Club de Bienestar', 30, 20, 'Viernes 15:00-17:00', TRUE);

INSERT INTO membresias_club (id_membresia, id_estudiante, id_club, fecha_ingreso, estado_membresia) VALUES
(1, 1, 1, '2026-02-01', 'Activa'),
(2, 2, 2, '2026-02-01', 'Activa'),
(3, 3, 3, '2026-02-01', 'Activa'),
(4, 4, 4, '2026-02-01', 'Activa'),
(5, 5, 5, '2026-02-01', 'Activa'),
(6, 6, 6, '2026-02-01', 'Activa'),
(7, 7, 7, '2026-02-01', 'Activa'),
(8, 8, 8, '2026-02-01', 'Activa'),
(9, 9, 9, '2026-02-01', 'Activa'),
(10, 10, 10, '2026-02-01', 'Activa'),
(11, 11, 11, '2026-02-01', 'Activa'),
(12, 12, 12, '2026-02-01', 'Activa'),
(13, 13, 13, '2026-02-01', 'Activa'),
(14, 14, 14, '2026-02-01', 'Activa'),
(15, 15, 15, '2026-02-01', 'Activa'),
(16, 16, 16, '2026-02-01', 'Activa'),
(17, 17, 17, '2026-02-01', 'Activa'),
(18, 18, 18, '2026-02-01', 'Activa'),
(19, 19, 19, '2026-02-01', 'Activa'),
(20, 20, 20, '2026-02-01', 'Activa'),
(21, 21, 21, '2026-02-01', 'Activa'),
(22, 22, 22, '2026-02-01', 'Activa'),
(23, 23, 23, '2026-02-01', 'Activa'),
(24, 24, 24, '2026-02-01', 'Activa'),
(25, 25, 25, '2026-02-01', 'Activa'),
(26, 26, 26, '2026-02-01', 'Activa'),
(27, 27, 27, '2026-02-01', 'Activa'),
(28, 28, 28, '2026-02-01', 'Activa'),
(29, 29, 29, '2026-02-01', 'Activa'),
(30, 30, 30, '2026-02-01', 'Activa');

INSERT INTO eventos (id_evento, nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, id_organizador, esta_activo) VALUES
(1, 'Feria de Ciencias', 'Evento institucional: Feria de Ciencias', 'Académico', '2026-09-11', 'Auditorio principal', TRUE, 1, TRUE),
(2, 'Día Cultural', 'Evento institucional: Día Cultural', 'Cultural', '2026-09-12', 'Auditorio principal', FALSE, 2, TRUE),
(3, 'Día del Deporte', 'Evento institucional: Día del Deporte', 'Deportivo', '2026-09-13', 'Auditorio principal', TRUE, 3, TRUE),
(4, 'Festival de Artes', 'Evento institucional: Festival de Artes', 'Cultural', '2026-09-14', 'Auditorio principal', FALSE, 4, TRUE),
(5, 'Olimpiadas Matemáticas', 'Evento institucional: Olimpiadas Matemáticas', 'Académico', '2026-09-15', 'Auditorio principal', TRUE, 5, TRUE),
(6, 'Feria de Emprendimiento', 'Evento institucional: Feria de Emprendimiento', 'Académico', '2026-09-16', 'Auditorio principal', FALSE, 6, TRUE),
(7, 'Semana de la Lectura', 'Evento institucional: Semana de la Lectura', 'Cultural', '2026-09-17', 'Auditorio principal', TRUE, 7, TRUE),
(8, 'Concurso de Robótica', 'Evento institucional: Concurso de Robótica', 'Científico', '2026-09-18', 'Auditorio principal', FALSE, 8, TRUE),
(9, 'Día del Idioma', 'Evento institucional: Día del Idioma', 'Cultural', '2026-09-19', 'Auditorio principal', TRUE, 9, TRUE),
(10, 'Festival de Música', 'Evento institucional: Festival de Música', 'Cultural', '2026-09-20', 'Auditorio principal', FALSE, 10, TRUE);

INSERT INTO participantes_evento (id_participacion, id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) VALUES
(1, 1, 1, 'Participante', '1er Lugar', 'Participación registrada'),
(2, 2, 2, 'Competidor', 'Participante', 'Participación registrada'),
(3, 3, 3, 'Organizador', '3er Lugar', 'Participación registrada'),
(4, 4, 4, 'Asistente', 'Participante', 'Participación registrada'),
(5, 5, 5, 'Participante', '1er Lugar', 'Participación registrada'),
(6, 6, 6, 'Competidor', 'Participante', 'Participación registrada'),
(7, 7, 7, 'Organizador', '3er Lugar', 'Participación registrada'),
(8, 8, 8, 'Asistente', 'Participante', 'Participación registrada'),
(9, 9, 9, 'Participante', '1er Lugar', 'Participación registrada'),
(10, 10, 10, 'Competidor', 'Participante', 'Participación registrada'),
(11, 1, 11, 'Organizador', '3er Lugar', 'Participación registrada'),
(12, 2, 12, 'Asistente', 'Participante', 'Participación registrada'),
(13, 3, 13, 'Participante', '1er Lugar', 'Participación registrada'),
(14, 4, 14, 'Competidor', 'Participante', 'Participación registrada'),
(15, 5, 15, 'Organizador', '3er Lugar', 'Participación registrada'),
(16, 6, 16, 'Asistente', 'Participante', 'Participación registrada'),
(17, 7, 17, 'Participante', '1er Lugar', 'Participación registrada'),
(18, 8, 18, 'Competidor', 'Participante', 'Participación registrada'),
(19, 9, 19, 'Organizador', '3er Lugar', 'Participación registrada'),
(20, 10, 20, 'Asistente', 'Participante', 'Participación registrada'),
(21, 1, 21, 'Participante', '1er Lugar', 'Participación registrada'),
(22, 2, 22, 'Competidor', 'Participante', 'Participación registrada'),
(23, 3, 23, 'Organizador', '3er Lugar', 'Participación registrada'),
(24, 4, 24, 'Asistente', 'Participante', 'Participación registrada'),
(25, 5, 25, 'Participante', '1er Lugar', 'Participación registrada'),
(26, 6, 26, 'Competidor', 'Participante', 'Participación registrada'),
(27, 7, 27, 'Organizador', '3er Lugar', 'Participación registrada'),
(28, 8, 28, 'Asistente', 'Participante', 'Participación registrada'),
(29, 9, 29, 'Participante', '1er Lugar', 'Participación registrada'),
(30, 10, 30, 'Competidor', 'Participante', 'Participación registrada');

INSERT INTO conceptos_estudiante (id_concepto_estudiante, id_estudiante, id_concepto, monto_personalizado, motivo) VALUES
(1, 21, 2, 315000, 'Descuento por hermano en el colegio'),
(2, 22, 2, 315000, 'Descuento por hermano en el colegio'),
(3, 23, 2, 315000, 'Descuento por hermano en el colegio'),
(4, 24, 2, 315000, 'Descuento por hermano en el colegio'),
(5, 25, 2, 315000, 'Descuento por hermano en el colegio'),
(6, 26, 2, 315000, 'Descuento por hermano en el colegio'),
(7, 27, 2, 315000, 'Descuento por hermano en el colegio'),
(8, 28, 2, 315000, 'Descuento por hermano en el colegio'),
(9, 29, 2, 315000, 'Descuento por hermano en el colegio'),
(10, 30, 2, 315000, 'Descuento por hermano en el colegio');

INSERT INTO cargos_estudiante (id_cargo, id_estudiante, id_concepto, año_academico, periodo_academico, monto, fecha_vencimiento, estado, observaciones) VALUES
(1, 1, 1, 2026, 'Anual', 500000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(2, 2, 2, 2026, 'I', 350000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(3, 3, 3, 2026, 'I', 250000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(4, 4, 4, 2026, 'I', 150000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(5, 5, 5, 2026, 'I', 120000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(6, 6, 6, 2026, 'I', 50000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(7, 7, 7, 2026, 'I', 15000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(8, 8, 1, 2026, 'Anual', 500000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(9, 9, 2, 2026, 'I', 350000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(10, 10, 3, 2026, 'I', 250000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(11, 11, 4, 2026, 'I', 150000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(12, 12, 5, 2026, 'I', 120000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(13, 13, 6, 2026, 'I', 50000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(14, 14, 7, 2026, 'I', 15000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(15, 15, 1, 2026, 'Anual', 500000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(16, 16, 2, 2026, 'I', 350000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(17, 17, 3, 2026, 'I', 250000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(18, 18, 4, 2026, 'I', 150000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(19, 19, 5, 2026, 'I', 120000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(20, 20, 6, 2026, 'I', 50000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(21, 21, 7, 2026, 'I', 15000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(22, 22, 1, 2026, 'Anual', 500000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(23, 23, 2, 2026, 'I', 315000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(24, 24, 3, 2026, 'I', 250000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(25, 25, 4, 2026, 'I', 150000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(26, 26, 5, 2026, 'I', 120000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(27, 27, 6, 2026, 'I', 50000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(28, 28, 7, 2026, 'I', 15000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(29, 29, 1, 2026, 'Anual', 500000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente'),
(30, 30, 2, 2026, 'I', 315000, '2026-10-15', 'Pendiente', 'Cargo generado automáticamente');

INSERT INTO pagos (id_pago, id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) VALUES
(1, 1, 1, 1, 300000, '2026-10-02', 'Efectivo', 'REC-2026-00001', 2026, 'Anual', 'Pago registrado en caja'),
(2, 2, 2, 2, 244999, '2026-10-03', 'Transferencia', 'REC-2026-00002', 2026, 'I', 'Pago registrado en caja'),
(3, 3, 3, 3, 200000, '2026-10-04', 'Tarjeta Débito', 'REC-2026-00003', 2026, 'I', 'Pago registrado en caja'),
(4, 4, 4, 4, 135000, '2026-10-05', 'Tarjeta Crédito', 'REC-2026-00004', 2026, 'I', 'Pago registrado en caja'),
(5, 5, 5, 5, 120000, '2026-10-06', 'Cheque', 'REC-2026-00005', 2026, 'I', 'Pago registrado en caja'),
(6, 6, 6, 6, 55000, '2026-10-07', 'Efectivo', 'REC-2026-00006', 2026, 'I', 'Pago registrado en caja'),
(7, 7, 7, 7, 9000, '2026-10-08', 'Transferencia', 'REC-2026-00007', 2026, 'I', 'Pago registrado en caja'),
(8, 8, 8, 1, 350000, '2026-10-09', 'Tarjeta Débito', 'REC-2026-00008', 2026, 'Anual', 'Pago registrado en caja'),
(9, 9, 9, 2, 280000, '2026-10-10', 'Tarjeta Crédito', 'REC-2026-00009', 2026, 'I', 'Pago registrado en caja'),
(10, 10, 10, 3, 225000, '2026-10-11', 'Cheque', 'REC-2026-00010', 2026, 'I', 'Pago registrado en caja'),
(11, 11, 11, 4, 150000, '2026-10-12', 'Efectivo', 'REC-2026-00011', 2026, 'I', 'Pago registrado en caja'),
(12, 12, 12, 5, 132000, '2026-10-13', 'Transferencia', 'REC-2026-00012', 2026, 'I', 'Pago registrado en caja'),
(13, 13, 13, 6, 30000, '2026-10-14', 'Tarjeta Débito', 'REC-2026-00013', 2026, 'I', 'Pago registrado en caja'),
(14, 14, 14, 7, 10500, '2026-10-15', 'Tarjeta Crédito', 'REC-2026-00014', 2026, 'I', 'Pago registrado en caja'),
(15, 15, 15, 1, 400000, '2026-10-16', 'Cheque', 'REC-2026-00015', 2026, 'Anual', 'Pago registrado en caja'),
(16, 16, 16, 2, 315000, '2026-10-17', 'Efectivo', 'REC-2026-00016', 2026, 'I', 'Pago registrado en caja'),
(17, 17, 17, 3, 250000, '2026-10-18', 'Transferencia', 'REC-2026-00017', 2026, 'I', 'Pago registrado en caja'),
(18, 18, 18, 4, 165000, '2026-10-19', 'Tarjeta Débito', 'REC-2026-00018', 2026, 'I', 'Pago registrado en caja'),
(19, 19, 19, 5, 72000, '2026-10-20', 'Tarjeta Crédito', 'REC-2026-00019', 2026, 'I', 'Pago registrado en caja'),
(20, 20, 20, 6, 35000, '2026-10-21', 'Cheque', 'REC-2026-00020', 2026, 'I', 'Pago registrado en caja'),
(21, 21, 21, 7, 12000, '2026-10-22', 'Efectivo', 'REC-2026-00021', 2026, 'I', 'Pago registrado en caja'),
(22, 22, 22, 1, 450000, '2026-10-23', 'Transferencia', 'REC-2026-00022', 2026, 'Anual', 'Pago registrado en caja'),
(23, 23, 23, 2, 315000, '2026-10-24', 'Tarjeta Débito', 'REC-2026-00023', 2026, 'I', 'Pago registrado en caja'),
(24, 24, 24, 3, 275000, '2026-10-25', 'Tarjeta Crédito', 'REC-2026-00024', 2026, 'I', 'Pago registrado en caja'),
(25, 25, 25, 4, 90000, '2026-10-26', 'Cheque', 'REC-2026-00025', 2026, 'I', 'Pago registrado en caja'),
(26, 26, 26, 5, 84000, '2026-10-27', 'Efectivo', 'REC-2026-00026', 2026, 'I', 'Pago registrado en caja'),
(27, 27, 27, 6, 40000, '2026-10-28', 'Transferencia', 'REC-2026-00027', 2026, 'I', 'Pago registrado en caja'),
(28, 28, 28, 7, 13500, '2026-10-01', 'Tarjeta Débito', 'REC-2026-00028', 2026, 'I', 'Pago registrado en caja'),
(29, 29, 29, 1, 500000, '2026-10-02', 'Tarjeta Crédito', 'REC-2026-00029', 2026, 'Anual', 'Pago registrado en caja'),
(30, 30, 30, 2, 346500, '2026-10-03', 'Cheque', 'REC-2026-00030', 2026, 'I', 'Pago registrado en caja');

INSERT INTO recursos (id_recurso, codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES
(1, 'REC-001', 'Libro: Cien Años de Soledad', 1, 'Recurso institucional: Libro: Cien Años de Soledad', '2024-03-01', 'Biblioteca', 'Prestado'),
(2, 'REC-002', 'Libro: El Principito', 2, 'Recurso institucional: Libro: El Principito', '2024-03-01', 'Laboratorio 1', 'Prestado'),
(3, 'REC-003', 'Libro: Historia de Colombia', 3, 'Recurso institucional: Libro: Historia de Colombia', '2024-03-01', 'Laboratorio 2', 'Prestado'),
(4, 'REC-004', 'Libro: Física Conceptual', 4, 'Recurso institucional: Libro: Física Conceptual', '2024-03-01', 'Gimnasio', 'Prestado'),
(5, 'REC-005', 'Libro: Química General', 5, 'Recurso institucional: Libro: Química General', '2024-03-01', 'Sala de Tecnología', 'Prestado'),
(6, 'REC-006', 'Microscopio Óptico 01', 1, 'Recurso institucional: Microscopio Óptico 01', '2024-03-01', 'Biblioteca', 'Prestado'),
(7, 'REC-007', 'Microscopio Óptico 02', 2, 'Recurso institucional: Microscopio Óptico 02', '2024-03-01', 'Laboratorio 1', 'Prestado'),
(8, 'REC-008', 'Balanza de Precisión', 3, 'Recurso institucional: Balanza de Precisión', '2024-03-01', 'Laboratorio 2', 'Prestado'),
(9, 'REC-009', 'Kit de Laboratorio 01', 4, 'Recurso institucional: Kit de Laboratorio 01', '2024-03-01', 'Gimnasio', 'Prestado'),
(10, 'REC-010', 'Kit de Laboratorio 02', 5, 'Recurso institucional: Kit de Laboratorio 02', '2024-03-01', 'Sala de Tecnología', 'Prestado'),
(11, 'REC-011', 'Balón de Fútbol', 1, 'Recurso institucional: Balón de Fútbol', '2024-03-01', 'Biblioteca', 'Prestado'),
(12, 'REC-012', 'Balón de Baloncesto', 2, 'Recurso institucional: Balón de Baloncesto', '2024-03-01', 'Laboratorio 1', 'Prestado'),
(13, 'REC-013', 'Balón de Voleibol', 3, 'Recurso institucional: Balón de Voleibol', '2024-03-01', 'Laboratorio 2', 'Prestado'),
(14, 'REC-014', 'Raqueta de Tenis 01', 4, 'Recurso institucional: Raqueta de Tenis 01', '2024-03-01', 'Gimnasio', 'Prestado'),
(15, 'REC-015', 'Raqueta de Tenis 02', 5, 'Recurso institucional: Raqueta de Tenis 02', '2024-03-01', 'Sala de Tecnología', 'Prestado'),
(16, 'REC-016', 'Tablet Samsung 01', 1, 'Recurso institucional: Tablet Samsung 01', '2024-03-01', 'Biblioteca', 'Prestado'),
(17, 'REC-017', 'Tablet Samsung 02', 2, 'Recurso institucional: Tablet Samsung 02', '2024-03-01', 'Laboratorio 1', 'Prestado'),
(18, 'REC-018', 'Computador Portátil 01', 3, 'Recurso institucional: Computador Portátil 01', '2024-03-01', 'Laboratorio 2', 'Prestado'),
(19, 'REC-019', 'Calculadora Científica 01', 4, 'Recurso institucional: Calculadora Científica 01', '2024-03-01', 'Gimnasio', 'Prestado'),
(20, 'REC-020', 'Calculadora Científica 02', 5, 'Recurso institucional: Calculadora Científica 02', '2024-03-01', 'Sala de Tecnología', 'Prestado'),
(21, 'REC-021', 'Proyector Portátil 01', 1, 'Recurso institucional: Proyector Portátil 01', '2024-03-01', 'Biblioteca', 'Disponible'),
(22, 'REC-022', 'Cámara Fotográfica 01', 2, 'Recurso institucional: Cámara Fotográfica 01', '2024-03-01', 'Laboratorio 1', 'Disponible'),
(23, 'REC-023', 'Kit de Robótica 01', 3, 'Recurso institucional: Kit de Robótica 01', '2024-03-01', 'Laboratorio 2', 'Disponible'),
(24, 'REC-024', 'Kit de Robótica 02', 4, 'Recurso institucional: Kit de Robótica 02', '2024-03-01', 'Gimnasio', 'Disponible'),
(25, 'REC-025', 'Mapa de Colombia', 5, 'Recurso institucional: Mapa de Colombia', '2024-03-01', 'Sala de Tecnología', 'Disponible'),
(26, 'REC-026', 'Modelo del Sistema Solar', 1, 'Recurso institucional: Modelo del Sistema Solar', '2024-03-01', 'Biblioteca', 'Disponible'),
(27, 'REC-027', 'Instrumento Musical 01', 2, 'Recurso institucional: Instrumento Musical 01', '2024-03-01', 'Laboratorio 1', 'Disponible'),
(28, 'REC-028', 'Instrumento Musical 02', 3, 'Recurso institucional: Instrumento Musical 02', '2024-03-01', 'Laboratorio 2', 'Disponible'),
(29, 'REC-029', 'Equipo de Sonido 01', 4, 'Recurso institucional: Equipo de Sonido 01', '2024-03-01', 'Gimnasio', 'En Mantenimiento'),
(30, 'REC-030', 'Televisor 01', 5, 'Recurso institucional: Televisor 01', '2024-03-01', 'Sala de Tecnología', 'Dado de Baja');

INSERT INTO prestamos_estudiante (id_prestamo, id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(1, 1, 1, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(2, 2, 2, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(3, 3, 3, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(4, 4, 4, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(5, 5, 5, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(6, 6, 6, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(7, 7, 7, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(8, 8, 8, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(9, 9, 9, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante'),
(10, 10, 10, '2026-10-01', '2026-10-20', NULL, 'Activo', 'Préstamo a estudiante');

INSERT INTO prestamos_profesor (id_prestamo, id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(1, 11, 1, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(2, 12, 2, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(3, 13, 3, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(4, 14, 4, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(5, 15, 5, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(6, 16, 6, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(7, 17, 7, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(8, 18, 8, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(9, 19, 9, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor'),
(10, 20, 10, '2026-10-05', '2026-12-15', NULL, 'Activo', 'Préstamo a profesor');

-- Inicializa los consecutivos con los datos de prueba ya cargados
INSERT INTO secuencias_financieras (nombre_secuencia, año, ultimo_valor)
VALUES ('recibo', 2026, 30)
ON DUPLICATE KEY UPDATE ultimo_valor = GREATEST(ultimo_valor, 30);

INSERT INTO secuencias_financieras (nombre_secuencia, año, ultimo_valor)
VALUES ('factura', 2026, 0)
ON DUPLICATE KEY UPDATE ultimo_valor = GREATEST(ultimo_valor, 0);

-- ============================================
-- VERIFICATION
-- ============================================
SELECT 'estudiantes' AS tabla, COUNT(*) AS registros FROM estudiantes
UNION ALL SELECT 'acudientes', COUNT(*) FROM acudientes
UNION ALL SELECT 'profesores', COUNT(*) FROM profesores
UNION ALL SELECT 'materias', COUNT(*) FROM materias
UNION ALL SELECT 'registros_academicos', COUNT(*) FROM registros_academicos
UNION ALL SELECT 'pagos', COUNT(*) FROM pagos
UNION ALL SELECT 'clubes', COUNT(*) FROM clubes
UNION ALL SELECT 'recursos', COUNT(*) FROM recursos
UNION ALL SELECT 'secuencias_financieras', COUNT(*) FROM secuencias_financieras;
