/**
 * Domain model class for Loan.
 */
package com.littlehorizons.erp.model;

import java.util.Date;

public class Loan {

    
    private int idLoan;

    
    private int idResource;       
    private int idStudent;        
    private int idTeacher;        

    
    private String loanDate;              
    private String expectedReturnDate;    
    private String actualReturnDate;      

    
    private String state;         

    
    private String observations;

    
    private String creationDate;
    private String resourceName;
    private String borrowerName;
    private String updateDate;

    
    public Loan() {
    }

    
    public Loan(int idLoan) {
        this.idLoan = idLoan;
    }

    
    public Loan(int idLoan, int idResource, int idStudent,
                String loanDate, String expectedReturnDate,
                String actualReturnDate, String state,
                String observations, String creationDate, String updateDate) {
        this.idLoan = idLoan;
        this.idResource = idResource;
        this.idStudent = idStudent;
        this.idTeacher = 0;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.state = state;
        this.observations = observations;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    
    
    

    public int getIdLoan() { return idLoan; }

    public void setIdLoan(int idLoan) { this.idLoan = idLoan; }

    public int getIdResource() { return idResource; }

    public void setIdResource(int idResource) { this.idResource = idResource; }

    public int getIdStudent() { return idStudent; }

    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }

    public int getIdTeacher() { return idTeacher; }

    public void setIdTeacher(int idTeacher) { this.idTeacher = idTeacher; }

    public String getLoanDate() { return loanDate; }

    public void setLoanDate(String loanDate) { this.loanDate = loanDate; }

    public String getExpectedReturnDate() { return expectedReturnDate; }

    public void setExpectedReturnDate(String expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public String getActualReturnDate() { return actualReturnDate; }

    public void setActualReturnDate(String actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getObservations() { return observations; }

    public void setObservations(String observations) { this.observations = observations; }

    public String getCreationDate() { return creationDate; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getUpdateDate() { return updateDate; }

    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    
    @Override
    public String toString() {
        return "Loan{" +
                "idLoan=" + idLoan +
                ", resourceId=" + idResource +
                ", borrowerId=" + (idStudent > 0 ? idStudent : idTeacher) +
                ", state='" + state + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Loan that)) return false;
        return idLoan > 0 && idLoan == that.idLoan;
    }

    @Override
    public int hashCode() {
        return idLoan > 0 ? Integer.hashCode(idLoan)
                : System.identityHashCode(this);
    }
}
