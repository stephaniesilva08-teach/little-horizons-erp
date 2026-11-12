package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Matriculation;
import java.util.List;

/**
 * Persistence contract for matriculations.
 */
public interface MatriculationDao {

    int insert(Matriculation matriculation);

    void update(Matriculation matriculation);

    boolean delete(int idMatriculation);

    Matriculation findById(int idMatriculation);

    List<Matriculation> findAll();

    List<Matriculation> findByStudent(int idStudent);

    List<Matriculation> findByYear(int academicYear);
}
