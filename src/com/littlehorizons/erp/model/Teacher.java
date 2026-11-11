/**
 * Domain model class for Teacher.
 */
package com.littlehorizons.erp.model;

public class Teacher {

    
    private int idTeacher;
    private String documentType;
    private String documentNumber;

    
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;

    
    private String email;
    private String phone;
    private String specialization;  

    
    private String hireDate;        

    
    private String state;           

    
    private String creationDate;
    private String updateDate;

    
    public Teacher() {
    }

    
    public Teacher(int idTeacher) {
        this.idTeacher = idTeacher;
    }

    
    public Teacher(int idTeacher, String documentType, String documentNumber,
                   String firstName, String secondName, String firstLastName,
                   String secondLastName, String email, String phone,
                   String specialization, String hireDate, String state,
                   String creationDate, String updateDate) {
        this.idTeacher = idTeacher;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.secondName = secondName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.email = email;
        this.phone = phone;
        this.specialization = specialization;
        this.hireDate = hireDate;
        this.state = state;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    
    
    

    public int getIdTeacher() { return idTeacher; }

    public void setIdTeacher(int idTeacher) { this.idTeacher = idTeacher; }

    public String getDocumentType() { return documentType; }

    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentNumber() { return documentNumber; }

    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getSecondName() { return secondName; }

    public void setSecondName(String secondName) { this.secondName = secondName; }

    public String getFirstLastName() { return firstLastName; }

    public void setFirstLastName(String firstLastName) { this.firstLastName = firstLastName; }

    public String getSecondLastName() { return secondLastName; }

    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }

    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getHireDate() { return hireDate; }

    public void setHireDate(String hireDate) { this.hireDate = hireDate; }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getUpdateDate() { return updateDate; }

    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    
    public String getFullName() {
        return firstName + " " + secondName + " " + firstLastName + " " + secondLastName;
    }

    
    @Override
    public String toString() {
        return "Teacher{" +
                "idTeacher=" + idTeacher +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", specialization='" + specialization + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Teacher)) return false;
        Teacher that = (Teacher) other;
        return documentNumber != null && that.documentNumber != null
                && documentNumber.equals(that.documentNumber);
    }
}
