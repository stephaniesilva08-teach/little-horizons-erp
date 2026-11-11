package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.MatriculationDao;
import com.littlehorizons.erp.model.Matriculation;
import com.littlehorizons.erp.util.Validator;

import java.util.List;

/**
 * Business rules for annual matriculations.
 */
public class MatriculationController {

    private final MatriculationDao matriculationDao = new MatriculationDao();

    public int register(Matriculation matriculation) {
        validate(matriculation, true);
        return matriculationDao.insert(matriculation);
    }

    public void update(Matriculation matriculation) {
        validate(matriculation, false);
        if (matriculationDao.findById(matriculation.getIdMatriculation()) == null) {
            throw new IllegalArgumentException("La matrícula no existe.");
        }
        matriculationDao.update(matriculation);
    }

    public void remove(int idMatriculation) {
        if (!matriculationDao.delete(idMatriculation)) {
            throw new IllegalArgumentException("No se encontró la matrícula a eliminar.");
        }
    }

    public Matriculation findById(int idMatriculation) {
        return matriculationDao.findById(idMatriculation);
    }

    public List<Matriculation> listAll() {
        return matriculationDao.findAll();
    }

    public List<Matriculation> listByStudent(int idStudent) {
        return matriculationDao.findByStudent(idStudent);
    }

    public List<Matriculation> listByYear(int year) {
        return matriculationDao.findByYear(year);
    }

    private void validate(Matriculation m, boolean creating) {
        if (m == null) {
            throw new IllegalArgumentException("Los datos de la matrícula son obligatorios.");
        }
        if (m.getIdStudent() <= 0 || m.getIdGrade() <= 0) {
            throw new IllegalArgumentException("Estudiante y grado son obligatorios.");
        }
        if (!Validator.isInRange(m.getAcademicYear(), 2000, 2100)) {
            throw new IllegalArgumentException("El año académico es inválido.");
        }
        if (!Validator.isOneOf(m.getState(), "Activa", "Completada", "Retirada")) {
            throw new IllegalArgumentException("El estado de la matrícula es inválido.");
        }
        if (!Validator.isValidDate(m.getEnrollmentDate())) {
            throw new IllegalArgumentException("La fecha de matrícula es inválida.");
        }
        if (Validator.isNotEmpty(m.getWithdrawalDate())
                && !Validator.isValidDate(m.getWithdrawalDate())) {
            throw new IllegalArgumentException("La fecha de retiro es inválida.");
        }
        if (!creating && m.getIdMatriculation() <= 0) {
            throw new IllegalArgumentException("Falta el identificador de la matrícula.");
        }
    }
}
