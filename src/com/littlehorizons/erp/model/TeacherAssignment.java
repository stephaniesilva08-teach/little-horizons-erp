package com.littlehorizons.erp.model;

/** Assignment of a teacher to a subject for an academic year and section. */
public class TeacherAssignment {

    private int idAssignment;
    private int idTeacher;
    private int idSubject;
    private int academicYear;
    private String section;
    private String teacherName;
    private String subjectName;
    private String creationDate;

    public TeacherAssignment() {
    }

    public TeacherAssignment(int idAssignment) {
        this.idAssignment = idAssignment;
    }

    public TeacherAssignment(int idAssignment, int idTeacher, int idSubject,
                             int academicYear, String section) {
        this.idAssignment = idAssignment;
        this.idTeacher = idTeacher;
        this.idSubject = idSubject;
        this.academicYear = academicYear;
        this.section = section;
    }

    public int getIdAssignment() { return idAssignment; }
    public void setIdAssignment(int idAssignment) { this.idAssignment = idAssignment; }
    public int getIdTeacher() { return idTeacher; }
    public void setIdTeacher(int idTeacher) { this.idTeacher = idTeacher; }
    public int getIdSubject() { return idSubject; }
    public void setIdSubject(int idSubject) { this.idSubject = idSubject; }
    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    @Override
    public String toString() {
        return "TeacherAssignment{" +
                "idAssignment=" + idAssignment +
                ", teacher='" + teacherName + '\'' +
                ", subject='" + subjectName + '\'' +
                ", year=" + academicYear +
                ", section='" + section + '\'' +
                '}';
    }
}
