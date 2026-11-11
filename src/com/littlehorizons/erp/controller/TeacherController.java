package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.Validator;

import java.util.List;

/**
 * Business rules for teachers.
 */
public class TeacherController {

    private final TeacherDao teacherDao = new TeacherDao();

    public int register(Teacher teacher) {
        validate(teacher);
        return teacherDao.insert(teacher);
    }

    public void update(Teacher teacher) {
        validate(teacher);
        if (teacher.getIdTeacher() <= 0 || teacherDao.findById(teacher.getIdTeacher()) == null) {
            throw new IllegalArgumentException("El profesor no existe.");
        }
        teacherDao.update(teacher);
    }

    public void remove(int idTeacher) {
        if (!teacherDao.delete(idTeacher)) {
            throw new IllegalArgumentException("No se encontró el profesor a eliminar.");
        }
    }

    public Teacher findById(int idTeacher) {
        return teacherDao.findById(idTeacher);
    }

    public List<Teacher> listAll() {
        return teacherDao.findAll();
    }

    public List<Teacher> listActive() {
        return teacherDao.findActive();
    }

    private void validate(Teacher t) {
        if (t == null) {
            throw new IllegalArgumentException("Los datos del profesor son obligatorios.");
        }
        if (!Validator.isOneOf(t.getDocumentType(), "CC", "CE", "Pasaporte")) {
            throw new IllegalArgumentException("Tipo de documento inválido.");
        }
        if (!Validator.isValidDocumentNumber(t.getDocumentNumber())) {
            throw new IllegalArgumentException("Número de documento inválido.");
        }
        if (!Validator.isValidName(t.getFirstName())
                || !Validator.isValidName(t.getFirstLastName())) {
            throw new IllegalArgumentException("Nombre o apellido inválido.");
        }
        if (!Validator.isValidEmail(t.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico es inválido.");
        }
        if (Validator.isNotEmpty(t.getPhone()) && !Validator.isValidPhone(t.getPhone())) {
            throw new IllegalArgumentException("El teléfono es inválido.");
        }
        if (!Validator.isValidDate(t.getHireDate())) {
            throw new IllegalArgumentException("La fecha de contratación es inválida.");
        }
        if (!Validator.isOneOf(t.getState(), "Activo", "Inactivo", "Licencia")) {
            throw new IllegalArgumentException("El estado del profesor es inválido.");
        }
    }
}
