# Auditoría técnica — Little Horizons ERP

**Fecha de corte:** 13 de noviembre de 2026
**Base versionada:** `78c8d6f97666a53f58e778ddca16ca66ff878633` (rama `main`, sincronizada con `origin/main`)
**Estado revisado:** hardening y corrección financiera preparados localmente sobre el commit base; publicación remota e integración JDBC real pendientes.
**Alcance:** código fuente Java, esquema y datos de `docs/database.sql`, `README.md`, `pom.xml`, pruebas y configuración del repositorio.
**Naturaleza:** auditoría general de seguridad, persistencia, buenas prácticas, escalabilidad, arquitectura MVC, patrones de diseño y JDBC, con verificación estricta del checklist de `proyectojavaa.md`.

---

## 1. Resumen ejecutivo

El proyecto mantiene una arquitectura **sólida y sustancialmente alineada** con los resultados esperados. En este corte se aplicaron correcciones locales de persistencia, consistencia, consultas y documentación. La suite se compiló y ejecutó nuevamente; las pruebas unitarias pasan. La prueba contra el servidor MySQL sigue pendiente: DBeaver contiene un perfil guardado para `mi_base_datos`, pero desde el proceso disponible no se logró ejecutar una consulta autenticada.

Los indicadores de inventario y búsquedas SQL se recalcularon sobre el árbol de trabajo actual; el estado de compilación y pruebas corresponde a la ejecución descrita abajo.

| Indicador | Resultado |
|---|---|
| Archivos Java | 109 en `src/` + 14 en `test/` |
| Líneas Java | 15.825 (fuente + pruebas) |
| Compilación (`javac --release 17`, fuente + pruebas) | Aprobada en este corte |
| Advertencias (`-Xlint:all`) | 2 (recursos de conexión intencionalmente abiertos solo para comprobar disponibilidad) |
| Pruebas automatizadas | **47/47 aprobadas en este corte** |
| Cobertura del esquema | 29 tablas; DDL con PK, FK, índices, `CHECK` y `ENUM` |
| DML | Todos los mínimos exigidos cumplen (30/30/30/30, 60 registros académicos, 30 pagos, etc.) |
| Inyección SQL | No detectada; 202 referencias a `PreparedStatement`, 0 `createStatement` |
| Gestión de recursos JDBC | Las 149 llamadas a `getConnection()` están en código DAO revisado con try-with-resources |

**Veredicto:** proyecto **apto con observaciones, pendiente de integración JDBC y entrega Git final**. La asignación pago-cargo ahora se persiste y la cartera se calcula desde esas aplicaciones; las pruebas simuladas no sustituyen integración MySQL.

**Actualización del corte 13-11-2026:** Java 17 compiló `src` + `test`; `AllTests` aprobó **47/47** con `mvn test`. Maven 3.9.9 quedó instalado en `/tmp` porque `sudo` solicita una contraseña para instalación global. `mvn clean package` terminó con `BUILD SUCCESS` y generó el JAR sombreado; Shade avisó que los JAR comparten `META-INF/MANIFEST.MF`. Se conectó el runner propio al ciclo Maven. La corrección financiera agrega aplicaciones persistidas, créditos no aplicados, asignación FIFO transaccional y una migración para registros existentes. Las pruebas simuladas cubren asignación, excedentes, aplicación posterior y rollback; no se ejecutó SQL contra MySQL real. También se corrigieron la validación del operador de auditoría, el orden de fechas de préstamos, el escape de comodines de búsqueda y el índice de asistencia.

**Limitación de la auditoría:** no se ejecutó SQL ni CRUD contra MySQL desde esta sesión. Una revisión previa documentó MySQL 8.0.46 y un perfil DBeaver inaccesible por CLI; esta auditoría no vuelve a verificar ese servidor. La sesión DBeaver guarda credenciales cifradas y no se extrajeron.

---

## 2. Método y evidencia

1. Lectura completa de `proyectojavaa.md`, `README.md`, `pom.xml` y `docs/database.sql`.
2. Revisión estática de los 109 archivos de `src/` y los 14 de `test/`.
3. Compilación con JDK 17 y `-Xlint:all`.
4. Ejecución de la suite incluida, sin Maven:

```bash
javac --release 17 -encoding UTF-8 \
      -cp "lib/mysql-connector-j-8.4.0.jar" \
      -d /tmp/erp-audit-build $(find src test -name '*.java')

java -cp "/tmp/erp-audit-build:lib/mysql-connector-j-8.4.0.jar" \
     com.littlehorizons.erp.test.AllTests
# All 47 tests passed.
```

5. Validación estática de `docs/database.sql`: conteo de filas por tabla, duplicados de PK/UNIQUE, integridad de FK, valores permitidos en `ENUM`, formato de fechas, `CHECK` y coherencia pagos/cargos.
6. Recompilación y ejecución actual de `AllTests` (47 pruebas) con `mvn test` y `mvn clean package`.
6. Revisión específica de seguridad: autenticación, hash de contraseñas, bloqueo de cuentas, RBAC, TLS, consultas dinámicas, logs y auditoría.
7. Revisión específica de persistencia: transacciones, aislamiento, recuperación ante fallos, secuencias y consistencia de actualizaciones.

---

## 3. Matriz de cumplimiento frente a `proyectojavaa.md`

Leyenda: **✅** cumple · **⚠️** cumple con observaciones · **❌** no cumple o no verificable.

### 3.1 Código y estructura

| Requisito | Estado | Evidencia / observación |
|---|---|---|
| Proyecto Java estándar | ✅ | `pom.xml`, estructura `src/com/littlehorizons/erp`, compilación con JDK 17 |
| Arquitectura MVC | ✅ | `model/`, `view/`, `controller/`, `dao/`, `util/`; las vistas no contienen SQL ni acceso JDBC |
| Código completamente en inglés | ✅ | Clases, métodos, variables y comentarios Java en inglés; el SQL conserva nombres en español, justificado en el README |
| Convenciones Java | ✅ | PascalCase, camelCase y UPPER_SNAKE_CASE consistentes; no se encontraron identificadores españoles en Java |
| Separación de responsabilidades | ✅ | DAO encapsula JDBC; controlador valida reglas; vista gestiona entrada/salida |

### 3.2 Base de datos

| Requisito | Estado | Evidencia / observación |
|---|---|---|
| Base `erp_little_horizons` | ✅ | `CREATE DATABASE IF NOT EXISTS` al inicio de `docs/database.sql` |
| DDL completo | ✅ | 29 tablas con PK, FK, índices, `CHECK`, `ENUM` y políticas `ON DELETE` donde aplica |
| DML con mínimo 30 registros por tabla principal | ✅ | estudiantes 30, acudientes 30, profesores 30, materias 30, registros académicos 60, pagos 30, clubes 30, recursos 30 |
| Datos coherentes y realistas | ⚠️ | Los cinco excedentes quedan representados como crédito no aplicado; algunos segundos nombres usan cadena vacía |
| MySQL 8.0+ | ✅ | Requisito documentado; `CHECK` y `ENUM` compatibles con 8.0.16+; no se pudo validar en vivo |
| Script ejecutable desde cero | ⚠️ | Funciona sobre base vacía; no es idempotente (documentado en README) |

### 3.3 Funcionalidades

| Requisito | Estado | Evidencia / observación |
|---|---|---|
| Estudiantes, acudientes y matrícula | ✅ | CRUD, asociación N:M, matrícula anual por grado y estados |
| Profesores, materias y horarios | ✅ | CRUD, asignaciones y detección de conflictos de horario |
| Notas y promedios | ✅ | Registro 0.0–5.0 con un decimal, historial, promedios por materia/periodo/curso |
| Asistencia y alertas | ✅ | Estados requeridos, resumen y ausencias recurrentes |
| Clubes y eventos | ✅ | Reactivación con bloqueo transaccional y verificación de cupo; pruebas de controlador aprobadas |
| Pagos, recibos y facturación TXT | ⚠️ | Pago, aplicaciones, créditos y factura atómicos; faltan pruebas con MySQL real |
| Recursos y préstamos | ✅ | Préstamo/devolución transaccionales, control de inventario y vencidos |
| Reportes | ✅ | Boletines, promedios, asistencia, morosos, préstamos vencidos y matrícula |
| Validación de entradas | ✅ | Comprobaciones de inscripción y asignación para registrar notas/asistencia; pruebas de controlador aprobadas |
| Mensajes claros | ⚠️ | Buenos mensajes de negocio; algunas violaciones de UNIQUE/FK terminan en mensajes genéricos (M-09) |
| Navegación intuitiva | ✅ | Menús por módulo, retorno y confirmaciones |

### 3.4 Calidad del código

| Requisito | Estado | Evidencia / observación |
|---|---|---|
| Manejo de errores con try-catch | ✅ | Excepciones de dominio y `DataAccessException`; sin `printStackTrace` ni `catch (Exception)` |
| Logs en `error_log.txt` | ✅ | `LoggerUtil` con niveles INFO/WARN/DEBUG/ERROR y sanitización de saltos de línea |
| `PreparedStatement` en todas las consultas | ✅ | 202 referencias; 0 `createStatement`; nombres de tabla dinámicos solo con listas blancas |
| Try-with-resources | ✅ | Conexiones DAO, `PreparedStatement` y `ResultSet` usan gestión automática de recursos |
| Principios SOLID | ✅ | Interfaces DAO, inyección por constructor, responsabilidades separadas; ver observaciones de diseño |
| Lambdas y Stream API | ✅ | Uso extensivo en controladores, vistas y reportes |
| Reportes/facturas TXT | ✅ | Factura con colegio, cliente, consecutivo, CUFE, QR, resolución, subtotal/IVA/total; pruebas unitarias |

### 3.5 Documentación y entrega

| Requisito | Estado | Evidencia / observación |
|---|---|---|
| Carpeta `docs/` | ✅ | `docs/database.sql` y migraciones `001`–`003` |
| `database.sql` con DDL y DML | ✅ | 1.288 líneas; secciones separadas y bloque de verificación final |
| README con las 16 secciones | ✅ | Título, descripción, autores, tecnologías, requisitos, BD, ejecución, estructura, SOLID/patrones, funcionalidades, logs, capturas, limitaciones, mejoras, licencia y contacto |
| Repositorio privado y trainer como colaborador | ⚠️ | No verificable desde el workspace; el README declara la URL privada |
| Commits claros y atómicos | ✅ | Historial legible en inglés (`fix:`, `feat:`) |
| Hash completo del último commit documentado | ❌ | El README no incluye el SHA final. La base revisada es `78c8d6f97666a53f58e778ddca16ca66ff878633`; todavía falta el commit final posterior a las correcciones financieras. |
| Estructura de carpetas organizada | ✅ | `src`, `test`, `docs`, `lib`, `facturas` |
| Compila sin errores | ✅ | 0 errores con `javac --release 17` |
| Ejecuta correctamente | ⚠️ | Requiere MySQL, variables de entorno y terminal interactiva (`System.console()`) |
| Conexión a BD y CRUD en vivo | ⚠️ | No verificado contra MySQL real; estas pruebas no modificaron datos |
| Logs y TXT generados | ✅ | `error_log.txt`, `audit_log.txt` y `facturas/factura_FAC-2026-00001.txt` presentes |

---

## 4. Hallazgos

### 4.1 Críticos

**Ninguno.** No se encontraron vulnerabilidades de inyección SQL, ejecución arbitraria, exposición de credenciales, bypass de autenticación ni corrupción inmediata de datos.

### 4.2 Altos

#### H-01 — [Corregido; pruebas unitarias aprobadas, integración JDBC pendiente] Se podían registrar notas y asistencias sin inscripción ni asignación válida

- **Ubicación:** `AcademicController.registerGrade` (`src/com/littlehorizons/erp/controller/AcademicController.java:57-67`), `registerAttendance` (`:126-136`), y validadores de entrada del mismo controlador.
- **Estado actual:** `AcademicController.registerGrade` consulta `requireEnrollment` y `requireTeacherAssignment`; `registerAttendance` consulta matrícula y, si se indicó, la asignación docente. El hallazgo original queda atendido en el código actualmente visible, aunque el árbol de trabajo está sin confirmar.
- **Descripción original:** las validaciones solo comprobaban identificadores/formato y no la relación académica. Las FK por sí solas no prueban que el estudiante curse la materia ni que el docente esté asignado.
- **Impacto:** un boletín o promedio puede incluir notas de materias no inscritas o impartidas por un profesor no asignado; la asistencia puede registrarse para materias no cursadas. Afecta la integridad académica y los reportes.
- **Verificación:** pruebas de controlador para matrícula y asignación docente incluidas en las 47 aprobadas. Falta validación de comportamiento JDBC contra MySQL.
- **Esfuerzo:** medio.

#### H-02 — [Corregido en controlador; integración de persistencia pendiente] Re-inscripción en clubes y capacidad

- **Ubicación:** `docs/database.sql:302` (`UNIQUE KEY membresia_unica (id_estudiante, id_club)`); `JdbcClubDao.insertMembership` (`src/com/littlehorizons/erp/dao/JdbcClubDao.java:171-229`); `JdbcClubDao.updateMembership` (`:231-254`); `ExtracurricularController.enrollInClub` (`src/com/littlehorizons/erp/controller/ExtracurricularController.java:63-79`); `ExtracurricularView` (`:172-196`).
- **Estado actual:** el controlador y la vista exponen reactivación; `JdbcClubDao.reactivateMembership` vuelve a leer y bloquear la membresía y el club con `FOR UPDATE`, comprueba actividad/cupo, actualiza la fila existente y confirma en una transacción. La unicidad conserva el historial y el controlador dirige al flujo de reactivación.
- **Descripción original:** `insertMembership` no podía crear una segunda fila por la restricción única `(id_estudiante, id_club)`, y no había opción de reactivar controlando el cupo.
- **Impacto:** un estudiante no puede volver a un club tras retirarse sin perder trazabilidad; si se reactiva por SQL, se puede superar el cupo máximo, incumpliendo el requisito de validación de capacidad.
- **Verificación:** pruebas de controlador para reactivación con cupo disponible, capacidad completa y membresía retirada/duplicada. El bloqueo concurrente `FOR UPDATE` requiere integración MySQL; aún no se ejecutó.
- **Esfuerzo:** medio.

#### H-03 — [Corregido en código y DDL; prueba JDBC real pendiente] La numeración financiera podía iniciar en 2 y presentar saltos

- **Ubicación:** `docs/database.sql:1211-1217` (semilla `factura = 1`, `recibo = 30`, sin filas en `facturas`); `JdbcFinanceDao.nextSequenceNumber` (`src/com/littlehorizons/erp/dao/JdbcFinanceDao.java:305-336`); `FinanceController.registerPayment` (`src/com/littlehorizons/erp/controller/FinanceController.java:160-183`); `generateInvoiceForPayment` (`:189-212`).
- **Corrección:** los números se reservan en la misma conexión y transacción que el pago y la factura. El TXT se elimina si la transacción falla. La semilla nueva de factura comienza en cero; `docs/migrations/002_financial_sequences.sql` reconcilia instalaciones existentes con sus documentos persistidos.
- **Impacto:** se debilita el requisito de “número de factura consecutivo”; pueden existir huecos entre recibos/facturas y una diferencia entre la secuencia de recibo y la de factura. En un contexto fiscal simulado, es una observación de cumplimiento relevante.
- **Verificación:** prueba con conexión simulada confirma consecutivos y rollback cuando falla la creación de factura. Falta forzar el caso contra MySQL real.
- **Esfuerzo:** medio.

### 4.3 Medios

#### M-01 — [Corregido] Nombres opcionales nulos o vacíos producían “null” o dobles espacios

- **Ubicación:** `Student.getFullName` (`src/com/littlehorizons/erp/model/Student.java:160-162`), `Parent.getFullName` (`src/com/littlehorizons/erp/model/Parent.java:139-141`), `Teacher.getFullName` (`src/com/littlehorizons/erp/model/Teacher.java:126-128`); `ViewUtils.readWithDefault` (`src/com/littlehorizons/erp/view/ViewUtils.java:86-91`); `JdbcStudentDao.bind` (`src/com/littlehorizons/erp/dao/JdbcStudentDao.java:236`); uso en facturas (`FinanceController.java:173` y `:201`).
- **Descripción:** los métodos concatenan los cuatro componentes sin filtrar nulos ni vacíos. Al registrar un estudiante nuevo, el segundo nombre opcional queda `null`; al leer datos sembrados con `''`, queda cadena vacía. El resultado es `"Ana null Pérez null"` o `"Ana  Pérez "`.
- **Impacto:** facturas, listados y boletines muestran nombres poco profesionales; la factura es un artefacto de cara al usuario.
- **Recomendación:** centralizar el formateo en un helper que una solo partes no blank con un espacio; normalizar opcionales a `null` o `''` de forma consistente; añadir pruebas para nulo, vacío y espacios.
- **Esfuerzo:** bajo.

#### M-02 — [Corregido] Quince modelos sobrescribían `equals` sin `hashCode`

- **Ubicación:** `Student`, `Parent`, `Teacher`, `Subject`, `Club`, `Membership`, `Event`, `Participant`, `Attendance`, `Grade`, `Payment`, `PaymentConcept`, `Invoice`, `Loan`, `Resource` (todos en `src/com/littlehorizons/erp/model/`). Ejemplo: `Student.equals` (`Student.java:177-183`).
- **Descripción:** `-Xlint:all` reporta 15 advertencias. Además, `Student.equals` usa `documentNumber`, un campo mutable con setter.
- **Impacto:** comportamiento incorrecto en `HashSet`/`HashMap`; si el documento cambia después de insertar el objeto en una colección, la búsqueda falla. No afecta los flujos actuales, que no usan esos modelos como claves, pero es una deuda técnica objetiva.
- **Recomendación:** implementar `hashCode` con `Objects.hash` o convertir los modelos en `record`; para igualdad estable, usar el ID inmutable. Añadir pruebas de contrato `equals/hashCode`.
- **Esfuerzo:** bajo.

#### M-03 — [Corregido] Varias actualizaciones DAO ignoraban las filas afectadas

- **Ubicación:** `JdbcStudentDao.update` (`:105-121`), `JdbcAttendanceDao.update` (`:62-75`), `JdbcEnrollmentDao.update` (`:60-73`), `JdbcClubDao.updateMembership` (`:231-254`), entre otras.
- **Descripción:** se ejecuta `ps.executeUpdate()` sin comprobar el valor devuelto. Algunos controladores prevalidan la existencia, pero otros no (`updateAttendance`, `updateGrade`, `updateMembership`). Ante un ID inexistente o una carrera, la operación no cambia nada y la vista informa éxito.
- **Impacto:** falsos positivos de éxito; el usuario cree que actualizó un registro que no existe.
- **Recomendación:** devolver `boolean`/`int` desde los métodos de actualización y lanzar `IllegalArgumentException` si es 0; o validar existencia dentro de la misma transacción. Añadir pruebas con ID inexistente.
- **Esfuerzo:** bajo-medio.

#### M-04 — [Corregido] El alta de estudiante + acudiente no era atómica

- **Ubicación:** `StudentDao.insertWithGuardian` (`src/com/littlehorizons/erp/dao/StudentDao.java:13-22`); `StudentController.register` (`src/com/littlehorizons/erp/controller/StudentController.java:26-39`).
- **Descripción:** el método por defecto inserta el estudiante, luego asigna el acudiente y, si falla, intenta borrar el estudiante como compensación. No es una transacción real: un fallo del proceso entre ambos pasos deja un estudiante huérfano, y el borrado compensatorio puede fallar.
- **Impacto:** inconsistencia de datos y violación del requisito de que cada estudiante tenga al menos un acudiente.
- **Recomendación:** implementar la operación en `JdbcStudentDao` con una sola conexión, `setAutoCommit(false)`, `commit()`/`rollback()`; o introducir un servicio transaccional. Añadir prueba de fallo inducido.
- **Esfuerzo:** medio.

#### M-05 — [Corregido; integración JDBC real pendiente] La relación pago-cargo no se persistía

- **Corrección:** `pagos_cargos` persiste cuánto de cada pago se aplica a cada obligación. El DAO serializa operaciones financieras por estudiante, aplica créditos en orden de vencimiento dentro de la misma transacción de pago/factura y guarda cualquier excedente como crédito no aplicado. El reporte de morosos suma las aplicaciones persistidas. Los excedentes no se pierden ni se mezclan entre conceptos/períodos.
- **Datos existentes:** `003_payment_charge_allocations.sql` reconstruye asignaciones históricas FIFO con funciones de ventana, conserva excesos como crédito y recalcula estados. La migración requiere respaldo y una única ejecución con la aplicación detenida.
- **Límite:** no hay flujo de devolución o reverso de pagos; no se permite anular un cargo con pagos aplicados. La migración y las transacciones necesitan validación con MySQL real.
- **Esfuerzo:** alto.

#### M-06 — [Corregido] Consultas de asistencia usaban `YEAR()` y alertas sin ventana temporal

- **Ubicación:** `JdbcAttendanceDao.summaryByStudent` (`:127`), `findRecurringAbsences` (`:148-156`).
- **Corrección:** `summaryByStudent` ahora filtra por rango anual semiabierto; alertas recurrentes limitadas a los últimos 90 días; índice compuesto definido en `docs/database.sql` y anotado para actualización de instalaciones previas.
- **Impacto:** escaneo completo y degradación progresiva al crecer el histórico; la alerta puede devolver patrones antiguos.
- **Recomendación:** usar rangos (`fecha_asistencia >= ? AND fecha_asistencia < ?`), limitar a año/periodo vigente y crear un índice compuesto `(estado_asistencia, fecha_asistencia, id_estudiante)`.
- **Esfuerzo:** bajo.

#### M-07 — [Mitigado: hay rotación, persiste I/O síncrono] Logging con apertura/cierre por entrada

- **Ubicación:** `LoggerUtil.log` (`src/com/littlehorizons/erp/util/LoggerUtil.java:54-68`); `AuditLogger.append` (`src/com/littlehorizons/erp/util/AuditLogger.java:125-149`).
- **Descripción:** cada entrada abre y cierra el archivo (`FileWriter` o `Files.writeString`); ambos métodos están sincronizados. Los dos logs rotan al alcanzar 5 MiB y conservan tres copias.
- **Impacto:** I/O y contención de locks en cada operación. En una aplicación de consola es tolerable, aunque limita la concurrencia.
- **Recomendación:** si crece la concurrencia, usar un writer con buffer o un appender asíncrono; mantener la rotación actual.
- **Esfuerzo:** medio.

#### M-08 — [Corregido] Operaciones de lectura que modificaban estado

- **Ubicación:** anteriormente `ResourceController.listStudentLoans` y `listTeacherLoans` llamaban a `resourceDao.markOverdueLoans()`.
- **Descripción:** listar préstamos ejecuta una transacción de `UPDATE` para marcar vencidos. Una consulta de solo lectura tiene efectos secundarios y toma locks.
- **Impacto:** acoplamiento inesperado, contención y dificultad para razonar sobre reportes; si el `UPDATE` falla, el listado falla.
- **Corrección:** los listados ya no escriben; `JdbcResourceDao` calcula `Vencido` en la consulta a partir de la fecha límite.
- **Esfuerzo:** bajo.

#### M-09 — [Mitigado] Prevalidaciones débiles que terminaban en errores genéricos de constraint

- **Ubicación:** `MatriculationController.register` (`:26-29`) no verifica existencia de estudiante/grado ni duplicado; `TeacherController.register` (`:25-28`) no verifica documento duplicado; `SubjectController.register` (`:25-28`) no verifica código duplicado; `JdbcEnrollmentDao.insert` (`:42-58`) no verifica duplicado.
- **Descripción:** se delega la validación a las restricciones de la base. Una violación de UNIQUE/FK produce `DataAccessException` con el mensaje genérico “No se pudo registrar…”, sin indicar el campo.
- **Impacto:** experiencia de usuario pobre y incumplimiento parcial del requisito de mensajes claros.
- **Corrección y límite:** `DataAccessException` transforma las restricciones MySQL comunes de duplicado y FK en mensajes de negocio sin exponer SQL. No todas las entidades tienen preconsulta por duplicado; la restricción de base conserva la garantía de integridad.
- **Esfuerzo:** medio.

#### M-10 — [Corregido] `mvn test` ejecuta el runner de la suite

- **Corrección:** `pom.xml` configura `testSourceDirectory=test` y ejecuta el runner `AllTests` en la fase `test`; README documenta `mvn test`.
- **Verificación:** `mvn test` aprobó 47 pruebas. Surefire no tiene pruebas anotadas; la suite propia se lanza como proceso Java y su código de salida controla el build.
- **Esfuerzo:** bajo.

### 4.4 Bajos

#### B-01 — [Corregido] `AuditLogger.startSession` valida el operador antes de crear su prefijo

- **Ubicación:** `AuditLogger.startSession` (`:32-39`).
- **Estado:** valida el valor sanitizado antes de añadir el prefijo `actor_id:`.
- **Descripción original:** la comprobación posterior al prefijo no detectaba un operador vacío.
- **Impacto:** código muerto; no es explotable porque la vista pasa el nombre visible autenticado.
- **Recomendación:** validar `operatorName` antes de concatenar.

#### B-02 — [Corregido] Fechas de préstamo con orden validado

- **Ubicación:** `docs/database.sql:481-510`; `ResourceController.validateLoan` (`:152-170`).
- **Corrección:** el controlador rechaza fecha esperada anterior al préstamo y el DDL incluye `CHECK` para fechas esperadas y reales en las dos tablas de préstamo.
- **Descripción original:** no se exigía orden entre las fechas.
- **Impacto:** es posible registrar devoluciones anteriores al préstamo.
- **Recomendación:** añadir `CHECK` en ambas tablas y validación en el controlador.

#### B-03 — [Corregido parcialmente] Búsqueda con comodines escapados; `LIKE '%…%'` aún escanea

- **Ubicación:** `JdbcStudentDao.search` (`:180-185`).
- **Corrección:** `JdbcStudentDao.search` escapa `!`, `%` y `_` con `ESCAPE '!'`, de modo que se buscan como caracteres literales.
- **Límite:** el patrón `%texto%` sigue impidiendo un índice B-tree útil; con 500 estudiantes es aceptable. Considerar búsqueda por prefijo o texto completo si aumenta el volumen.

#### B-04 — [Parcialmente corregido] Advertencias de compilación y código muerto

- **Ubicación:** `FileUtil.java` sin usos; `LoggerUtil.debug` y `Database.openConnections` sin usos; `Main.java:35` y `ConnectionPoolTest.java:98` tienen recursos `ignored` no referenciados. Las excepciones ya declaran `serialVersionUID`.
- **Impacto:** ruido de compilación y deuda técnica menor.
- **Recomendación:** añadir `serialVersionUID`, eliminar `FileUtil` o usarlo, y suprimir/anotar el recurso no referenciado.

#### B-05 — Diferencias menores en README

- **Ubicación:** captura del menú principal (`README.md:550-568`) omite la opción 11 “Administrar usuarios”; no se documenta el hash completo del commit.
- **Impacto:** inconsistencia documental y requisito de entrega incompleto.
- **Recomendación:** actualizar la captura y añadir una sección “Hash de entrega” con el SHA completo del commit final, después de confirmar todos los cambios locales. No publicar el SHA de la base actual como hash de entrega.

#### B-06 — [Corregido en el primer commit de auditoría] La especificación correcta debe quedar versionada

- **Ubicación:** el archivo correcto es `proyectojavaa.md`; existe otra versión distinta `proyectojava.md`, que `.gitignore` excluye.
- **Corrección:** se incorpora `proyectojavaa.md` al repositorio para que la especificación usada en la evaluación quede junto al código.

#### B-07 — [Parcialmente resuelto] DML no idempotente; excedentes registrados como créditos

- **Ubicación:** `docs/database.sql` (sin `DROP`/`TRUNCATE`, IDs fijos); cinco excedentes quedan en `monto_no_aplicado` como crédito.
- **Impacto:** reejecutar el script falla; el excedente financiero ya tiene tratamiento explícito y se muestra en el historial de pagos.
- **Recomendación:** mantener la instalación sobre base vacía documentada y respaldar antes de reinstalar. Antes de ejecutar `003`, conciliar los cargos marcados manualmente como pagados que no tengan pagos históricos suficientes: la migración recalcula el estado desde las asignaciones reconstruidas y podría dejarlos pendientes.

#### B-08 — Entrada de contraseña dependiente de TTY

- **Ubicación:** `ConsoleUtils.readPassword` (`:43-56`).
- **Descripción:** lanza `IllegalStateException` si `System.console()` es `null`. La aplicación no se puede autenticar desde un IDE sin terminal, un pipe o CI.
- **Impacto:** limitación operativa, documentada en README (`:242`).
- **Recomendación:** mantener el comportamiento seguro y, opcionalmente, ofrecer un modo de prueba con lectura por `Scanner` solo bajo una bandera explícita.

---

## 5. Análisis por dimensión

### 5.1 Seguridad

**Fortalezas verificadas:**

- **Sin inyección SQL:** las 175 sentencias usan `PreparedStatement`; no hay `createStatement`. Las únicas interpolaciones dinámicas son nombres de tabla en `JdbcResourceDao.returnLoan` y `JdbcAssignmentDao.lockIds`, restringidas por listas blancas de constantes.
- **Credenciales:** `ConnectionFactory` exige `ERP_DB_USER` y `ERP_DB_PASSWORD`; no hay usuario/clave embebidos. TLS por defecto con `sslMode=VERIFY_IDENTITY`.
- **Contraseñas:** PBKDF2-HMAC-SHA256 con 600.000 iteraciones, sal aleatoria de 16 bytes, hash de 32 bytes y comparación en tiempo constante (`MessageDigest.isEqual`). Política de 12–128 caracteres con mayúscula, minúscula, número y símbolo.
- **Autenticación:** hash ficticio para usuarios inexistentes (evita enumeración por tiempo), bloqueo de 15 minutos tras 5 intentos fallidos, reseteo en login exitoso y mensaje genérico que no revela estado de la cuenta.
- **Autorización:** RBAC por rol (`ADMINISTRADOR`, `SECRETARIA`, `DOCENTE`, `FINANZAS`, `BIBLIOTECA`) con validación tanto al mostrar el menú como al ejecutar la opción; registro de accesos denegados.
- **Auditoría:** `audit_log.txt` con sesión, operador, rol, pantalla, evento, acción, resultado, ID y tipo de error; escritura fail-closed (si no se puede auditar, la operación aborta) y permisos `rw-------`.
- **Logs:** `LoggerUtil` sanitiza saltos de línea y tabulaciones; `AuditLogger` además neutraliza `|` y `=`. Ambos aplican permisos restrictivos.
- **Facturas:** el generador escapa saltos de línea y rechaza sobrescribir archivos existentes; el nombre se construye internamente, sin entrada directa del usuario.

**Riesgos residuales:** los de M-07 (I/O de logs) y B-08 (TTY) son operativos, no vulnerabilidades. No se identificaron fallos críticos.

### 5.2 Persistencia, transacciones y JDBC

**Fortalezas verificadas:**

- Transacciones con `setAutoCommit(false)`, `commit()` y `rollback()` en:
  - pago + factura (`JdbcFinanceDao.registerPaymentWithInvoice`);
  - alta de membresía con bloqueo `FOR UPDATE` y control de cupo (`JdbcClubDao.insertMembership`);
  - préstamo/devolución con bloqueo de recurso (`JdbcResourceDao.insertLoan`, `returnLoan`);
  - actualización de recurso con préstamos activos (`JdbcResourceDao.updateResource`);
  - reserva de secuencias de pago/factura dentro de la transacción;
  - reactivación de membresía con bloqueo y control de capacidad.
- Restauración de `autoCommit` en `finally`.
- Pool propio con máximo 5 conexiones, timeout de 5 s, validación de conexiones ociosas, proxies que devuelven al pool al cerrar y contabilidad thread-safe; 6 pruebas específicas.
- Uso consistente de `DaoSupport` para fechas, nulos y claves generadas.
- `registerPaymentWithInvoice` revierte la transacción y el controlador elimina el TXT si algo falla, evitando archivos huérfanos en el caso normal.

**Debilidades:** la migración de asignaciones financieras aún no se ejecutó sobre MySQL real, falta el flujo de reembolsos, y el pool de cinco conexiones no se ha evaluado bajo carga. La suite verifica asignaciones y rollback con conexiones simuladas.

### 5.3 MVC, SOLID y patrones

**Cumplimiento:**

- **Modelo:** entidades y `record`s de proyección (`Debtor`, `AttendanceSummary`, `SubjectAverage`, etc.).
- **Vista:** menús, lectura y tablas; no hay SQL ni `java.sql` en `view/`.
- **Controlador:** reglas de negocio y coordinación; no hay `System.out` en `controller/`.
- **DAO:** interfaces + implementaciones JDBC; 16 interfaces DAO, inyección por constructor y dobles en memoria en pruebas.
- **Solid:**
  - SRP: separación por capas y por módulo.
  - OCP: interfaces DAO permiten nuevas implementaciones.
  - LSP: implementaciones JDBC y fakes respetan el contrato.
  - ISP: interfaces pequeñas y específicas.
  - DIP: controladores dependen de abstracciones; los constructores sin argumentos son solo un valor por defecto para la composición.
- **Patrones:** Singleton (`ConnectionFactory` con holder lazy, `Database` con pool compartido), DAO, métodos plantilla de consulta en las implementaciones JDBC, fábrica de conexiones y composición manual en `Main`/`MainMenu`.
- **Lambdas/Streams:** uso extensivo en controladores y vistas (362 líneas con `stream` o `->`), con `map`, `filter`, `sorted`, `collect`, `forEach` y `Optional` en proyecciones.

**Observaciones de diseño (no bloqueantes):**

- Las vistas instancian controladores concretos en sus constructores; un “composition root” único (como `MainMenu`) mejoraría la testabilidad, pero la inyección por constructor ya existe.
- `StudentDao.insertWithGuardian` mezcla orquestación de negocio en la interfaz DAO (ver M-04).
- Los DAO gestionan transacciones directamente; es aceptable en una arquitectura JDBC por capas, pero un servicio transaccional haría explícita la unidad de trabajo.

### 5.4 Escalabilidad y rendimiento

La aplicación es una consola monousuario; el diseño es adecuado para el tamaño del colegio (500 estudiantes). Los límites principales son:

1. **Logs síncronos** con apertura/cierre por entrada y `synchronized` (M-07), aunque ahora rotan para limitar el crecimiento del disco.
2. **Asignación pago-cargo** ahora persistida con créditos pendientes; queda pendiente probar su migración y transacciones sobre MySQL.
3. **Búsqueda** con `LIKE '%…%'` (B-03).
4. **Reportes** que cargan listas completas en memoria; aceptable a esta escala, pero sin paginación.
5. **Pool de 5 conexiones**: suficiente para consola; no para concurrencia web.
6. El pool de conexiones es compartido y acotado a cinco; es apropiado para consola pero no se ha evaluado bajo carga web concurrente.

No se observaron problemas de N+1 relevantes en los flujos principales; los reportes usan consultas agregadas.

### 5.5 Base de datos y documentación

- **DDL:** 29 tablas, PK en todas, FK con políticas `ON DELETE` en la mayoría de relaciones, índices para claves de búsqueda y `CHECK` para montos, notas, años, intentos y estados. Se usan `ENUM` para estados y tipos.
- **DML:** conteos mínimos cumplidos; integridad referencial validada; fechas lógicas y montos en COP. El bloque final de verificación facilita la comprobación.
- **Observaciones:** 5 sobrepagos de semilla se conservan como créditos, cadenas vacías en segundos nombres, ausencia de filas en `facturas` pese a la semilla 1, y DML no idempotente (documentado).
- **README:** completo, con las 16 secciones, arquitectura, SOLID, patrones, configuración, credenciales, TLS, ejecución, logs, factura de ejemplo y limitaciones. Es uno de los puntos más fuertes del proyecto.
- **Migraciones:** `001_system_users.sql` agrega cuentas; `002_financial_sequences.sql` reconcilia consecutivos; `003_payment_charge_allocations.sql` reconstruye aplicaciones pago-cargo. Aplicarlas una sola vez y en orden.

### 5.6 Pruebas y reproducibilidad

- **47 pruebas** aprobadas en este corte: incluyen asignación FIFO, pagos parciales, excedentes, créditos sin cargo, aplicación posterior y rollback financiero simulado.
- Las pruebas usan dobles en memoria (`InMemoryStudentDao`, `InMemoryParentDao`) y no requieren base de datos, lo que explica que pasen en este entorno.
- **Carencias:** no hay pruebas de integración JDBC ni transacciones reales contra MySQL; tampoco hay pruebas específicas de los flujos de autenticación/RBAC.
- **Reproducibilidad:** `mvn test` ejecuta las 47 pruebas del runner propio; el README también documenta la ejecución directa con `javac` y `java`.

---

## 6. Plan de remediación priorizado

| Prioridad | Hallazgo | Acción recomendada | Esfuerzo |
|---|---|---|---|
| Hecho | H-03 | Reservar secuencias dentro de la transacción; corregir semilla y añadir migración | Medio |
| Hecho | H-01 | Validar reglas académicas en pruebas de controlador | Bajo |
| Pendiente DB | H-02 | Probar bloqueo, reactivación y capacidad con MySQL real | Bajo-medio |
| Hecho | M-01 | Helper de nombre completo y normalización de opcionales | Bajo |
| Hecho | M-03 | Comprobar filas afectadas en operaciones `update` | Bajo-medio |
| Hecho | M-04 | Alta estudiante+acudiente en una sola transacción JDBC | Medio |
| Hecho | M-05 | Persistir asignaciones pago-cargo, saldos de crédito y migración histórica | Alto |
| Hecho | M-06 | Rangos de fecha, alertas acotadas e índice de asistencia | Bajo |
| Mitigado | M-07 | Rotación de logs; la escritura continúa síncrona | Medio |
| Mitigado | M-09 | Mensajes específicos para duplicados y FK | Medio |
| Hecho | M-02 | `hashCode` e igualdad de entidades persistidas por ID | Bajo |
| Hecho | M-08 | Calcular vencimiento sin mutar datos en una lectura | Bajo |
| Hecho | M-10 | Conectar `AllTests` al ciclo `mvn test` | Bajo |
| Hecho | B-01 | Validar operador antes del prefijo | Bajo |
| Hecho | B-02 | `CHECK` y validación de fechas de préstamo | Bajo |
| Hecho | B-03 | Escapar comodines de búsqueda; patrón contiene comodín inicial | Bajo |
| Parcial | B-04 | Añadir `serialVersionUID`; quedan dos advertencias y utilidades sin uso | Bajo |
| P3 | B-05 | Actualizar README (menú y hash completo) | Bajo |
| Hecho | B-06 | Versionar `proyectojavaa.md`, la especificación auditada | Bajo |
| Parcial | B-07 | Los excedentes ya son créditos explícitos; DML sigue sin ser idempotente | Bajo |

**Siguiente paso:** ejecutar integración JDBC sobre una base de prueba `mi_base_datos` o equivalente, cubrir CRUD, secuencias y reactivación, y revisar los hallazgos bajos que siguen abiertos.

---

## 7. Conclusión

Little Horizons ERP **cumple sustancialmente** con los resultados esperados de `proyectojavaa.md`. La estructura MVC es clara, el código Java está en inglés, el esquema y los datos cumplen los mínimos, y el README documenta configuración, ejecución y límites. La compilación Java 17 y las 47 pruebas pasan en este corte.

Los riesgos restantes son la falta de integración JDBC ejecutada en vivo, la validación de la nueva migración sobre MySQL y hallazgos bajos del análisis estático. El runner se ejecutó con Maven y el paquete se construyó correctamente. Los commits preparados localmente todavía deben publicarse en GitHub.

**Recomendación final:** respaldar una base existente, probar la migración `003` y los flujos de pago/cargo con MySQL, luego crear el commit final en `main` y entregar su SHA completo.
