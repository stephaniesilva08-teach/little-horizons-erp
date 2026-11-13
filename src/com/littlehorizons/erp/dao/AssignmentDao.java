package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.TeacherAssignment;
import java.util.List;

/**
 * Persistence contract for assignments.
 */
public interface AssignmentDao {

    int insert(TeacherAssignment assignment);

    void update(TeacherAssignment assignment);

    boolean delete(int idAssignment);

    TeacherAssignment findById(int idAssignment);

    List<TeacherAssignment> findAll();

    List<TeacherAssignment> findByTeacher(int idTeacher);

    List<TeacherAssignment> findByYear(int academicYear);

    /**
     * Checks whether a teacher is assigned to a subject in the given year and
     * section.
     */
    boolean hasAssignment(int idTeacher, int idSubject, int academicYear, String section);

    int insertSchedule(Schedule schedule);

    void updateSchedule(Schedule schedule);

    boolean hasScheduleConflict(Schedule schedule);

    boolean deleteSchedule(int idSchedule);

    List<Schedule> findSchedulesByAssignment(int idAssignment);
}
