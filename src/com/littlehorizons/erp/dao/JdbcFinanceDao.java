package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Charge;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.StudentConcept;
import com.littlehorizons.erp.util.ConnectionProvider;
import com.littlehorizons.erp.util.Database;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.PaymentAllocator;
import com.littlehorizons.erp.util.PaymentAllocator.ChargeBalance;
import com.littlehorizons.erp.util.PaymentAllocator.PaymentCredit;
import com.littlehorizons.erp.util.PaymentAllocator.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Data access object for the financial module.
 *
 * <p>Payments and their invoices are persisted inside a single transaction so
 * that no payment can exist without its invoice and vice versa.</p>
 */
public class JdbcFinanceDao implements FinanceDao {

    private final ConnectionProvider connectionProvider;

    public JdbcFinanceDao() {
        this(Database.provider());
    }

    public JdbcFinanceDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private static final String CONCEPT_SELECT =
            "SELECT id_concepto, codigo_concepto, nombre_concepto, descripcion, "
            + "monto_base, es_recurrente, esta_activo, fecha_creacion, "
            + "fecha_actualizacion FROM conceptos_pago ";

    private static final String CHARGE_SELECT =
            "SELECT c.id_cargo, c.id_estudiante, c.id_concepto, c.año_academico, "
            + "c.periodo_academico, c.monto, c.fecha_vencimiento, c.estado, "
            + "c.observaciones, c.fecha_creacion, "
            + "CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), "
            + "NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, cp.nombre_concepto "
            + "FROM cargos_estudiante c "
            + "INNER JOIN estudiantes e ON e.id_estudiante = c.id_estudiante "
            + "INNER JOIN conceptos_pago cp ON cp.id_concepto = c.id_concepto ";

    private static final String PAYMENT_SELECT =
            "SELECT p.id_pago, p.id_estudiante, p.id_acudiente, p.id_concepto, "
            + "p.monto_pagado, p.monto_no_aplicado, p.fecha_pago, p.metodo_pago, p.numero_recibo, "
            + "p.año_academico, p.periodo_academico, p.notas, p.fecha_creacion, "
            + "CONCAT_WS(' ', NULLIF(TRIM(e.primer_nombre), ''), NULLIF(TRIM(e.segundo_nombre), ''), NULLIF(TRIM(e.primer_apellido), ''), "
            + "NULLIF(TRIM(e.segundo_apellido), '')) AS student_name, cp.nombre_concepto "
            + "FROM pagos p "
            + "INNER JOIN estudiantes e ON e.id_estudiante = p.id_estudiante "
            + "INNER JOIN conceptos_pago cp ON cp.id_concepto = p.id_concepto ";

    private static final String INVOICE_SELECT =
            "SELECT id_factura, numero_factura, id_pago, fecha_emision, cufe, "
            + "nombre_archivo_txt, ruta_archivo, fecha_creacion FROM facturas ";

    public int insertConcept(PaymentConcept concept) {
        String sql = "INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, "
                + "descripcion, monto_base, es_recurrente, esta_activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, concept.getConceptCode());
            ps.setString(2, concept.getName());
            ps.setString(3, concept.getDescription());
            ps.setBigDecimal(4, concept.getBaseAmount());
            ps.setBoolean(5, concept.isRecurring());
            ps.setBoolean(6, concept.isActive());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            concept.setIdConcept(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert payment concept", e);
            throw new DataAccessException("No se pudo registrar el concepto de pago.", e);
        }
    }

    public void updateConcept(PaymentConcept concept) {
        String sql = "UPDATE conceptos_pago SET codigo_concepto = ?, nombre_concepto = ?, "
                + "descripcion = ?, monto_base = ?, es_recurrente = ?, esta_activo = ? "
                + "WHERE id_concepto = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, concept.getConceptCode());
            ps.setString(2, concept.getName());
            ps.setString(3, concept.getDescription());
            ps.setBigDecimal(4, concept.getBaseAmount());
            ps.setBoolean(5, concept.isRecurring());
            ps.setBoolean(6, concept.isActive());
            ps.setInt(7, concept.getIdConcept());
            DaoSupport.requireUpdated(ps.executeUpdate(), "el concepto de pago");
        } catch (SQLException e) {
            LoggerUtil.error("Could not update payment concept", e);
            throw new DataAccessException("No se pudo actualizar el concepto de pago.", e);
        }
    }

    public boolean deleteConcept(int idConcept) {
        String sql = "DELETE FROM conceptos_pago WHERE id_concepto = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idConcept);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete payment concept", e);
            throw new DataAccessException("No se pudo eliminar el concepto de pago.", e);
        }
    }

    public PaymentConcept findConceptById(int idConcept) {
        String sql = CONCEPT_SELECT + "WHERE id_concepto = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idConcept);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapConcept(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find payment concept", e);
            throw new DataAccessException("No se pudo consultar el concepto de pago.", e);
        }
    }

    public List<PaymentConcept> findAllConcepts() {
        String sql = CONCEPT_SELECT + "ORDER BY nombre_concepto";
        return queryConcepts(sql);
    }

    public List<PaymentConcept> findActiveConcepts() {
        String sql = CONCEPT_SELECT + "WHERE esta_activo = TRUE ORDER BY nombre_concepto";
        return queryConcepts(sql);
    }

    public int insertStudentConcept(StudentConcept studentConcept) {
        String sql = "INSERT INTO conceptos_estudiante (id_estudiante, id_concepto, "
                + "monto_personalizado, motivo) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE monto_personalizado = VALUES(monto_personalizado), "
                + "motivo = VALUES(motivo)";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentConcept.getIdStudent());
            ps.setInt(2, studentConcept.getIdConcept());
            ps.setBigDecimal(3, studentConcept.getCustomAmount());
            ps.setString(4, studentConcept.getReason());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            studentConcept.setIdStudentConcept(id);
            return id;
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert student concept", e);
            throw new DataAccessException("No se pudo registrar el valor personalizado.", e);
        }
    }

    public List<StudentConcept> findStudentConcepts(int idStudent) {
        String sql = "SELECT id_concepto_estudiante, id_estudiante, id_concepto, "
                + "monto_personalizado, motivo FROM conceptos_estudiante "
                + "WHERE id_estudiante = ?";
        List<StudentConcept> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentConcept(rs.getInt("id_concepto_estudiante"),
                            rs.getInt("id_estudiante"), rs.getInt("id_concepto"),
                            rs.getBigDecimal("monto_personalizado"), rs.getString("motivo")));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list student concepts", e);
            throw new DataAccessException("No se pudieron listar los valores personalizados.", e);
        }
    }

    public int insertCharge(Charge charge) {
        String sql = "INSERT INTO cargos_estudiante (id_estudiante, id_concepto, "
                + "año_academico, periodo_academico, monto, fecha_vencimiento, estado, "
                + "observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockStudentFinancialRecord(connection, charge.getIdStudent());
                int id;
                try (PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, charge.getIdStudent());
                    ps.setInt(2, charge.getIdConcept());
                    ps.setInt(3, charge.getAcademicYear());
                    ps.setString(4, charge.getAcademicPeriod());
                    ps.setBigDecimal(5, charge.getAmount());
                    DaoSupport.setDate(ps, 6, charge.getDueDate());
                    ps.setString(7, charge.getState());
                    ps.setString(8, charge.getObservations());
                    ps.executeUpdate();
                    id = DaoSupport.generatedKey(ps);
                }
                charge.setIdCharge(id);
                applyAvailableFunds(connection, charge.getIdStudent(), charge.getIdConcept(),
                        charge.getAcademicYear(), charge.getAcademicPeriod());
                connection.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert charge", e);
            throw new DataAccessException("No se pudo registrar el cargo.", e);
        }
    }

    public void updateChargeState(int idCharge, String state) {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                FinancialKey key = findChargeKey(connection, idCharge);
                if (key == null) {
                    throw new IllegalArgumentException("No se encontró el cargo.");
                }
                lockStudentFinancialRecord(connection, key.studentId());
                ChargeState charge = findChargeState(connection, idCharge);
                if (charge == null) {
                    throw new IllegalArgumentException("No se encontró el cargo.");
                }
                BigDecimal applied = appliedAmount(connection, idCharge);
                if ("Anulado".equals(state) && applied.signum() > 0) {
                    throw new IllegalArgumentException(
                            "No se puede anular un cargo con pagos aplicados.");
                }
                if ("Pagado".equals(state) && applied.compareTo(charge.amount()) < 0) {
                    throw new IllegalArgumentException(
                            "El cargo solo queda pagado al cubrir su saldo con pagos.");
                }
                if ("Pendiente".equals(state) && applied.compareTo(charge.amount()) >= 0) {
                    throw new IllegalArgumentException(
                            "No se puede marcar pendiente un cargo totalmente pagado.");
                }
                String sql = "UPDATE cargos_estudiante SET estado = ? WHERE id_cargo = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, state);
                    ps.setInt(2, idCharge);
                    DaoSupport.requireUpdated(ps.executeUpdate(), "el cargo");
                }
                if ("Pendiente".equals(state)) {
                    applyAvailableFunds(connection, key.studentId(), key.conceptId(),
                            key.academicYear(), key.academicPeriod());
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not update charge", e);
            throw new DataAccessException("No se pudo actualizar el cargo.", e);
        }
    }

    public boolean deleteCharge(int idCharge) {
        String sql = "DELETE FROM cargos_estudiante WHERE id_cargo = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCharge);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Could not delete charge", e);
            throw new DataAccessException("No se pudo eliminar el cargo.", e);
        }
    }

    public Charge findChargeById(int idCharge) {
        String sql = CHARGE_SELECT + "WHERE c.id_cargo = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCharge);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapCharge(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find charge", e);
            throw new DataAccessException("No se pudo consultar el cargo.", e);
        }
    }

    public List<Charge> findAllCharges() {
        return queryCharges(CHARGE_SELECT + "ORDER BY c.fecha_vencimiento, student_name");
    }

    public List<Charge> findChargesByStudent(int idStudent) {
        return queryCharges(CHARGE_SELECT + "WHERE c.id_estudiante = ? "
                + "ORDER BY c.fecha_vencimiento", idStudent);
    }

    public int insertPayment(Payment payment) {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockStudentFinancialRecord(connection, payment.getIdStudent());
                int id = insertPayment(connection, payment);
                Result allocation = applyAvailableFunds(connection, payment.getIdStudent(),
                        payment.getIdConcept(), payment.getAcademicYear(),
                        payment.getAcademicPeriod());
                payment.setUnappliedAmount(allocation.remainingCredits().get(id));
                connection.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert payment", e);
            throw new DataAccessException("No se pudo registrar el pago.", e);
        }
    }

    public Payment findPaymentById(int idPayment) {
        String sql = PAYMENT_SELECT + "WHERE p.id_pago = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPayment);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapPayment(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find payment", e);
            throw new DataAccessException("No se pudo consultar el pago.", e);
        }
    }

    public List<Payment> findAllPayments() {
        return queryPayments(PAYMENT_SELECT + "ORDER BY p.fecha_pago DESC, p.id_pago DESC");
    }

    public List<Payment> findPaymentsByStudent(int idStudent) {
        return queryPayments(PAYMENT_SELECT + "WHERE p.id_estudiante = ? "
                + "ORDER BY p.fecha_pago DESC", idStudent);
    }

    public List<Payment> findPaymentsByGuardian(int idParent) {
        return queryPayments(PAYMENT_SELECT + "WHERE p.id_acudiente = ? "
                + "ORDER BY p.fecha_pago DESC", idParent);
    }

    /**
     * Reserves both numbers and inserts a payment and its invoice atomically.
     *
     * @return the generated payment id.
     */
    public int registerPaymentWithInvoice(Payment payment,
                                          Function<String, Invoice> invoiceFactory) {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                lockStudentFinancialRecord(connection, payment.getIdStudent());
                int year = java.time.LocalDate.now().getYear();
                payment.setReceiptNumber(nextSequenceNumber(connection, "recibo", "REC", year));
                String invoiceNumber = nextSequenceNumber(connection, "factura", "FAC", year);
                int paymentId = insertPayment(connection, payment);
                Result allocation = applyAvailableFunds(connection, payment.getIdStudent(),
                        payment.getIdConcept(), payment.getAcademicYear(),
                        payment.getAcademicPeriod());
                payment.setUnappliedAmount(allocation.remainingCredits().get(paymentId));
                Invoice invoice = invoiceFactory.apply(invoiceNumber);
                invoice.setIdPayment(paymentId);
                insertInvoice(connection, invoice);
                connection.commit();
                LoggerUtil.info("Payment " + paymentId + " and invoice "
                        + invoice.getInvoiceNumber() + " committed");
                return paymentId;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                if (e instanceof SQLException) {
                    LoggerUtil.error("Could not register payment and invoice", e);
                    throw new DataAccessException(
                            "No se pudo registrar el pago y su factura.", e);
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Transaction failed", e);
            throw new DataAccessException("No se pudo completar la transacción.", e);
        }
    }

    public int insertInvoiceWithSequence(Function<String, Invoice> invoiceFactory) {
        try (Connection connection = connectionProvider.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int year = java.time.LocalDate.now().getYear();
                String number = nextSequenceNumber(connection, "factura", "FAC", year);
                Invoice invoice = invoiceFactory.apply(number);
                insertInvoice(connection, invoice);
                connection.commit();
                return invoice.getIdInvoice();
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
            LoggerUtil.error("Could not register invoice with sequence", e);
            throw new DataAccessException("No se pudo registrar la factura.", e);
        }
    }

    private String nextSequenceNumber(Connection connection, String sequenceName,
                                      String prefix, int year) throws SQLException {
        String incrementSql = "INSERT INTO secuencias_financieras "
                + "(nombre_secuencia, año, ultimo_valor) VALUES (?, ?, 1) "
                + "ON DUPLICATE KEY UPDATE ultimo_valor = ultimo_valor + 1";
        try (PreparedStatement ps = connection.prepareStatement(incrementSql)) {
            ps.setString(1, sequenceName);
            ps.setInt(2, year);
            ps.executeUpdate();
        }
        String selectSql = "SELECT ultimo_valor FROM secuencias_financieras "
                + "WHERE nombre_secuencia = ? AND año = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
            ps.setString(1, sequenceName);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Financial sequence row was not created");
                }
                return String.format("%s-%d-%05d", prefix, year,
                        rs.getInt("ultimo_valor"));
            }
        }
    }

    public int insertInvoice(Invoice invoice) {
        try (Connection connection = connectionProvider.getConnection()) {
            insertInvoice(connection, invoice);
            return invoice.getIdInvoice();
        } catch (SQLException e) {
            LoggerUtil.error("Could not insert invoice", e);
            throw new DataAccessException("No se pudo registrar la factura.", e);
        }
    }

    public Invoice findInvoiceByPayment(int idPayment) {
        String sql = INVOICE_SELECT + "WHERE id_pago = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPayment);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapInvoice(rs) : null;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not find invoice", e);
            throw new DataAccessException("No se pudo consultar la factura.", e);
        }
    }

    public List<Invoice> findAllInvoices() {
        String sql = INVOICE_SELECT + "ORDER BY id_factura DESC";
        List<Invoice> invoices = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                invoices.add(mapInvoice(rs));
            }
            return invoices;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list invoices", e);
            throw new DataAccessException("No se pudieron listar las facturas.", e);
        }
    }

    public java.math.BigDecimal totalPaidByStudent(int idStudent, int academicYear) {
        String sql = "SELECT COALESCE(SUM(monto_pagado), 0) FROM pagos "
                + "WHERE id_estudiante = ? AND año_academico = ?";
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Could not sum payments", e);
            throw new DataAccessException("No se pudo calcular el total pagado.", e);
        }
    }

    private void lockStudentFinancialRecord(Connection connection, int idStudent)
            throws SQLException {
        String sql = "SELECT id_estudiante FROM estudiantes WHERE id_estudiante = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idStudent);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Student does not exist: " + idStudent);
                }
            }
        }
    }

    private FinancialKey findChargeKey(Connection connection, int idCharge)
            throws SQLException {
        String sql = "SELECT id_estudiante, id_concepto, año_academico, periodo_academico "
                + "FROM cargos_estudiante WHERE id_cargo = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCharge);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new FinancialKey(rs.getInt("id_estudiante"),
                        rs.getInt("id_concepto"), rs.getInt("año_academico"),
                        rs.getString("periodo_academico")) : null;
            }
        }
    }

    private ChargeState findChargeState(Connection connection, int idCharge)
            throws SQLException {
        String sql = "SELECT monto FROM cargos_estudiante "
                + "WHERE id_cargo = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCharge);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new ChargeState(rs.getBigDecimal("monto")) : null;
            }
        }
    }

    private BigDecimal appliedAmount(Connection connection, int idCharge)
            throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto_aplicado), 0) FROM pagos_cargos "
                + "WHERE id_cargo = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCharge);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private Result applyAvailableFunds(Connection connection, int idStudent, int idConcept,
                                       int academicYear, String academicPeriod)
            throws SQLException {
        List<ChargeBalance> charges = new ArrayList<>();
        String chargeSql = "SELECT c.id_cargo, c.monto, "
                + "COALESCE(SUM(pc.monto_aplicado), 0) AS monto_aplicado "
                + "FROM cargos_estudiante c LEFT JOIN pagos_cargos pc "
                + "ON pc.id_cargo = c.id_cargo WHERE c.id_estudiante = ? "
                + "AND c.id_concepto = ? AND c.año_academico = ? "
                + "AND c.periodo_academico <=> ? AND c.estado <> 'Anulado' "
                + "GROUP BY c.id_cargo, c.monto, c.fecha_vencimiento "
                + "ORDER BY c.fecha_vencimiento, c.id_cargo";
        try (PreparedStatement ps = connection.prepareStatement(chargeSql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, idConcept);
            ps.setInt(3, academicYear);
            ps.setString(4, academicPeriod);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    charges.add(new ChargeBalance(rs.getInt("id_cargo"),
                            rs.getBigDecimal("monto"), rs.getBigDecimal("monto_aplicado")));
                }
            }
        }

        List<PaymentCredit> credits = new ArrayList<>();
        String paymentSql = "SELECT id_pago, monto_no_aplicado FROM pagos "
                + "WHERE id_estudiante = ? AND id_concepto = ? AND año_academico = ? "
                + "AND periodo_academico <=> ? AND monto_no_aplicado > 0 "
                + "ORDER BY fecha_pago, id_pago";
        try (PreparedStatement ps = connection.prepareStatement(paymentSql)) {
            ps.setInt(1, idStudent);
            ps.setInt(2, idConcept);
            ps.setInt(3, academicYear);
            ps.setString(4, academicPeriod);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    credits.add(new PaymentCredit(rs.getInt("id_pago"),
                            rs.getBigDecimal("monto_no_aplicado")));
                }
            }
        }

        Result result = PaymentAllocator.allocate(credits, charges);
        String allocationSql = "INSERT INTO pagos_cargos "
                + "(id_pago, id_cargo, monto_aplicado) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(allocationSql)) {
            for (PaymentAllocator.Application application : result.applications()) {
                ps.setInt(1, application.paymentId());
                ps.setInt(2, application.chargeId());
                ps.setBigDecimal(3, application.amount());
                ps.addBatch();
            }
            if (!result.applications().isEmpty()) {
                ps.executeBatch();
            }
        }

        String paymentUpdateSql = "UPDATE pagos SET monto_no_aplicado = ? WHERE id_pago = ?";
        try (PreparedStatement ps = connection.prepareStatement(paymentUpdateSql)) {
            for (Map.Entry<Integer, BigDecimal> credit : result.remainingCredits().entrySet()) {
                ps.setBigDecimal(1, credit.getValue());
                ps.setInt(2, credit.getKey());
                ps.addBatch();
            }
            if (!result.remainingCredits().isEmpty()) {
                ps.executeBatch();
            }
        }

        String chargeUpdateSql = "UPDATE cargos_estudiante SET estado = ? WHERE id_cargo = ? "
                + "AND estado <> 'Anulado'";
        try (PreparedStatement ps = connection.prepareStatement(chargeUpdateSql)) {
            for (Map.Entry<Integer, BigDecimal> balance : result.remainingCharges().entrySet()) {
                ps.setString(1, balance.getValue().signum() == 0 ? "Pagado" : "Pendiente");
                ps.setInt(2, balance.getKey());
                ps.addBatch();
            }
            if (!result.remainingCharges().isEmpty()) {
                ps.executeBatch();
            }
        }
        return result;
    }

    private void rollback(Connection connection, Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }

    private int insertPayment(Connection connection, Payment p) throws SQLException {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, "
                + "monto_pagado, monto_no_aplicado, fecha_pago, metodo_pago, numero_recibo, "
                + "año_academico, periodo_academico, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdStudent());
            if (p.getIdParent() > 0) {
                ps.setInt(2, p.getIdParent());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, p.getIdConcept());
            ps.setBigDecimal(4, p.getAmountPaid());
            ps.setBigDecimal(5, p.getAmountPaid());
            DaoSupport.setDate(ps, 6, p.getPaymentDate());
            ps.setString(7, p.getPaymentMethod());
            ps.setString(8, p.getReceiptNumber());
            ps.setInt(9, p.getAcademicYear());
            ps.setString(10, p.getAcademicPeriod());
            ps.setString(11, p.getNotes());
            ps.executeUpdate();
            int id = DaoSupport.generatedKey(ps);
            p.setIdPayment(id);
            p.setUnappliedAmount(p.getAmountPaid());
            return id;
        }
    }

    private void insertInvoice(Connection connection, Invoice invoice) throws SQLException {
        String sql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, "
                + "cufe, nombre_archivo_txt, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invoice.getInvoiceNumber());
            ps.setInt(2, invoice.getIdPayment());
            DaoSupport.setDate(ps, 3, invoice.getIssueDate());
            ps.setString(4, invoice.getCufe());
            ps.setString(5, invoice.getTxtFileName());
            ps.setString(6, invoice.getTxtPath());
            ps.executeUpdate();
            invoice.setIdInvoice(DaoSupport.generatedKey(ps));
        }
    }

    private record FinancialKey(int studentId, int conceptId, int academicYear,
                                String academicPeriod) { }

    private record ChargeState(BigDecimal amount) { }

    private List<PaymentConcept> queryConcepts(String sql) {
        List<PaymentConcept> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapConcept(rs));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list payment concepts", e);
            throw new DataAccessException("No se pudieron listar los conceptos de pago.", e);
        }
    }

    private List<Charge> queryCharges(String sql) {
        List<Charge> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCharge(rs));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list charges", e);
            throw new DataAccessException("No se pudieron listar los cargos.", e);
        }
    }

    private List<Charge> queryCharges(String sql, int parameter) {
        List<Charge> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCharge(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list charges", e);
            throw new DataAccessException("No se pudieron listar los cargos.", e);
        }
    }

    private List<Payment> queryPayments(String sql) {
        List<Payment> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPayment(rs));
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list payments", e);
            throw new DataAccessException("No se pudieron listar los pagos.", e);
        }
    }

    private List<Payment> queryPayments(String sql, int parameter) {
        List<Payment> list = new ArrayList<>();
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            LoggerUtil.error("Could not list payments", e);
            throw new DataAccessException("No se pudieron listar los pagos.", e);
        }
    }

    private PaymentConcept mapConcept(ResultSet rs) throws SQLException {
        PaymentConcept c = new PaymentConcept();
        c.setIdConcept(rs.getInt("id_concepto"));
        c.setConceptCode(rs.getString("codigo_concepto"));
        c.setName(rs.getString("nombre_concepto"));
        c.setDescription(rs.getString("descripcion"));
        c.setBaseAmount(rs.getBigDecimal("monto_base"));
        c.setRecurring(rs.getBoolean("es_recurrente"));
        c.setActive(rs.getBoolean("esta_activo"));
        c.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        c.setUpdateDate(DaoSupport.getTimestamp(rs, "fecha_actualizacion"));
        return c;
    }

    private Charge mapCharge(ResultSet rs) throws SQLException {
        Charge c = new Charge();
        c.setIdCharge(rs.getInt("id_cargo"));
        c.setIdStudent(rs.getInt("id_estudiante"));
        c.setIdConcept(rs.getInt("id_concepto"));
        c.setAcademicYear(rs.getInt("año_academico"));
        c.setAcademicPeriod(rs.getString("periodo_academico"));
        c.setAmount(rs.getBigDecimal("monto"));
        c.setDueDate(DaoSupport.getDate(rs, "fecha_vencimiento"));
        c.setState(rs.getString("estado"));
        c.setStudentName(rs.getString("student_name"));
        c.setConceptName(rs.getString("nombre_concepto"));
        return c;
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setIdPayment(rs.getInt("id_pago"));
        p.setIdStudent(rs.getInt("id_estudiante"));
        p.setIdParent(DaoSupport.getInt(rs, "id_acudiente"));
        p.setIdConcept(rs.getInt("id_concepto"));
        p.setAmountPaid(rs.getBigDecimal("monto_pagado"));
        p.setUnappliedAmount(rs.getBigDecimal("monto_no_aplicado"));
        p.setPaymentDate(DaoSupport.getDate(rs, "fecha_pago"));
        p.setPaymentMethod(rs.getString("metodo_pago"));
        p.setReceiptNumber(rs.getString("numero_recibo"));
        p.setAcademicYear(rs.getInt("año_academico"));
        p.setAcademicPeriod(rs.getString("periodo_academico"));
        p.setNotes(rs.getString("notas"));
        p.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        p.setStudentName(rs.getString("student_name"));
        p.setConceptName(rs.getString("nombre_concepto"));
        return p;
    }

    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        Invoice i = new Invoice();
        i.setIdInvoice(rs.getInt("id_factura"));
        i.setInvoiceNumber(rs.getString("numero_factura"));
        i.setIdPayment(rs.getInt("id_pago"));
        i.setIssueDate(DaoSupport.getDate(rs, "fecha_emision"));
        i.setCufe(rs.getString("cufe"));
        i.setTxtFileName(rs.getString("nombre_archivo_txt"));
        i.setTxtPath(rs.getString("ruta_archivo"));
        i.setCreationDate(DaoSupport.getTimestamp(rs, "fecha_creacion"));
        return i;
    }
}
