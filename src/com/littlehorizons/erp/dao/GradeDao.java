package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Grade;
import java.util.List;

/**
 * Persistence contract for grades.
 */
public interface GradeDao {

    List<Grade> findAll();

    Grade findById(int idGrade);
}
