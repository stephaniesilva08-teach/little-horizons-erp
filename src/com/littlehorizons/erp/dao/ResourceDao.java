package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import java.util.List;

/**
 * Persistence contract for resources.
 */
public interface ResourceDao {

    List<ResourceCategory> findAllCategories();

    int insertCategory(ResourceCategory category);

    int insertResource(Resource resource);

    void updateResource(Resource resource);

    boolean deleteResource(int idResource);

    Resource findResourceById(int idResource);

    List<Resource> findAllResources();

    List<Resource> findAvailableResources();

    int insertStudentLoan(Loan loan);

    int insertTeacherLoan(Loan loan);

    Loan findStudentLoanById(int idLoan);

    Loan findTeacherLoanById(int idLoan);

    List<Loan> findStudentLoans();

    List<Loan> findTeacherLoans();

    List<Loan> findLoansByStudent(int idStudent);

    List<Loan> findLoansByTeacher(int idTeacher);

    void returnStudentLoan(int idLoan);

    void returnTeacherLoan(int idLoan);
}
