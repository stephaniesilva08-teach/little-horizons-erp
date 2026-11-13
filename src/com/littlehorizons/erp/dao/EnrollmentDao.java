package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.SubjectEnrollment;
import java.util.List;

/**
 * Persistence contract for enrollments.
 */
public interface EnrollmentDao {

    int insert(SubjectEnrollment enrollment);

    void update(SubjectEnrollment enrollment);

    boolean delete(int idEnrollment);

    SubjectEnrollment findById(int idEnrollment);

    List<SubjectEnrollment> findAll();

    List<SubjectEnrollment> findByStudent(int idStudent);

    /**
     * Finds an active enrollment for a student, subject and academic year.
     * When {@code academicPeriod} is {@code null}, any period of the year is
     * accepted (used for final grades and attendance).
     */
    SubjectEnrollment findActive(int idStudent, int idSubject, int academicYear,
                                 String academicPeriod);
}
