/**
 * Domain model class for Parent.
 */
package com.littlehorizons.erp.model;

public class Parent {

    
    private int idParent;
    private String documentType;
    private String documentNumber;

    
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;

    
    private String relationship;      
    private String phonePrimary;
    private String phoneSecondary;
    private String email;
    private String address;
    private String occupation;

    
    private boolean isFinancialResponsible;  

    
    private String creationDate;
    private String updateDate;

    
    public Parent() {
    }

    
    public Parent(int idParent) {
        this.idParent = idParent;
    }

    
    public Parent(int idParent, String documentType, String documentNumber,
                  String firstName, String secondName, String firstLastName,
                  String secondLastName, String relationship,
                  String phonePrimary, String phoneSecondary, String email,
                  String address, String occupation, boolean isFinancialResponsible,
                  String creationDate, String updateDate) {
        this.idParent = idParent;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.secondName = secondName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.relationship = relationship;
        this.phonePrimary = phonePrimary;
        this.phoneSecondary = phoneSecondary;
        this.email = email;
        this.address = address;
        this.occupation = occupation;
        this.isFinancialResponsible = isFinancialResponsible;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    
    
    

    public int getIdParent() { return idParent; }

    public void setIdParent(int idParent) { this.idParent = idParent; }

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

    public String getRelationship() { return relationship; }

    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPhonePrimary() { return phonePrimary; }

    public void setPhonePrimary(String phonePrimary) { this.phonePrimary = phonePrimary; }

    public String getPhoneSecondary() { return phoneSecondary; }

    public void setPhoneSecondary(String phoneSecondary) { this.phoneSecondary = phoneSecondary; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getOccupation() { return occupation; }

    public void setOccupation(String occupation) { this.occupation = occupation; }

    public boolean isIsFinancialResponsible() { return isFinancialResponsible; }

    public void setIsFinancialResponsible(boolean isFinancialResponsible) {
        this.isFinancialResponsible = isFinancialResponsible;
    }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getUpdateDate() { return updateDate; }

    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    
    public String getFullName() {
        return firstName + " " + secondName + " " + firstLastName + " " + secondLastName;
    }

    
    @Override
    public String toString() {
        return "Parent{" +
                "idParent=" + idParent +
                ", fullName='" + getFullName() + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Parent)) return false;
        Parent that = (Parent) other;
        return documentNumber != null && that.documentNumber != null
                && documentNumber.equals(that.documentNumber);
    }
}
