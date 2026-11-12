package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Charge;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.StudentConcept;

import java.math.BigDecimal;
import java.util.List;

/**
 * Persistence contract for the financial module.
 */
public interface FinanceDao {

    int insertConcept(PaymentConcept concept);

    void updateConcept(PaymentConcept concept);

    boolean deleteConcept(int idConcept);

    PaymentConcept findConceptById(int idConcept);

    List<PaymentConcept> findAllConcepts();

    List<PaymentConcept> findActiveConcepts();

    int insertStudentConcept(StudentConcept studentConcept);

    List<StudentConcept> findStudentConcepts(int idStudent);

    int insertCharge(Charge charge);

    void updateChargeState(int idCharge, String state);

    boolean deleteCharge(int idCharge);

    Charge findChargeById(int idCharge);

    List<Charge> findAllCharges();

    List<Charge> findChargesByStudent(int idStudent);

    int insertPayment(Payment payment);

    Payment findPaymentById(int idPayment);

    List<Payment> findAllPayments();

    List<Payment> findPaymentsByStudent(int idStudent);

    List<Payment> findPaymentsByGuardian(int idParent);

    String nextReceiptNumber();

    String nextInvoiceNumber();

    int registerPaymentWithInvoice(Payment payment, Invoice invoice);

    int insertInvoice(Invoice invoice);

    Invoice findInvoiceByPayment(int idPayment);

    List<Invoice> findAllInvoices();

    BigDecimal totalPaidByStudent(int idStudent, int academicYear);
}
