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
| MySQL | 8.0.16 o superior (probado con MySQL 8.0.46; esta versión aplica `CHECK`) |
| MySQL Connector/J | 8.4.0 |
| Maven (opcional) | 3.9+ para compilar y empaquetar con `pom.xml` |
| Interfaz | Consola de texto (`System.console`/`Scanner`) |
| Librerías externas | Solo el driver JDBC de MySQL |

El código Java (clases, métodos, variables y comentarios) está en inglés. Los mensajes
de consola dirigidos al usuario y los nombres de columnas SQL están en español.

---

## 5. Requerimientos Previos

- **JDK 17 o superior** instalado y disponible en `PATH` (`java -version`, `javac -version`).
- **MySQL Server 8.0.16 o superior** en ejecución. El esquema utiliza `CHECK`; MySQL
  empezó a aplicar esas restricciones desde la versión 8.0.16.
- Usuario de MySQL con permisos para crear la base de datos `erp_little_horizons`.
- Terminal o consola (Bash, PowerShell, CMD).
- Opcional: Maven 3.9+ si se desea usar `pom.xml`.

> **Importante:** el proyecto no funciona con MySQL 5.7 ni con MariaDB sin adaptaciones,
> debido al script DDL entregado.

---

## 6. Configuración de la Base de Datos

### 6.1. Creación de la base de datos

El script `docs/database.sql` contiene el DDL completo y los datos de prueba (mínimo 30
registros en las tablas principales). Crea `erp_little_horizons` si no existe y no borra
la base de datos ni sus datos. Está pensado para una base vacía: si las tablas ya existen,
MySQL detendrá la ejecución al encontrar nombres duplicados; para reinstalar desde cero,
respalde los datos y elimine la base manualmente antes de ejecutar el script.

```bash
mysql -u root -p < docs/database.sql
```

También se puede ejecutar desde MySQL Workbench o DBeaver abriendo el archivo y
ejecutándolo completo. Al final, el script muestra un conteo de verificación por tabla.

Si ya existe la base de datos y las tablas del ERP, no vuelva a ejecutar el script completo.
Aplique las migraciones pendientes una sola vez y en orden: `001_system_users.sql` para
las cuentas, `002_financial_sequences.sql` para reconciliar consecutivos y
`003_payment_charge_allocations.sql` para guardar las aplicaciones pago-cargo y convertir
pagos históricos en asignaciones FIFO con créditos no aplicados explícitos. Ejecute las
migraciones sobre `erp_little_horizons` con la aplicación detenida y después de respaldar
la base de datos. Antes de `003`, concilie los cargos que se marcaron manualmente como
`Pagado`: la migración deriva el estado de las asignaciones reconstruidas desde pagos
registrados, por lo que un cargo marcado como pagado sin pago histórico suficiente quedará
`Pendiente` y requiere conciliación contable previa.

### 6.2. Configuración de credenciales

La configuración local se lee desde `.env` en la carpeta raíz del proyecto. Ese
archivo está excluido de Git. Crea uno con estas entradas y ajusta los valores a tu
instalación:

```properties
ERP_DB_USER=erp_user
ERP_DB_PASSWORD=una_clave_segura
ERP_DB_URL=jdbc:mysql://localhost:3306/erp_little_horizons?sslMode=VERIFY_IDENTITY&serverTimezone=UTC&characterEncoding=UTF-8
```

La aplicación también acepta variables de entorno cuando una clave no está definida
en `.env`.

| Variable | Obligatoria | Descripción |
|---|---|---|
| `ERP_DB_USER` | Sí | Usuario de MySQL. |
| `ERP_DB_PASSWORD` | Sí | Contraseña del usuario. |
| `ERP_DB_URL` | No | URL JDBC. Por defecto apunta a `localhost:3306/erp_little_horizons` con `sslMode=VERIFY_IDENTITY`. |

Si falta `ERP_DB_USER` o `ERP_DB_PASSWORD`, la aplicación muestra un mensaje claro y
termina con código de salida 1.

Si el usuario no existe, se puede crear uno dedicado con:

```sql
CREATE USER IF NOT EXISTS 'erp_user'@'localhost' IDENTIFIED BY 'una_clave_segura';
GRANT SELECT, INSERT, UPDATE, DELETE ON erp_little_horizons.* TO 'erp_user'@'localhost';
FLUSH PRIVILEGES;
```

La conexión exige TLS y valida la identidad del servidor por defecto. No use
`sslMode=DISABLED` en redes compartidas o conexiones remotas. El usuario de aplicación
solo necesita permisos `SELECT`, `INSERT`, `UPDATE` y `DELETE` dentro de esta base.
El script DDL debe ejecutarse con una cuenta administrativa antes de configurar ese usuario.

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
- **IntelliJ IDEA:** si el proyecto se abrió como carpeta y no aparece un módulo, abra
  `pom.xml` como proyecto Maven o use **Add as Maven Project**. Sin módulo, IntelliJ no
  habilita la ejecución de `Main`; marque `src` como *Sources Root* y `test` como
  *Test Sources Root* si configura el módulo manualmente.

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
En el primer inicio, la consola permite crear el administrador inicial si aún no hay
cuentas. Después pide usuario y contraseña; por defecto la contraseña se oculta al
escribirla, por lo que debe ejecutarse desde una terminal interactiva compatible con
`System.console()`. No existen credenciales predeterminadas.

Para ejecutar desde el botón **Run** de IntelliJ IDEA, que no ofrece una terminal real,
agregue `-Derp.allowInsecurePassword=true` en las *VM options* de la configuración de
ejecución. En ese modo la contraseña se muestra en la consola del IDE y se emite una
advertencia; úselo solo para desarrollo local, nunca en producción ni en equipos
compartidos. La opción segura sigue siendo ejecutar desde una terminal real o desde la
terminal integrada del IDE.

> Antes de ejecutar, configure `ERP_DB_USER` y `ERP_DB_PASSWORD` como se explica en la
> sección 6.2. La aplicación no incluye credenciales embebidas y no inicia sin ellas.

### 7.5. Ejecutar las pruebas

La suite usa un runner mínimo incluido en `test/`, sin JUnit ni dependencias externas:

```bash
mvn test
```

También se puede compilar y ejecutar directamente con el JDK:

```bash
mkdir -p out-test
javac --release 17 -encoding UTF-8 -cp lib/mysql-connector-j-8.4.0.jar \
      -d out-test $(find src test -name "*.java")
java -cp "out-test:lib/mysql-connector-j-8.4.0.jar" com.littlehorizons.erp.test.AllTests
```

Cubre validaciones de entrada, formato de facturas, reglas académicas, reactivación de
membresías, identidad de entidades, asignación FIFO de pagos a cargos, créditos no
aplicados, atomicidad simulada de pagos/facturas, registro de estudiantes con DAOs en
memoria y ciclo de vida del pool. En el corte auditado pasan 47 pruebas; no requiere base
de datos y no sustituye las pruebas de integración JDBC.

---

## 8. Estructura del Proyecto

### Arquitectura MVC + DAO

```
Little-horizons-erp/
├── docs/
│   ├── database.sql                 # DDL + DML (esquema para instalaciones nuevas)
│   ├── AUDITORIA_TECNICA.md         # Hallazgos y verificación del proyecto
│   └── migrations/                  # Migraciones 001–003 para bases existentes
├── facturas/                        # Facturas .txt generadas en tiempo de ejecución
│   └── factura_FAC-2026-00001.txt   # Muestra de factura generada
├── lib/
│   └── mysql-connector-j-8.4.0.jar  # Driver JDBC
├── src/
│   └── com/littlehorizons/erp/
│       ├── Main.java                # Punto de entrada
│       ├── model/                   # Entidades del dominio y DTOs (records)
│       ├── dao/                     # Interfaces de persistencia + implementaciones Jdbc*
│       ├── controller/              # Reglas de negocio y validaciones
│       ├── view/                    # Menús e interfaz de consola
│       └── util/                    # Pool de conexiones, validación, logs, facturas .txt
├── test/                            # Suite de pruebas sin dependencias externas
├── pom.xml
└── README.md
```

### Descripción de cada paquete

| Paquete | Responsabilidad |
|---|---|
| `model` | Clases de dominio (`Student`, `Parent`, `Teacher`, `Subject`, `Payment`, etc.) y records para reportes (`CourseAverage`, `Debtor`, `OverdueLoan`, etc.). |
| `dao` | Interfaces de persistencia (`StudentDao`, `FinanceDao`, …) e implementaciones `Jdbc*Dao` con SQL, mapeo `ResultSet` → objeto y transacciones. Ningún DAO imprime por consola. |
| `controller` | Validaciones, reglas de negocio, coordinación entre DAOs y generación de facturas. Recibe sus DAOs por constructor. |
| Autenticación y autorización | `AuthenticationController`, `SystemUserController`, `SystemUserDao` y `AccessRole` gestionan sesiones, cuentas y roles. |
| `view` | Menús, lectura de datos, tablas de salida y mensajes al usuario. |
| `util` | `ConnectionProvider`, `ConnectionPool`, `Database` (composición), `ConnectionFactory`, `Validator`, `LoggerUtil`, `ConsoleUtils`, `TextInvoiceGenerator`, `FileUtil`, `DataAccessException`. |
| `test` | Runner y casos de prueba en Java puro, con dobles en memoria para los DAOs. |

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
- **O — Open/Closed:** los controladores reciben contratos DAO; se pueden añadir
  implementaciones de persistencia compatibles sin cambiar las reglas del controlador.
- **L — Liskov Substitution:** `JdbcStudentDao` y `InMemoryStudentDao` implementan
  `StudentDao` y se pueden intercambiar en `StudentController`, como hace la suite de pruebas.
- **I — Interface Segregation:** cada DAO expone una interfaz con las operaciones de su
  módulo (`StudentDao`, `FinanceDao`, …); los controladores dependen solo de esos
  contratos y no de detalles JDBC.
- **D — Dependency Inversion:** los controladores reciben sus DAOs por constructor y los
  DAOs reciben un `ConnectionProvider`; la implementación concreta (pool o doble de
  prueba) se decide en el punto de composición (`Database`).

### 9.2. Patrones de Diseño

- **MVC (Model-View-Controller):** separación estricta entre dominio, interfaz y lógica.
- **DAO (Data Access Object):** todo el SQL vive en las implementaciones `Jdbc*Dao`;
  los controladores trabajan contra las interfaces de `dao/`.
- **Dependency Injection por constructor:** controladores y DAOs declaran sus
  dependencias en el constructor; las vistas usan el constructor sin argumentos, que
  enlaza las implementaciones reales.
- **Object Pool:** `ConnectionPool` reutiliza un máximo de 5 conexiones, las valida antes
  de entregarlas y las devuelve al pool al cerrarlas.
- **Singleton:** `ConnectionFactory` mantiene una única configuración de conexión y
  `Database` una única instancia del pool.
- **Autenticación de consola:** las cuentas usan contraseñas con salt aleatorio y PBKDF2;
  cinco intentos fallidos bloquean temporalmente la cuenta. El menú aplica permisos por rol.
- **Generador estático:** `TextInvoiceGenerator.generate(...)` centraliza la creación
  del archivo de factura y devuelve un `GeneratedInvoice` inmutable.
- **DTO con `record`:** reportes y proyecciones (`CourseAverage`, `Debtor`,
  `AttendanceSummary`, etc.) usan records de Java 17.
- `DaoSupport` concentra utilidades compartidas para mapear fechas, nulos y llaves generadas.

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

- Registro, búsqueda por documento o nombre, actualización y retiro lógico. El retiro
  conserva notas, asistencia, matrículas, préstamos y demás historial del estudiante.
- El registro exige un acudiente existente y lo asocia como principal y contacto de
  emergencia; así ningún estudiante queda sin acudiente.
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

- Registro de notas de 0.0 a 5.0 **con un solo decimal** por estudiante, materia,
  profesor, año y período.
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
- Aplicación FIFO de pagos a cargos del mismo estudiante, concepto, año y período, con
  relación persistida para auditar abonos parciales y pagos que cubren varios cargos.
- Los excedentes se conservan como crédito no aplicado, se muestran en el historial y se
  usan al registrar un cargo coincidente. El estado `Pagado` se deriva de las aplicaciones.
- Consulta del historial de pagos por estudiante **o por acudiente** (hermanos).
- Generación automática de factura `.txt` en `facturas/` y registro en la tabla
  `facturas`.
- Consecutivos de recibo y factura generados con una secuencia atómica por año
  (`secuencias_financieras`), sin condiciones de carrera entre usuarios.
- La inserción del pago y su factura es **atómica**: si falla la factura, se revierte el
  pago.
- La factura incluye datos del colegio, NIT, resolución simulada, número consecutivo,
  datos del cliente (nombre, documento y **dirección**), detalle, subtotal, IVA (19 % cuando
  aplica), total, CUFE simulado y payload QR simulado.

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
Dirección: Calle 45 #12-34, Bogotá, Colombia
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
- El estado `Vencido` se calcula en las consultas a partir de la fecha límite; listar
  préstamos no modifica datos.
- Reporte de préstamos vencidos.

### 10.8. Reportes

- Promedio por curso/materia/sección.
- Boletín individual de notas.
- Resumen de notas por materia y período (promedio, máxima, mínima, registros).
- Resumen de asistencia.
- Reporte de estudiantes morosos con cargos pendientes vencidos hace más de un mes
  (cargado, pagos aplicados por antigüedad FIFO y saldo), basado en asignaciones
  `pagos_cargos` persistidas.
- Préstamos vencidos.
- Resumen de matrícula por grado.

---

## 11. Manejo de Errores y Logs

### Sistema de logging

- Implementado en `util/LoggerUtil.java`.
- Archivo: **`error_log.txt`** en el directorio de ejecución.
- Niveles: `INFO`, `WARN`, `DEBUG`, `ERROR`.
- Cada entrada incluye fecha/hora, nivel y mensaje. Los errores registran el tipo de
  excepción y sus marcos de pila, sin incluir textos de usuario ni contraseñas.
- Los mensajes de consola se muestran en español. Los archivos técnicos `error_log.txt`
  y `audit_log.txt` se escriben en inglés. La bitácora registra aperturas de pantalla,
  selección de opciones con su módulo, intentos de autenticación, rechazos de validación,
  consultas identificadas, y el inicio y resultado de las operaciones. Cada entrada incluye
  fecha, sesión, rol, acción, resultado e ID cuando está disponible. El operador se
  identifica con su ID interno, sin guardar el nombre de usuario, contraseñas ni valores
  personales introducidos en formularios. En sistemas POSIX el archivo queda legible solo
  por el propietario.

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
- Los errores de conexión al inicio no muestran la URL ni las credenciales; registran
  únicamente SQLState y código del proveedor.
- Los archivos de log rotan al alcanzar 5 MB y conservan hasta tres respaldos.

### Cuentas y roles de consola

- `ADMINISTRADOR`: acceso a todos los módulos y administración de usuarios.
- `SECRETARIA`: estudiantes, acudientes, profesores, materias, matrículas, módulo académico
  y actividades extracurriculares.
- `DOCENTE`: módulo académico.
- `FINANZAS`: módulo financiero y facturación.
- `BIBLIOTECA`: inventario y préstamos de recursos.
- Los reportes globales se reservan al administrador porque incluyen datos académicos,
  financieros y de préstamos.
- Un administrador puede listar, crear, desactivar y reactivar cuentas, y restablecer sus contraseñas.
  El sistema impide desactivar la cuenta propia y exige mantener al menos un administrador.
- Los nombres de usuario se guardan en minúsculas. Las contraseñas requieren entre 12 y
  128 caracteres, con mayúscula, minúscula, número y símbolo; se almacenan como hash PBKDF2
  con salt individual, nunca en texto plano.
- La identidad autenticada alimenta `audit_log.txt`; no se registran contraseñas.
- Los permisos se aplican por módulo en esta aplicación de consola. El rol docente todavía
  no limita el acceso a cursos o estudiantes específicos.

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
 11. Administrar usuarios
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
- Los permisos actuales separan módulos, pero todavía no filtran filas por docente, curso o
  estudiante. El sistema es una aplicación local de consola, no un servicio multiusuario web.
- Los pagos se aplican a cargos del mismo estudiante, concepto, año y período, en orden de
  vencimiento. Los excedentes quedan como crédito no aplicado para ese mismo grupo y se
  aplican cuando se registra un cargo posterior. El sistema no permite anular ni borrar un
  cargo con aplicaciones financieras; todavía no incluye reembolsos ni reversos contables.

---

## 14. Futuras Mejoras

- Firma digital mediante certificado DIAN y envío del archivo `.txt` firmado.
- Interfaz gráfica (JavaFX/Swing) o API REST con Spring Boot.
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

La verificación funcional se realiza ejecutando la aplicación directamente desde la consola.
Primero configure las credenciales (sección 6.2):

```bash
export ERP_DB_USER='erp_user'
export ERP_DB_PASSWORD='una_clave_segura'
java -cp "out:lib/mysql-connector-j-8.4.0.jar" com.littlehorizons.erp.Main
```

El flujo valida:

1. Conexión exitosa a la base de datos `erp_little_horizons`.
2. Menú principal interactivo con navegación entre sus módulos: estudiantes, acudientes,
   profesores, materias, matrículas, gestión académica, actividades extracurriculares,
   finanzas, recursos y reportes.
3. Operaciones CRUD completas para estudiantes, acudientes, profesores y materias.
4. Registro de pagos y generación automática de la factura electrónica en formato `.txt` (`facturas/factura_*.txt`).
5. Préstamo y devolución de recursos con actualización atómica de inventario.
6. Generación de reportes de notas, promedios, inasistencias y morosidad.

La suite `com.littlehorizons.erp.test.AllTests` (sección 7.5) valida sin base de datos las
validaciones, el formato de facturas, las reglas de registro y el pool de conexiones.

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
- Tabla `secuencias_financieras` para generar consecutivos de recibos y facturas de
  forma atómica y segura ante concurrencia.
- `ON DELETE RESTRICT` en relaciones financieras, académicas y de préstamos para
  preservar registros históricos y evitar dejar estudiantes sin acudiente.
- Numeración de recibos unificada a cinco dígitos (`REC-2026-00001`) para que coincida
  con el formato de las facturas (`FAC-2026-00001`).
- La tabla `facturas` permanece vacía en el script; las facturas se generan desde la
  aplicación para mantener la trazabilidad con cada pago real.
- El retiro de estudiantes conserva el historial; las claves foráneas impiden borrar
  físicamente estudiantes, acudientes o recursos que tengan registros relacionados.
