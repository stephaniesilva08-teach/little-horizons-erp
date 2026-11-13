/**
 * Domain model class for Payment.
 */
package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.util.Date;

public class Payment {

    
    private int idPayment;
    private int idStudent;
    private int idParent;
    private int idConcept;

    
    private BigDecimal amountPaid;        
    private BigDecimal unappliedAmount = BigDecimal.ZERO;
    private String paymentDate;           
    private String paymentMethod;         
    private String receiptNumber;         

    
    private int academicYear;             
    private String academicPeriod;        

    
    private String notes;

    
    private String creationDate;
    private String studentName;
    private String conceptName;

    
    public Payment() {
    }

    
    public Payment(int idPayment) {
        this.idPayment = idPayment;
    }

    
    public Payment(int idPayment, int idStudent, int idParent,
                   int idConcept, BigDecimal amountPaid, String paymentDate,
                   String paymentMethod, String receiptNumber,
                   int academicYear, String academicPeriod,
                   String notes, String creationDate) {
        this.idPayment = idPayment;
        this.idStudent = idStudent;
        this.idParent = idParent;
        this.idConcept = idConcept;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.notes = notes;
        this.creationDate = creationDate;
    }

    
    
    

    public int getIdPayment() { return idPayment; }

    public void setIdPayment(int idPayment) { this.idPayment = idPayment; }

    public int getIdStudent() { return idStudent; }

    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }

    public int getIdParent() { return idParent; }

    public void setIdParent(int idParent) { this.idParent = idParent; }

    public int getIdConcept() { return idConcept; }

    public void setIdConcept(int idConcept) { this.idConcept = idConcept; }

    public BigDecimal getAmountPaid() { return amountPaid; }

    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getUnappliedAmount() { return unappliedAmount; }

    public void setUnappliedAmount(BigDecimal unappliedAmount) {
        this.unappliedAmount = unappliedAmount == null ? BigDecimal.ZERO : unappliedAmount;
    }

    public String getPaymentDate() { return paymentDate; }

    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }

    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReceiptNumber() { return receiptNumber; }

    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public int getAcademicYear() { return academicYear; }

    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getAcademicPeriod() { return academicPeriod; }

    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public String getCreationDate() { return creationDate; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getConceptName() { return conceptName; }
    public void setConceptName(String conceptName) { this.conceptName = conceptName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    
    @Override
    public String toString() {
        return "Payment{" +
                "idPayment=" + idPayment +
                ", studentId=" + idStudent +
                ", conceptCode='" + idConcept + '\'' +
                ", amount=" + amountPaid +
                ", date='" + paymentDate + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Payment that)) return false;
        return idPayment > 0 && idPayment == that.idPayment;
    }

    @Override
    public int hashCode() {
        return idPayment > 0 ? Integer.hashCode(idPayment)
                : System.identityHashCode(this);
    }
}
