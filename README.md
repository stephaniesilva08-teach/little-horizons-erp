# Little Horizons ERP

Sistema de gestión educativa por consola para el **Colegio Internacional Little Horizons**.
Centraliza estudiantes, acudientes, matrículas, profesores, materias, notas, asistencia,
clubes, eventos, pagos, facturación y préstamos de recursos en una sola aplicación Java
conectada a MySQL.

---

## 1. Título del Proyecto

**Little Horizons ERP — Sistema de Gestión Educativa**

Proyecto académico de Java + MySQL que implementa una arquitectura MVC con capa DAO,
transacciones, validaciones, logs y generación de facturas electrónicas en formato de
texto (`.txt`).

---

## 2. Descripción

### Propósito

Reemplazar los registros en papel y las hojas de cálculo dispersas del colegio por un
sistema único, consistente y auditable que cubra el ciclo académico y administrativo de
la institución.

### Problemas que resuelve

- Pérdida y duplicación de información académica en cuadernos y archivos aislados.
- Dificultad para consultar notas, promedios, asistencia e historial de un estudiante.
- Falta de control sobre cupos de clubes, participación en eventos y préstamos de recursos.
- Procesos manuales de facturación lentos, con errores de cálculo y sin trazabilidad.
- Imposibilidad de identificar estudiantes morosos o con inasistencia recurrente.
- Asignación desorganizada de profesores, materias, secciones y horarios.

### Funcionalidades principales

- CRUD de estudiantes, acudientes, profesores, materias, matrículas, clubes, eventos,
  conceptos de pago, cargos y recursos.
- Asociación de estudiantes con uno o varios acudientes (incluidos hermanos).
- Matrícula anual por grado y consulta por estudiante o por año.
- Registro de notas por materia, período y profesor, con promedios por período,
  por materia y general.
- Control de asistencia diaria por materia y alertas de inasistencia recurrente.
- Inscripción de estudiantes en materias y en clubes con validación de cupo.
- Registro de participantes en eventos y seguimiento de posiciones.
- Registro de pagos con recibos consecutivos y generación automática de factura `.txt`
  con datos estilo DIAN (resolución simulada, CUFE simulado y payload QR).
- Valores personalizados por estudiante (descuentos, becas, pensiones diferenciales).
- Préstamos de recursos a estudiantes y profesores, con devolución y control de estado.
- Reportes de promedios, boletines, resumen de notas, asistencia, morosos, préstamos
  vencidos y resumen de matrícula.

---

## 3. Autores

| Nombre completo | Correo electrónico | Contacto |
|---|---|---|
| Stephanie Silva | nathaliasilva0816@gmail.com | https://github.com/stephaniesilva08-teach/little-horizons-erp |

---

## 4. Tecnologías Utilizadas

| Tecnología | Versión / uso |
|---|---|
| Java | 17 o superior (probado con OpenJDK 21) |
| IDE | Cualquiera compatible con Java (IntelliJ IDEA, VS Code, NetBeans, Eclipse) |
| MySQL | 8.0 o superior (probado con MySQL 8.0.46) |
| MySQL Connector/J | 8.4.0 |
| Maven (opcional) | 3.9+ para compilar y empaquetar con `pom.xml` |
| Interfaz | Consola de texto (`System.console`/`Scanner`) |
| Librerías externas | Solo el driver JDBC de MySQL |

El código Java (clases, métodos, variables y comentarios) está en inglés. Los mensajes
de consola dirigidos al usuario y los nombres de columnas SQL están en español.

---

## 5. Requerimientos Previos

- **JDK 17 o superior** instalado y disponible en `PATH` (`java -version`, `javac -version`).
- **MySQL Server 8.0 o superior** en ejecución. Es un requisito obligatorio porque el
  esquema utiliza `CHECK`, `ENUM`, `JSON`/`TIMESTAMP` y otras características de MySQL 8.
- Usuario de MySQL con permisos para crear la base de datos `erp_little_horizons`.
- Terminal o consola (Bash, PowerShell, CMD).
- Opcional: Maven 3.9+ si se desea usar `pom.xml`.

> **Importante:** el proyecto no funciona con MySQL 5.7 ni con MariaDB sin adaptaciones,
> debido al script DDL entregado.

---

## 6. Configuración de la Base de Datos

### 6.1. Creación de la base de datos

El script `docs/database.sql` contiene el DDL completo y los datos de prueba (mínimo 30
registros en las tablas principales). Al inicio ejecuta `DROP DATABASE IF EXISTS
erp_little_horizons`, por lo que **reemplaza** cualquier versión existente.

```bash
mysql -u root -p < docs/database.sql
```

También se puede ejecutar desde MySQL Workbench o DBeaver abriendo el archivo y
ejecutándolo completo. Al final, el script muestra un conteo de verificación por tabla.

### 6.2. Configuración de credenciales

La aplicación usa por defecto:

| Parámetro | Valor |
|---|---|
| Base de datos | `erp_little_horizons` |
| Usuario | `nathalia123` |
| Contraseña | `1234` |
| Host / puerto | `localhost:3306` |

Si el usuario no existe, se puede crear con:

```sql
CREATE USER IF NOT EXISTS 'nathalia123'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON erp_little_horizons.* TO 'nathalia123'@'localhost';
FLUSH PRIVILEGES;
```

Para usar otras credenciales, definir variables de entorno antes de ejecutar:

```bash
export ERP_DB_URL='jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8'
export ERP_DB_USER='mi_usuario'
export ERP_DB_PASSWORD='mi_clave'
```

En Windows CMD:

```bat
set ERP_DB_URL=jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
set ERP_DB_USER=mi_usuario
set ERP_DB_PASSWORD=mi_clave
```

Si no se definen, `ConnectionFactory` aplica los valores por defecto indicados.

### 6.3. Instalación del driver JDBC

El driver ya está incluido en `lib/mysql-connector-j-8.4.0.jar`. Inclúyelo en el
classpath al compilar y al ejecutar.

Alternativa:

- Con Maven, la dependencia se descarga automáticamente desde `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.4.0</version>
</dependency>
```

---

## 7. Instalación y Ejecución

### 7.1. Clonar el repositorio

```bash
git clone https://github.com/stephaniesilva08-teach/little-horizons-erp.git
cd little-horizons-erp
```

### 7.2. Abrir el proyecto en el IDE

- Abrir la carpeta raíz como proyecto Java.
- Verificar que el JDK del proyecto sea 17 o superior.
- Agregar `lib/mysql-connector-j-8.4.0.jar` al classpath si el IDE no lo detecta.
- Alternativa Maven: abrir `pom.xml` como proyecto Maven.

### 7.3. Compilar

Desde la raíz del proyecto, compila todas las fuentes en `out/`:

```bash
mkdir -p out
javac --release 17 -encoding UTF-8 -cp lib/mysql-connector-j-8.4.0.jar \
      -d out $(find src -name "*.java")
```

**Windows (PowerShell/CMD):**

```bat
javac --release 17 -encoding UTF-8 -cp lib\mysql-connector-j-8.4.0.jar -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName })
```

**Alternativa Maven:**

```bash
mvn clean package
```

### 7.4. Ejecutar

```bash
java -cp "out:lib/mysql-connector-j-8.4.0.jar" com.littlehorizons.erp.Main
```

En Windows el separador del classpath es `;`:

```bat
java -cp "out;lib\mysql-connector-j-8.4.0.jar" com.littlehorizons.erp.Main
```

**Alternativa Maven:**

```bash
mvn exec:java
# o, tras mvn clean package:
java -jar target/erp-little-horizons-1.0.0.jar
```

La aplicación verifica la conexión antes de mostrar el menú principal. Las facturas se
crean en la carpeta `facturas/` del directorio desde el cual se ejecuta la aplicación.

---

## 8. Estructura del Proyecto

### Arquitectura MVC + DAO

```
Little-horizons-erp/
├── docs/
│   └── database.sql                 # DDL + DML (fuente de verdad del esquema)
├── facturas/                        # Facturas .txt generadas en tiempo de ejecución
│   └── factura_FAC-2026-00001.txt   # Muestra de factura generada
├── lib/
│   └── mysql-connector-j-8.4.0.jar  # Driver JDBC
├── src/
│   └── com/littlehorizons/erp/
│       ├── Main.java                # Punto de entrada
│       ├── model/                   # Entidades del dominio y DTOs (records)
│       ├── dao/                     # Acceso a datos con JDBC/PreparedStatement
│       ├── controller/              # Reglas de negocio y validaciones
│       ├── view/                    # Menús e interfaz de consola
│       └── util/                    # Conexión, validación, logs, facturas .txt, consola
├── pom.xml
└── README.md
```

### Descripción de cada paquete

| Paquete | Responsabilidad |
|---|---|
| `model` | Clases de dominio (`Student`, `Parent`, `Teacher`, `Subject`, `Payment`, etc.) y records para reportes (`CourseAverage`, `Debtor`, `OverdueLoan`, etc.). |
| `dao` | SQL, mapeo `ResultSet` → objeto, transacciones y acceso a MySQL. Ningún DAO imprime por consola. |
| `controller` | Validaciones, reglas de negocio, coordinación entre DAOs y generación de facturas. |
| `view` | Menús, lectura de datos, tablas de salida y mensajes al usuario. |
| `util` | `ConnectionFactory` (Singleton), `Validator`, `LoggerUtil`, `ConsoleUtils`, `TextInvoiceGenerator`, `FileUtil`, `DataAccessException`. |

Flujo típico:

```
View → Controller → DAO → MySQL
  ↑         ↓
  └── mensajes / errores
```

---

## 9. Principios y Patrones de Diseño Aplicados

### 9.1. Principios SOLID

- **S — Single Responsibility:** cada clase tiene una responsabilidad. `StudentDao` solo
  accede a datos; `StudentController` solo valida y aplica reglas; `StudentView` solo
  interactúa con el usuario.
- **O — Open/Closed:** se pueden agregar nuevos reportes o DAOs sin modificar los
  existentes; los records de reporte permiten nuevas consultas sin romper contratos.
- **L — Liskov Substitution:** los modelos son sustituibles por sus abstracciones y los
  records de reporte son inmutables y consistentes.
- **I — Interface Segregation:** los controladores exponen únicamente las operaciones que
  cada vista necesita; no se obliga a una vista a depender de métodos que no usa.
- **D — Dependency Inversion:** las capas superiores dependen de controladores/DAOs con
  responsabilidades claras y de `ConnectionFactory` como abstracción de configuración,
  no de credenciales embebidas en la lógica.

### 9.2. Patrones de Diseño

- **MVC (Model-View-Controller):** separación estricta entre dominio, interfaz y lógica.
- **DAO (Data Access Object):** todo el SQL vive en `dao/`; facilita mantenimiento y
  pruebas.
- **Singleton:** `ConnectionFactory` mantiene una única configuración de conexión.
- **Factory / utilidades estáticas:** `TextInvoiceGenerator.generate(...)` encapsula la
  creación de facturas y devuelve un `GeneratedInvoice` inmutable.
- **DTO con `record`:** reportes y proyecciones (`CourseAverage`, `Debtor`,
  `AttendanceSummary`, etc.) usan records de Java 17.
- **Template Method implícito:** los DAO comparten `DaoSupport` para mapeo de fechas,
  nulos y llaves generadas.

### 9.3. Buenas prácticas aplicadas

- `PreparedStatement` en todas las consultas para prevenir inyección SQL.
- `try-with-resources` para `Connection`, `PreparedStatement` y `ResultSet`.
- Transacciones con `setAutoCommit(false)`, `commit()` y `rollback()` en pagos/facturas
  y en préstamos/devoluciones.
- Stream API y lambdas en la resolución de valores personalizados y en las vistas.
- Validación centralizada con `Validator` y excepciones de negocio claras.
- Logs en `error_log.txt` con nivel, fecha, mensaje y traza completa.

---

## 10. Funcionalidades Principales

### 10.1. Estudiantes y acudientes

- Registro, búsqueda por documento o nombre, actualización y eliminación.
- Datos personales, contacto, tipo de sangre, alergias y condiciones médicas.
- Asociación de varios acudientes por estudiante, con acudiente principal y contacto de
  emergencia.
- Un acudiente puede tener varios estudiantes a cargo.

### 10.2. Matrículas y materias

- Matrícula anual por grado con estado (`Activa`, `Completada`, `Retirada`).
- Consulta por estudiante, por año o listado general.
- Catálogo de materias por grado con código, horas semanales y estado.
- Inscripción de estudiantes en materias por año, período y sección.

### 10.3. Profesores y carga académica

- CRUD de profesores con especialización, fecha de contratación y estado.
- Asignaciones profesor–materia por año y sección.
- Horarios semanales por asignación (`Lunes` a `Sábado`, hora inicio/fin).
- Validación de que la hora final sea posterior a la inicial.

### 10.4. Notas y asistencia

- Registro de notas de 0.0 a 5.0 por estudiante, materia, profesor, año y período.
- Promedio por período, promedio general y boletín detallado.
- Asistencia por materia con estados `Presente`, `Ausente`, `Tardanza` y
  `Excusa Médica`.
- Resumen de asistencia por año y alertas de inasistencia recurrente configurable.

### 10.5. Clubes y eventos

- CRUD de clubes con supervisor, capacidad máxima, horario y estado.
- Inscripción de estudiantes con validación de cupo máximo.
- Retiro de clubes con motivo y fecha.
- CRUD de eventos académicos, culturales, deportivos, sociales y científicos.
- Registro de participantes, rol, posición obtenida y observaciones.

### 10.6. Módulo financiero y facturación

- CRUD de conceptos de pago con monto base, recurrencia y estado.
- Valores personalizados por estudiante para descuentos o becas.
- Cargos por estudiante con vencimiento, período y estado.
- Registro de pagos con estudiante, acudiente, concepto, monto, fecha, método y recibo.
- Generación automática de factura `.txt` en `facturas/` y registro en la tabla
  `facturas`.
- La inserción del pago y su factura es **atómica**: si falla la factura, se revierte el
  pago.
- La factura incluye datos del colegio, NIT, resolución simulada, número consecutivo,
  datos del cliente, detalle, subtotal, IVA (19 % cuando aplica), total, CUFE simulado y
  payload QR simulado.

Ejemplo de factura generada:

```text
==========================================================
              FACTURA ELECTRÓNICA - LITTLE HORIZONS
==========================================================
Colegio: Colegio Internacional Little Horizons S.A.S.
NIT: 8927456321
Dirección: Calle 12 #34-56, Bogotá, Colombia
Teléfono: +57 1 234 5678
Correo: soporte@littlehorizons.edu.co
Resolución de habilitación: 001-2026 (simulada)
----------------------------------------------------------
Número de factura: FAC-2026-00001
Fecha de emisión: 2026-11-11
----------------------------------------------------------
INFORMACIÓN DEL CLIENTE
Acudiente responsable: Carlos Andres Garcia Lopez
Documento: 1020304050
Estudiante: Sofia Valentina Garcia Rodriguez
----------------------------------------------------------
DETALLE
Descripción                       Cant.     Valor unit.        Subtotal
Pension Mensual                       1     $350.000,00     $350.000,00
----------------------------------------------------------
Subtotal:                                                   $350.000,00
IVA (19%):                                                        $0,00
TOTAL:                                                      $350.000,00
----------------------------------------------------------
CUMPLIMIENTO DIAN (SIMULADO)
CUFE: be77c0904ce4ad54d88989692175c4723f9e2e8fe4b6252f766069e372630bcc
QR: NIT=8927456321|FACTURA=FAC-2026-00001|FECHA=2026-11-11|TOTAL=350000.00|CUFE=be77c0904ce4ad54d88989692175c4723f9e2e8fe4b6252f766069e372630bcc
==========================================================
Gracias por su pago.
```

> El formato es `.txt` por solicitud expresa del cliente para esta entrega. Se conservan
> todos los datos de una factura electrónica (resolución DIAN, CUFE, QR, desglose de impuestos).

### 10.7. Recursos y préstamos

- Categorías de recursos e inventario con código, ubicación y estado.
- Préstamos a estudiantes y profesores con fecha esperada de devolución.
- Cambio automático de estado del recurso: `Disponible → Prestado → Disponible`.
- Devolución transaccional y registro de fecha real.
- Reporte de préstamos vencidos.

### 10.8. Reportes

- Promedio por curso/materia/sección.
- Boletín individual de notas.
- Resumen de notas por materia y período (promedio, máxima, mínima, registros).
- Resumen de asistencia.
- Reporte de estudiantes morosos (cargado, pagado, saldo).
- Préstamos vencidos.
- Resumen de matrícula por grado.

---

## 11. Manejo de Errores y Logs

### Sistema de logging

- Implementado en `util/LoggerUtil.java`.
- Archivo: **`error_log.txt`** en el directorio de ejecución.
- Niveles: `INFO`, `WARN`, `DEBUG`, `ERROR`.
- Cada entrada incluye fecha/hora, nivel y mensaje. Los errores incluyen la traza
  completa de la excepción, escrita en el archivo (no se imprime la traza en consola).

### Estrategia de excepciones

| Excepción | Uso |
|---|---|
| `IllegalArgumentException` | Validaciones de negocio y datos de entrada. |
| `DataAccessException` | Errores de SQL, conexión o transacciones; envuelve la causa original. |
| `SQLException` | Se captura en los DAO y se transforma en `DataAccessException`. |

- Las vistas capturan `IllegalArgumentException` y `DataAccessException` mediante
  `ViewUtils.execute(...)` y muestran un mensaje claro al usuario.
- Las transacciones hacen `rollback()` ante cualquier fallo y restauran
  `autoCommit = true`.
- Si la entrada estándar se cierra (`Ctrl-D`), `ConsoleUtils.readLine` finaliza la
  aplicación de forma ordenada en lugar de entrar en un bucle.
- Los errores de conexión al inicio muestran la URL configurada y recomendaciones.

---

## 12. Capturas de Pantalla (opcional)

Evidencia de ejecución en consola:

```text
==========================================================
   COLEGIO LITTLE HORIZONS - ERP DE GESTIÓN EDUCATIVA
==========================================================
  Conexión a la base de datos establecida.
  Base de datos: erp_little_horizons

==========================================================
  COLEGIO LITTLE HORIZONS - SISTEMA DE GESTIÓN
==========================================================
  1. Estudiantes
  2. Acudientes
  3. Profesores
  4. Materias
  5. Matrículas
  6. Módulo académico
  7. Actividades extracurriculares
  8. Módulo financiero y facturación
  9. Recursos y préstamos
 10. Reportes
  0. Salir
Seleccione una opción:
```

```text
  PROMEDIO POR CURSO
----------------------------------------------------------
GRADO  MATERIA                      SECCIÓN        PROMEDIO   ESTUDIANTES
----------------------------------------------------------
4      Artes                        A              4.23333    3
10     Cátedra de Paz               A              4.32500    2
3      Ciencias Sociales            B              4.60000    3
...
```

---

## 13. Problemas Conocidos y Limitaciones

- La factura electrónica visual se genera en formato `.txt` con todos los elementos de cumplimiento fiscal DIAN (simulados para fines académicos).
- Los nombres de tablas y columnas SQL están en español porque provienen del esquema
  oficial del proyecto; el código Java sí está completamente en inglés.
- No se implementó autenticación de usuarios ni control de roles; el acceso es local.

---

## 14. Futuras Mejoras

- Firma digital mediante certificado DIAN y envío del archivo `.txt` firmado.
- Interfaz gráfica (JavaFX/Swing) o API REST con Spring Boot.
- Autenticación con roles (administrador, profesor, acudiente).
- Envío automático de facturas y boletines por correo electrónico.
- Dashboard web con indicadores académicos y financieros.
- Importación/exportación de datos en CSV y Excel.
- Notificaciones automáticas de morosidad e inasistencia.
- Migración del esquema a Flyway/Liquibase.

---

## 15. Licencia

Proyecto desarrollado con fines **académicos y educativos**. Su uso, modificación y
distribución deben respetar las condiciones del curso y la institución. No se ofrece
garantía comercial ni soporte oficial.

---

## 16. Contacto

- **Autora:** Stephanie Silva
- **Correo personal:** nathaliasilva0816@gmail.com
- **Institución:** Colegio Internacional Little Horizons S.A.S.
- **Correo institucional:** soporte@littlehorizons.edu.co
- **Teléfono:** +57 1 234 5678
- **Dirección:** Calle 12 #34-56, Bogotá, Colombia
- **Repositorio:** [https://github.com/stephaniesilva08-teach/little-horizons-erp](https://github.com/stephaniesilva08-teach/little-horizons-erp) (repositorio privado con el trainer como colaborador).

---

## Anexo A — Verificación del Sistema

La verificación funcional se realiza ejecutando la aplicación directamente desde la consola:

```bash
java -cp "out:lib/mysql-connector-j-8.4.0.jar" com.littlehorizons.erp.Main
```

El flujo valida:

1. Conexión exitosa a la base de datos `erp_little_horizons`.
2. Menú principal interactivo con navegación entre los 7 módulos del sistema.
3. Operaciones CRUD completas para estudiantes, acudientes, profesores y materias.
4. Registro de pagos y generación automática de la factura electrónica en formato `.txt` (`facturas/factura_*.txt`).
5. Préstamo y devolución de recursos con actualización atómica de inventario.
6. Generación de reportes de notas, promedios, inasistencias y morosidad.

## Anexo B — Cambios justificados al esquema

Además del script base se agregaron los siguientes elementos, todos documentados en
`docs/database.sql`:

- Tabla `horarios` para la programación semanal de asignaciones.
- Tabla `conceptos_estudiante` para valores personalizados por estudiante.
- Tabla `cargos_estudiante` para obligaciones y reporte de saldos/morosos.
- Restricción única `(id_estudiante, id_club)` en `membresias_club` para evitar
  inscripciones duplicadas en un mismo club.
- Restricción única `id_pago` en `facturas` para garantizar una factura por pago.
- Restricciones `CHECK` adicionales para montos, notas y años académicos.
- La tabla `facturas` permanece vacía en el script; las facturas se generan desde la
  aplicación para mantener la trazabilidad con cada pago real.
