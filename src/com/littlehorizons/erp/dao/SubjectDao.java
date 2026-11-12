package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Subject;
import java.util.List;

/**
 * Persistence contract for subjects.
 */
public interface SubjectDao {

    int insert(Subject subject);

    void update(Subject subject);

    boolean delete(int idSubject);

    Subject findById(int idSubject);

    List<Subject> findAll();

    List<Subject> findActive();

    List<Subject> findByGrade(int idGrade);
}
