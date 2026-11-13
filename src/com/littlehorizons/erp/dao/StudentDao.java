package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Student;
import java.util.List;

/**
 * Persistence contract for students.
 */
public interface StudentDao {

    int insert(Student student);

    int insertWithGuardian(Student student, int idParent);

    void update(Student student);

    boolean delete(int idStudent);

    Student findById(int idStudent);

    Student findByDocument(String documentNumber);

    List<Student> findAll();

    List<Student> findActive();

    List<Student> search(String text);

    void assignGuardian(int idStudent, int idParent, boolean main, boolean emergency);

    boolean exists(int idStudent);
}
