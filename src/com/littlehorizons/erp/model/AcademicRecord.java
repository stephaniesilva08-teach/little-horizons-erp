/**
 * Domain model class for AcademicRecord.
 */
package com.littlehorizons.erp.model;

import java.math.BigDecimal;

public class AcademicRecord {

    
    private int idRecord;
    private int idStudent;
    private int idSubject;
    private int idTeacher;

    
    private int academicYear;              
    private String academicPeriod;         
    private BigDecimal grade;              

    
    private String observations;

    
    private String recordDate;

    
    private String creationDate;
    private String studentName;
    private String subjectName;
    private String teacherName;
    private String updateDate;

    
    public AcademicRecord() {
    }

    
    public AcademicRecord(int idRecord) {
        this.idRecord = idRecord;
    }

    
    public AcademicRecord(int idRecord, int idStudent, int idSubject,
                          int idTeacher, int academicYear, String academicPeriod,
                          BigDecimal grade, String observations,
                          String recordDate, String creationDate, String updateDate) {
        this.idRecord = idRecord;
        this.idStudent = idStudent;
        this.idSubject = idSubject;
        this.idTeacher = idTeacher;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.grade = grade;
        this.observations = observations;
        this.recordDate = recordDate;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    
    
    

    public int getIdRecord() { return idRecord; }

    public void setIdRecord(int idRecord) { this.idRecord = idRecord; }

    public int getIdStudent() { return idStudent; }

    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }

    public int getIdSubject() { return idSubject; }

    public void setIdSubject(int idSubject) { this.idSubject = idSubject; }

    public int getIdTeacher() { return idTeacher; }

    public void setIdTeacher(int idTeacher) { this.idTeacher = idTeacher; }

    public int getAcademicYear() { return academicYear; }

    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getAcademicPeriod() { return academicPeriod; }

    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }

    public BigDecimal getGrade() { return grade; }

    public void setGrade(BigDecimal grade) { this.grade = grade; }

    public String getObservations() { return observations; }

    public void setObservations(String observations) { this.observations = observations; }

    public String getRecordDate() { return recordDate; }

    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }

    public String getCreationDate() { return creationDate; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getUpdateDate() { return updateDate; }

    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    
    @Override
    public String toString() {
        return "AcademicRecord{" +
                "idRecord=" + idRecord +
                ", studentId=" + idStudent +
                ", subjectId=" + idSubject +
                ", grade=" + grade +
                ", academicPeriod='" + academicPeriod + '\'' +
                '}';
    }
}
