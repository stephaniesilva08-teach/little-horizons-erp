/**
 * Domain model class for Subject.
 */
package com.littlehorizons.erp.model;

public class Subject {

    
    private int idSubject;
    private String subjectCode;   

    
    private String name;          
    private String description;   

    
    private int idGrade;          

    
    private int weeklyHours;      
    private boolean isActive;     

    
    private String creationDate;

    
    public Subject() {
    }

    
    public Subject(int idSubject) {
        this.idSubject = idSubject;
    }

    
    public Subject(int idSubject, String subjectCode, String name,
                   String description, int idGrade, int weeklyHours,
                   boolean isActive, String creationDate) {
        this.idSubject = idSubject;
        this.subjectCode = subjectCode;
        this.name = name;
        this.description = description;
        this.idGrade = idGrade;
        this.weeklyHours = weeklyHours;
        this.isActive = isActive;
        this.creationDate = creationDate;
    }

    
    
    

    public int getIdSubject() { return idSubject; }

    public void setIdSubject(int idSubject) { this.idSubject = idSubject; }

    public String getSubjectCode() { return subjectCode; }

    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public int getIdGrade() { return idGrade; }

    public void setIdGrade(int idGrade) { this.idGrade = idGrade; }

    public int getWeeklyHours() { return weeklyHours; }

    public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }

    public boolean isActive() { return isActive; }

    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    
    @Override
    public String toString() {
        return "Subject{" +
                "idSubject=" + idSubject +
                ", code='" + subjectCode + '\'' +
                ", name='" + name + '\'' +
                ", weeklyHours=" + weeklyHours +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Subject that)) return false;
        return idSubject > 0 && idSubject == that.idSubject;
    }

    @Override
    public int hashCode() {
        return idSubject > 0 ? Integer.hashCode(idSubject)
                : System.identityHashCode(this);
    }
}
