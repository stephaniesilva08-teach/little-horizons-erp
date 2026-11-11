package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/** Charge or obligation generated for a student. */
public class Charge {

    private int idCharge;
    private int idStudent;
    private int idConcept;
    private int academicYear;
    private String academicPeriod;
    private BigDecimal amount;
    private String dueDate;
    private String state;
    private String observations;
    private String studentName;
    private String conceptName;

    public Charge() {
    }

    public Charge(int idCharge, int idStudent, int idConcept, int academicYear,
                  String academicPeriod, BigDecimal amount, String dueDate,
                  String state) {
        this.idCharge = idCharge;
        this.idStudent = idStudent;
        this.idConcept = idConcept;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.amount = amount;
        this.dueDate = dueDate;
        this.state = state;
    }

    public int getIdCharge() { return idCharge; }
    public void setIdCharge(int idCharge) { this.idCharge = idCharge; }
    public int getIdStudent() { return idStudent; }
    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }
    public int getIdConcept() { return idConcept; }
    public void setIdConcept(int idConcept) { this.idConcept = idConcept; }
    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }
    public String getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getConceptName() { return conceptName; }
    public void setConceptName(String conceptName) { this.conceptName = conceptName; }
}
