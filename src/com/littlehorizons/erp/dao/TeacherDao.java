package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import java.util.List;

/**
 * Persistence contract for teachers.
 */
public interface TeacherDao {

    int insert(Teacher teacher);

    void update(Teacher teacher);

    boolean delete(int idTeacher);

    Teacher findById(int idTeacher);

    List<Teacher> findAll();

    List<Teacher> findActive();
}
