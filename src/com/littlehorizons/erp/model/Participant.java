/**
 * Domain model class for Participant.
 */
package com.littlehorizons.erp.model;

public class Participant {

    
    private int idParticipation;

    
    private int idEvent;          
    private int idStudent;        

    
    private String role;          

    
    private String position;      

    
    private String observations;

    
    private String creationDate;
    private String studentName;
    private String eventName;

    
    public Participant() {
    }

    
    public Participant(int idParticipation) {
        this.idParticipation = idParticipation;
    }

    
    public Participant(int idParticipation, int idEvent, int idStudent,
                       String role, String position, String observations,
                       String creationDate) {
        this.idParticipation = idParticipation;
        this.idEvent = idEvent;
        this.idStudent = idStudent;
        this.role = role;
        this.position = position;
        this.observations = observations;
        this.creationDate = creationDate;
    }

    
    
    

    public int getIdParticipation() { return idParticipation; }

    public void setIdParticipation(int idParticipation) { this.idParticipation = idParticipation; }

    public int getIdEvent() { return idEvent; }

    public void setIdEvent(int idEvent) { this.idEvent = idEvent; }

    public int getIdStudent() { return idStudent; }

    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public String getPosition() { return position; }

    public void setPosition(String position) { this.position = position; }

    public String getObservations() { return observations; }

    public void setObservations(String observations) { this.observations = observations; }

    public String getCreationDate() { return creationDate; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    
    @Override
    public String toString() {
        return "Participant{" +
                "idParticipation=" + idParticipation +
                ", studentId=" + idStudent +
                ", role='" + role + '\'' +
                ", position='" + position + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Participant)) return false;
        Participant that = (Participant) other;
        return idEvent == that.idEvent
                && idStudent == that.idStudent
                && creationDate.equals(that.creationDate);
    }
}
