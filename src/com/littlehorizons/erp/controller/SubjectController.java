package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.JdbcSubjectDao;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.Validator;

import java.util.List;

/**
 * Business rules for subjects.
 */
public class SubjectController {

    private final SubjectDao subjectDao;

    public SubjectController() {
        this(new JdbcSubjectDao());
    }

    public SubjectController(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    public int register(Subject subject) {
        validate(subject);
        return subjectDao.insert(subject);
    }

    public void update(Subject subject) {
        validate(subject);
        if (subject.getIdSubject() <= 0 || subjectDao.findById(subject.getIdSubject()) == null) {
            throw new IllegalArgumentException("La materia no existe.");
        }
        subjectDao.update(subject);
    }

    public void remove(int idSubject) {
        if (!subjectDao.delete(idSubject)) {
            throw new IllegalArgumentException("No se encontró la materia a eliminar.");
        }
    }

    public Subject findById(int idSubject) {
        return subjectDao.findById(idSubject);
    }

    public List<Subject> listAll() {
        return subjectDao.findAll();
    }

    public List<Subject> listActive() {
        return subjectDao.findActive();
    }

    public List<Subject> listByGrade(int idGrade) {
        return subjectDao.findByGrade(idGrade);
    }

    private void validate(Subject s) {
        if (s == null) {
            throw new IllegalArgumentException("Los datos de la materia son obligatorios.");
        }
        if (!Validator.isNotEmpty(s.getSubjectCode())) {
            throw new IllegalArgumentException("El código de la materia es obligatorio.");
        }
        if (!Validator.isValidName(s.getName())) {
            throw new IllegalArgumentException("El nombre de la materia es inválido.");
        }
        if (s.getIdGrade() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un grado válido.");
        }
        if (!Validator.isInRange(s.getWeeklyHours(), 1, 40)) {
            throw new IllegalArgumentException("Las horas semanales deben estar entre 1 y 40.");
        }
    }
}
