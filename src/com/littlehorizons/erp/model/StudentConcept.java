package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/** Personalized price for a payment concept and a specific student. */
public class StudentConcept {

    private int idStudentConcept;
    private int idStudent;
    private int idConcept;
    private BigDecimal customAmount;
    private String reason;

    public StudentConcept() {
    }

    public StudentConcept(int idStudentConcept, int idStudent, int idConcept,
                          BigDecimal customAmount, String reason) {
        this.idStudentConcept = idStudentConcept;
        this.idStudent = idStudent;
        this.idConcept = idConcept;
        this.customAmount = customAmount;
        this.reason = reason;
    }

    public int getIdStudentConcept() { return idStudentConcept; }
    public void setIdStudentConcept(int idStudentConcept) { this.idStudentConcept = idStudentConcept; }
    public int getIdStudent() { return idStudent; }
    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }
    public int getIdConcept() { return idConcept; }
    public void setIdConcept(int idConcept) { this.idConcept = idConcept; }
    public BigDecimal getCustomAmount() { return customAmount; }
    public void setCustomAmount(BigDecimal customAmount) { this.customAmount = customAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
