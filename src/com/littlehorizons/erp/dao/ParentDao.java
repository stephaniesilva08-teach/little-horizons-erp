package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Parent;
import java.util.List;

/**
 * Persistence contract for parents.
 */
public interface ParentDao {

    int insert(Parent parent);

    void update(Parent parent);

    boolean delete(int idParent);

    Parent findById(int idParent);

    Parent findByDocument(String documentNumber);

    List<Parent> findAll();

    List<Parent> findByStudent(int idStudent);
}
