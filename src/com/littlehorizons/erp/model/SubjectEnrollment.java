package com.littlehorizons.erp.model;

/** Enrollment of a student in a subject for an academic period. */
public class SubjectEnrollment {

    private int idEnrollment;
    private int idStudent;
    private int idSubject;
    private int academicYear;
    private String academicPeriod;
    private String section;
    private String enrollmentDate;
    private String state;
    private String studentName;
    private String subjectName;
    private String creationDate;

    public SubjectEnrollment() {
    }

    public SubjectEnrollment(int idEnrollment) {
        this.idEnrollment = idEnrollment;
    }

    public SubjectEnrollment(int idEnrollment, int idStudent, int idSubject,
                             int academicYear, String academicPeriod,
                             String section, String state) {
        this.idEnrollment = idEnrollment;
        this.idStudent = idStudent;
        this.idSubject = idSubject;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.section = section;
        this.state = state;
    }

    public int getIdEnrollment() { return idEnrollment; }
    public void setIdEnrollment(int idEnrollment) { this.idEnrollment = idEnrollment; }
    public int getIdStudent() { return idStudent; }
    public void setIdStudent(int idStudent) { this.idStudent = idStudent; }
    public int getIdSubject() { return idSubject; }
    public void setIdSubject(int idSubject) { this.idSubject = idSubject; }
    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }
    public String getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(String enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    @Override
    public String toString() {
        return "SubjectEnrollment{" +
                "idEnrollment=" + idEnrollment +
                ", student='" + studentName + '\'' +
                ", subject='" + subjectName + '\'' +
                ", period='" + academicPeriod + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
