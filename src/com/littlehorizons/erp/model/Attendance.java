/**
 * Domain model class for Attendance.
 */
package com.littlehorizons.erp.model;

public class Attendance {

    
    private int idAttendance;
    private int idStudent;
    private int idSubject;

    
    private String attendanceDate;   
    private String state;            
    private String observations;     

    
    private int registeredBy;        

    
    private String creationDate;
    private String studentName;
    private String subjectName;

    
    public Attendance() {
    }

    
    public Attendance(int idAttendance) {
        this.idAttendance = idAttendance;
    }

    
    public Attendance(int idAttendance, int idStudent, int idSubject,
                      String attendanceDate, String state,
                      String observations, int registeredBy,
                      String creationDate) {
        this.idAttendance = idAttendance;
        this.idStudent = idStudent;
        this.idSubject = idSubject;
        this.attendanceDate = attendanceDate;
        this.state = state;
        this.observations = observations;
        this.registeredBy = registeredBy;
        this.creationDate = creationDate;
    }

    
    
    

    public int getIdAttendance() { return idAttendance; }

    public void setIdAttendance(int idAttendance) { this.idAttendance = idAttendance; }

    public int getIdStudent() { return idStudent; }

    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }

    public int getIdSubject() { return idSubject; }

    public void setIdSubject(int idSubject) { this.idSubject = idSubject; }

    public String getAttendanceDate() { return attendanceDate; }

    public void setAttendanceDate(String attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getObservations() { return observations; }

    public void setObservations(String observations) { this.observations = observations; }

    public int getRegisteredBy() { return registeredBy; }

    public void setRegisteredBy(int registeredBy) { this.registeredBy = registeredBy; }

    public String getCreationDate() { return creationDate; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    
    @Override
    public String toString() {
        return "Attendance{" +
                "idAttendance=" + idAttendance +
                ", studentId=" + idStudent +
                ", date='" + attendanceDate + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Attendance that)) return false;
        return idAttendance > 0 && idAttendance == that.idAttendance;
    }

    @Override
    public int hashCode() {
        return idAttendance > 0 ? Integer.hashCode(idAttendance)
                : System.identityHashCode(this);
    }
}
