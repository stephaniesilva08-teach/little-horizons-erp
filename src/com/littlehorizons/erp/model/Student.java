/**
 * Domain model class for Student.
 */
package com.littlehorizons.erp.model;

import com.littlehorizons.erp.util.NameFormatter;

public class Student {

    
    private int idStudent;
    private String documentType;
    private String documentNumber;

    
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;

    
    private String birthDate;

    
    private String gender;
    private String address;
    private String phone;
    private String email;

    
    private String bloodType;
    private String allergies;
    private String medicalConditions;

    
    private String enrollmentState;  
    private String entryDate;

    
    private String creationDate;
    private String updateDate;

    
    public Student() {
    }

    
    public Student(int idStudent) {
        this.idStudent = idStudent;
    }

    
    public Student(int idStudent, String documentType, String documentNumber,
                   String firstName, String secondName, String firstLastName,
                   String secondLastName, String birthDate, String gender,
                   String address, String phone, String email,
                   String bloodType, String allergies, String medicalConditions,
                   String enrollmentState, String entryDate,
                   String creationDate, String updateDate) {
        this.idStudent = idStudent;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.secondName = secondName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.medicalConditions = medicalConditions;
        this.enrollmentState = enrollmentState;
        this.entryDate = entryDate;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    
    
    

    public int getIdStudent() { return idStudent; }

    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }

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

    public String getBirthDate() { return birthDate; }

    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }

    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getBloodType() { return bloodType; }

    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getAllergies() { return allergies; }

    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getMedicalConditions() { return medicalConditions; }

    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }

    public String getEnrollmentState() { return enrollmentState; }

    public void setEnrollmentState(String enrollmentState) { this.enrollmentState = enrollmentState; }

    public String getEntryDate() { return entryDate; }

    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getUpdateDate() { return updateDate; }

    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    
    public String getFullName() {
        return NameFormatter.fullName(firstName, secondName, firstLastName, secondLastName);
    }

    
    @Override
    public String toString() {
        return "Student{" +
                "idStudent=" + idStudent +
                ", fullName='" + getFullName() + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", enrollmentState='" + enrollmentState + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Student that)) return false;
        return idStudent > 0 && idStudent == that.idStudent;
    }

    @Override
    public int hashCode() {
        return idStudent > 0 ? Integer.hashCode(idStudent)
                : System.identityHashCode(this);
    }
}
