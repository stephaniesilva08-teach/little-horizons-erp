package com.littlehorizons.erp.model;

/** Annual enrollment (matriculation) of a student in a grade. */
public class Matriculation {

    private int idMatriculation;
    private int idStudent;
    private int idGrade;
    private int academicYear;
    private String state;
    private String enrollmentDate;
    private String withdrawalDate;
    private String observations;
    private String studentName;
    private String gradeName;

    public Matriculation() {
    }

    public Matriculation(int idMatriculation, int idStudent, int idGrade,
                         int academicYear, String state, String enrollmentDate,
                         String withdrawalDate, String observations) {
        this.idMatriculation = idMatriculation;
        this.idStudent = idStudent;
        this.idGrade = idGrade;
        this.academicYear = academicYear;
        this.state = state;
        this.enrollmentDate = enrollmentDate;
        this.withdrawalDate = withdrawalDate;
        this.observations = observations;
    }

    public int getIdMatriculation() { return idMatriculation; }
    public void setIdMatriculation(int idMatriculation) { this.idMatriculation = idMatriculation; }
    public int getIdStudent() { return idStudent; }
    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }
    public int getIdGrade() { return idGrade; }
    public void setIdGrade(int idGrade) { this.idGrade = idGrade; }
    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(String enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public String getWithdrawalDate() { return withdrawalDate; }
    public void setWithdrawalDate(String withdrawalDate) { this.withdrawalDate = withdrawalDate; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }
}
