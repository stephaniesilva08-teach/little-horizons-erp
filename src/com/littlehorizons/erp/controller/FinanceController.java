package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.FinanceDao;
import com.littlehorizons.erp.dao.JdbcFinanceDao;
import com.littlehorizons.erp.model.Charge;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Parent;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentConcept;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.TextInvoiceGenerator;
import com.littlehorizons.erp.util.Validator;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Business rules for the financial module, including invoice generation.
 */
public class FinanceController {

    private final FinanceDao financeDao;

    public FinanceController() {
        this(new JdbcFinanceDao());
    }

    public FinanceController(FinanceDao financeDao) {
        this.financeDao = financeDao;
    }

    public List<PaymentConcept> listActiveConcepts() {
        return financeDao.findActiveConcepts();
    }

    public List<PaymentConcept> listAllConcepts() {
        return financeDao.findAllConcepts();
    }

    public PaymentConcept findConcept(int idConcept) {
        return financeDao.findConceptById(idConcept);
    }

    public int createConcept(PaymentConcept concept) {
        if (!Validator.isNotEmpty(concept.getConceptCode())) {
            throw new IllegalArgumentException("El código del concepto es obligatorio.");
        }
        if (!Validator.isValidName(concept.getName())) {
            throw new IllegalArgumentException("El nombre del concepto es inválido.");
        }
        if (concept.getBaseAmount() == null
                || concept.getBaseAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto base no puede ser negativo.");
        }
        return financeDao.insertConcept(concept);
    }

    public void updateConcept(PaymentConcept concept) {
        if (concept.getIdConcept() <= 0) {
            throw new IllegalArgumentException("Falta el identificador del concepto.");
        }
        financeDao.updateConcept(concept);
    }

    public void removeConcept(int idConcept) {
        if (!financeDao.deleteConcept(idConcept)) {
            throw new IllegalArgumentException("No se encontró el concepto a eliminar.");
        }
    }

    public void setStudentConcept(int idStudent, int idConcept, BigDecimal amount,
                                  String reason) {
        if (!Validator.isValidAmount(amount)) {
            throw new IllegalArgumentException("El monto personalizado debe ser positivo.");
        }
        financeDao.insertStudentConcept(new StudentConcept(0, idStudent, idConcept,
                amount, reason));
    }

    public List<StudentConcept> listStudentConcepts(int idStudent) {
        return financeDao.findStudentConcepts(idStudent);
    }

    public BigDecimal resolveAmount(int idStudent, PaymentConcept concept) {
        return financeDao.findStudentConcepts(idStudent).stream()
                .filter(sc -> sc.getIdConcept() == concept.getIdConcept())
                .map(StudentConcept::getCustomAmount)
                .findFirst()
                .orElse(concept.getBaseAmount());
    }

    public int createCharge(Charge charge) {
        if (charge.getIdStudent() <= 0 || charge.getIdConcept() <= 0) {
            throw new IllegalArgumentException("Estudiante y concepto son obligatorios.");
        }
        if (!Validator.isInRange(charge.getAcademicYear(), 2000, 2100)) {
            throw new IllegalArgumentException("El año académico es inválido.");
        }
        if (!Validator.isOneOf(charge.getAcademicPeriod(), "I", "II", "III", "IV",
                "Anual")) {
            throw new IllegalArgumentException("El período es inválido.");
        }
        if (!Validator.isValidAmount(charge.getAmount())) {
            throw new IllegalArgumentException("El monto del cargo debe ser positivo.");
        }
        if (!Validator.isValidDate(charge.getDueDate())) {
            throw new IllegalArgumentException("La fecha de vencimiento es inválida.");
        }
        charge.setState("Pendiente");
        return financeDao.insertCharge(charge);
    }

    public List<Charge> listCharges() {
        return financeDao.findAllCharges();
    }

    public List<Charge> listChargesByStudent(int idStudent) {
        return financeDao.findChargesByStudent(idStudent);
    }

    public void updateChargeState(int idCharge, String state) {
        if (!Validator.isOneOf(state, "Pendiente", "Pagado", "Anulado")) {
            throw new IllegalArgumentException("Estado del cargo inválido.");
        }
        financeDao.updateChargeState(idCharge, state);
    }

    public void removeCharge(int idCharge) {
        if (!financeDao.deleteCharge(idCharge)) {
            throw new IllegalArgumentException("No se encontró el cargo a eliminar.");
        }
    }

    /**
     * Registers a payment and generates its .txt invoice in one atomic operation.
     */
    public Invoice registerPayment(Payment payment, Student student, Parent guardian) {
        validatePayment(payment);
        PaymentConcept concept = financeDao.findConceptById(payment.getIdConcept());
        if (concept == null) {
            throw new IllegalArgumentException("El concepto de pago no existe.");
        }
        if (student == null) {
            throw new IllegalArgumentException("El estudiante no existe.");
        }
        payment.setReceiptNumber(financeDao.nextReceiptNumber());
        String invoiceNumber = financeDao.nextInvoiceNumber();
        TextInvoiceGenerator.GeneratedInvoice generated =
                TextInvoiceGenerator.generate(invoiceNumber, student.getFullName(),
                        guardian == null ? "No registrado" : guardian.getFullName(),
                        guardian == null ? "N/A" : guardian.getDocumentNumber(),
                        guardian == null || !Validator.isNotEmpty(guardian.getAddress())
                                ? "No registrada" : guardian.getAddress(),
                        concept.getConceptCode(), concept.getName(), 1,
                        payment.getAmountPaid());
        Invoice invoice = buildInvoice(invoiceNumber, generated, payment.getIdPayment());
        try {
            financeDao.registerPaymentWithInvoice(payment, invoice);
        } catch (RuntimeException e) {
            deleteInvoiceFile(generated.fullPath());
            throw e;
        }
        return financeDao.findInvoiceByPayment(payment.getIdPayment());
    }

    public Invoice generateInvoiceForPayment(Payment payment, Student student,
                                             Parent guardian) {
        if (financeDao.findInvoiceByPayment(payment.getIdPayment()) != null) {
            throw new IllegalArgumentException("El pago ya tiene una factura asociada.");
        }
        PaymentConcept concept = financeDao.findConceptById(payment.getIdConcept());
        if (concept == null) {
            throw new IllegalArgumentException("El concepto de pago no existe.");
        }
        String invoiceNumber = financeDao.nextInvoiceNumber();
        TextInvoiceGenerator.GeneratedInvoice generated =
                TextInvoiceGenerator.generate(invoiceNumber, student.getFullName(),
                        guardian == null ? "No registrado" : guardian.getFullName(),
                        guardian == null ? "N/A" : guardian.getDocumentNumber(),
                        guardian == null || !Validator.isNotEmpty(guardian.getAddress())
                                ? "No registrada" : guardian.getAddress(),
                        concept.getConceptCode(), concept.getName(), 1,
                        payment.getAmountPaid());
        Invoice invoice = buildInvoice(invoiceNumber, generated, payment.getIdPayment());
        try {
            financeDao.insertInvoice(invoice);
        } catch (RuntimeException e) {
            deleteInvoiceFile(generated.fullPath());
            throw e;
        }
        return financeDao.findInvoiceByPayment(payment.getIdPayment());
    }

    public List<Payment> listPayments() {
        return financeDao.findAllPayments();
    }

    public List<Payment> listPaymentsByStudent(int idStudent) {
        return financeDao.findPaymentsByStudent(idStudent);
    }

    public List<Payment> listPaymentsByGuardian(int idParent) {
        if (idParent <= 0) {
            throw new IllegalArgumentException("El acudiente es obligatorio.");
        }
        return financeDao.findPaymentsByGuardian(idParent);
    }

    public Payment findPayment(int idPayment) {
        return financeDao.findPaymentById(idPayment);
    }

    public List<Invoice> listInvoices() {
        return financeDao.findAllInvoices();
    }

    public Invoice findInvoiceByPayment(int idPayment) {
        return financeDao.findInvoiceByPayment(idPayment);
    }

    public String nextReceiptNumber() {
        return financeDao.nextReceiptNumber();
    }

    private void deleteInvoiceFile(String fullPath) {
        try {
            Files.deleteIfExists(Path.of(fullPath));
            LoggerUtil.warn("Rolled back invoice file: " + fullPath);
        } catch (IOException e) {
            LoggerUtil.warn("Could not delete rolled back invoice file: " + fullPath);
        }
    }

    private Invoice buildInvoice(String invoiceNumber,
                                 TextInvoiceGenerator.GeneratedInvoice generated,
                                 int idPayment) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setIdPayment(idPayment);
        invoice.setIssueDate(LocalDate.now().toString());
        invoice.setCufe(generated.cufe());
        invoice.setTxtFileName(generated.fileName());
        invoice.setTxtPath(generated.fullPath());
        return invoice;
    }

    private void validatePayment(Payment p) {
        if (p.getIdStudent() <= 0 || p.getIdConcept() <= 0) {
            throw new IllegalArgumentException("Estudiante y concepto son obligatorios.");
        }
        if (!Validator.isValidAmount(p.getAmountPaid())) {
            throw new IllegalArgumentException("El monto pagado debe ser positivo.");
        }
        if (!Validator.isValidDate(p.getPaymentDate())) {
            throw new IllegalArgumentException("La fecha de pago es inválida.");
        }
        if (!Validator.isOneOf(p.getPaymentMethod(), "Efectivo", "Transferencia",
                "Tarjeta Débito", "Tarjeta Crédito", "Cheque")) {
            throw new IllegalArgumentException("Método de pago inválido.");
        }
        if (!Validator.isInRange(p.getAcademicYear(), 2000, 2100)) {
            throw new IllegalArgumentException("El año académico es inválido.");
        }
        if (!Validator.isOneOf(p.getAcademicPeriod(), "I", "II", "III", "IV",
                "Anual")) {
            throw new IllegalArgumentException("El período académico es inválido.");
        }
    }
}
