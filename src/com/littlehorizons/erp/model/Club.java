/**
 * Domain model class for Club.
 */
package com.littlehorizons.erp.model;

public class Club {

    
    private int idClub;

    
    private String name;            
    private String description;     

    
    private int idTeacher;          

    
    private int maxCapacity;        

    
    private String schedule;        

    
    private boolean isActive;       

    
    private String creationDate;
    private String supervisorName;
    private String updateDate;

    
    public Club() {
    }

    
    public Club(int idClub) {
        this.idClub = idClub;
    }

    
    public Club(int idClub, String name, String description, int idTeacher,
                int maxCapacity, String schedule, boolean isActive,
                String creationDate, String updateDate) {
        this.idClub = idClub;
        this.name = name;
        this.description = description;
        this.idTeacher = idTeacher;
        this.maxCapacity = maxCapacity;
        this.schedule = schedule;
        this.isActive = isActive;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    
    
    

    public int getIdClub() { return idClub; }

    public void setIdClub(int idClub) { this.idClub = idClub; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public int getIdTeacher() { return idTeacher; }

    public void setIdTeacher(int idTeacher) { this.idTeacher = idTeacher; }

    public int getMaxCapacity() { return maxCapacity; }

    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getSchedule() { return schedule; }

    public void setSchedule(String schedule) { this.schedule = schedule; }

    public boolean isActive() { return isActive; }

    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String getCreationDate() { return creationDate; }
    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getUpdateDate() { return updateDate; }

    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    
    @Override
    public String toString() {
        return "Club{" +
                "idClub=" + idClub +
                ", name='" + name + '\'' +
                ", maxCapacity=" + maxCapacity +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Club)) return false;
        Club that = (Club) other;
        return name != null && name.equals(that.name);
    }
}
