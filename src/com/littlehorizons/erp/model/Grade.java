/**
 * Domain model class for Grade.
 */
package com.littlehorizons.erp.model;

public class Grade {

    
    private int idGrade;
    private int levelNumber;  

    
    private String name;  

    
    private String creationDate;

    
    public Grade() {
    }

    
    public Grade(int idGrade) {
        this.idGrade = idGrade;
    }

    
    public Grade(int idGrade, String name, int levelNumber, String creationDate) {
        this.idGrade = idGrade;
        this.name = name;
        this.levelNumber = levelNumber;
        this.creationDate = creationDate;
    }

    
    
    

    public int getIdGrade() { return idGrade; }

    public void setIdGrade(int idGrade) { this.idGrade = idGrade; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getLevelNumber() { return levelNumber; }

    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    
    @Override
    public String toString() {
        return "Grade{" +
                "idGrade=" + idGrade +
                ", name='" + name + '\'' +
                ", levelNumber=" + levelNumber +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Grade)) return false;
        Grade that = (Grade) other;
        return name != null && name.equals(that.name);
    }
}
