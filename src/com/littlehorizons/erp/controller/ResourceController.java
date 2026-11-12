package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.JdbcResourceDao;
import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.util.Validator;

import java.time.LocalDate;
import java.util.List;

/**
 * Business rules for the resource inventory and loans.
 */
public class ResourceController {

    private final ResourceDao resourceDao;

    public ResourceController() {
        this(new JdbcResourceDao());
    }

    public ResourceController(ResourceDao resourceDao) {
        this.resourceDao = resourceDao;
    }

    public List<ResourceCategory> listCategories() {
        return resourceDao.findAllCategories();
    }

    public int createCategory(ResourceCategory category) {
        if (!Validator.isValidName(category.getName())) {
            throw new IllegalArgumentException("El nombre de la categoría es inválido.");
        }
        return resourceDao.insertCategory(category);
    }

    public int createResource(Resource resource) {
        validateResource(resource);
        return resourceDao.insertResource(resource);
    }

    public void updateResource(Resource resource) {
        if (resource.getIdResource() <= 0) {
            throw new IllegalArgumentException("Falta el identificador del recurso.");
        }
        validateResource(resource);
        resourceDao.updateResource(resource);
    }

    public void removeResource(int idResource) {
        if (!resourceDao.deleteResource(idResource)) {
            throw new IllegalArgumentException("No se encontró el recurso a eliminar.");
        }
    }

    public Resource findResource(int idResource) {
        return resourceDao.findResourceById(idResource);
    }

    public List<Resource> listResources() {
        return resourceDao.findAllResources();
    }

    public List<Resource> listAvailableResources() {
        return resourceDao.findAvailableResources();
    }

    public int lendToStudent(Loan loan) {
        validateLoan(loan, false);
        Resource resource = resourceDao.findResourceById(loan.getIdResource());
        requireAvailable(resource);
        loan.setLoanDate(loan.getLoanDate() == null
                ? LocalDate.now().toString() : loan.getLoanDate());
        return resourceDao.insertStudentLoan(loan);
    }

    public int lendToTeacher(Loan loan) {
        validateLoan(loan, true);
        Resource resource = resourceDao.findResourceById(loan.getIdResource());
        requireAvailable(resource);
        loan.setLoanDate(loan.getLoanDate() == null
                ? LocalDate.now().toString() : loan.getLoanDate());
        return resourceDao.insertTeacherLoan(loan);
    }

    public void returnStudentLoan(int idLoan) {
        resourceDao.returnStudentLoan(idLoan);
    }

    public void returnTeacherLoan(int idLoan) {
        resourceDao.returnTeacherLoan(idLoan);
    }

    public Loan findStudentLoan(int idLoan) {
        return resourceDao.findStudentLoanById(idLoan);
    }

    public Loan findTeacherLoan(int idLoan) {
        return resourceDao.findTeacherLoanById(idLoan);
    }

    public List<Loan> listStudentLoans() {
        resourceDao.markOverdueLoans();
        return resourceDao.findStudentLoans();
    }

    public List<Loan> listTeacherLoans() {
        resourceDao.markOverdueLoans();
        return resourceDao.findTeacherLoans();
    }

    public List<Loan> listLoansByStudent(int idStudent) {
        return resourceDao.findLoansByStudent(idStudent);
    }

    public List<Loan> listLoansByTeacher(int idTeacher) {
        return resourceDao.findLoansByTeacher(idTeacher);
    }

    private void requireAvailable(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("El recurso no existe.");
        }
        if (!"Disponible".equalsIgnoreCase(resource.getState())) {
            throw new IllegalArgumentException("El recurso no está disponible ("
                    + resource.getState() + ").");
        }
    }

    private void validateResource(Resource r) {
        if (!Validator.isNotEmpty(r.getResourceCode())) {
            throw new IllegalArgumentException("El código del recurso es obligatorio.");
        }
        if (!Validator.isNotEmpty(r.getName())) {
            throw new IllegalArgumentException("El nombre del recurso es obligatorio.");
        }
        if (r.getIdCategory() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }
        if (Validator.isNotEmpty(r.getAcquisitionDate())
                && !Validator.isValidDate(r.getAcquisitionDate())) {
            throw new IllegalArgumentException("La fecha de adquisición es inválida.");
        }
        if (!Validator.isOneOf(r.getState(), "Disponible", "Prestado",
                "En Mantenimiento", "Dado de Baja")) {
            throw new IllegalArgumentException("Estado del recurso inválido.");
        }
    }

    private void validateLoan(Loan loan, boolean teacher) {
        if (loan.getIdResource() <= 0) {
            throw new IllegalArgumentException("El recurso es obligatorio.");
        }
        if (teacher && loan.getIdTeacher() <= 0) {
            throw new IllegalArgumentException("El profesor es obligatorio.");
        }
        if (!teacher && loan.getIdStudent() <= 0) {
            throw new IllegalArgumentException("El estudiante es obligatorio.");
        }
        if (!Validator.isValidDate(loan.getExpectedReturnDate())) {
            throw new IllegalArgumentException("La fecha esperada de devolución es inválida.");
        }
        if (loan.getLoanDate() != null && !Validator.isValidDate(loan.getLoanDate())) {
            throw new IllegalArgumentException("La fecha de préstamo es inválida.");
        }
    }
}
