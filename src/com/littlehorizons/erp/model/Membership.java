/**
 * Domain model class for Membership.
 */
package com.littlehorizons.erp.model;

public class Membership {

    
    private int idMembership;

    
    private int idStudent;        
    private int idClub;           

    
    private String entryDate;     

    
    private String state;         

    
    private String withdrawalReason;  

    
    private String creationDate;
    private String studentName;
    private String clubName;

    
    public Membership() {
    }

    
    public Membership(int idMembership) {
        this.idMembership = idMembership;
    }

    
    public Membership(int idMembership, int idStudent, int idClub,
                      String entryDate, String state,
                      String withdrawalReason, String creationDate) {
        this.idMembership = idMembership;
        this.idStudent = idStudent;
        this.idClub = idClub;
        this.entryDate = entryDate;
        this.state = state;
        this.withdrawalReason = withdrawalReason;
        this.creationDate = creationDate;
    }

    
    
    

    public int getIdMembership() { return idMembership; }

    public void setIdMembership(int idMembership) { this.idMembership = idMembership; }

    public int getIdStudent() { return idStudent; }

    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }

    public int getIdClub() { return idClub; }

    public void setIdClub(int idClub) { this.idClub = idClub; }

    public String getEntryDate() { return entryDate; }

    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getWithdrawalReason() { return withdrawalReason; }

    public void setWithdrawalReason(String withdrawalReason) { this.withdrawalReason = withdrawalReason; }

    public String getCreationDate() { return creationDate; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    
    @Override
    public String toString() {
        return "Membership{" +
                "idMembership=" + idMembership +
                ", studentId=" + idStudent +
                ", clubId=" + idClub +
                ", state='" + state + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Membership)) return false;
        Membership that = (Membership) other;
        return idStudent == that.idStudent
                && idClub == that.idClub
                && entryDate.equals(that.entryDate);
    }
}
