package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ParentDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.Validator;

import java.time.LocalDate;
import java.util.List;

/**
 * Business rules for student management.
 */
public class StudentController {

    private final StudentDao studentDao = new StudentDao();
    private final ParentDao parentDao = new ParentDao();

    public int register(Student student, int idParent) {
        validate(student, true);
        if (idParent <= 0) {
            throw new IllegalArgumentException(
                    "Debe asociar un acudiente al registrar el estudiante.");
        }
        if (parentDao.findById(idParent) == null) {
            throw new IllegalArgumentException("El acudiente no existe.");
        }
        if (studentDao.findByDocument(student.getDocumentNumber()) != null) {
            throw new IllegalArgumentException("Ya existe un estudiante con ese documento.");
        }
        int idStudent = studentDao.insert(student);
        try {
            studentDao.assignGuardian(idStudent, idParent, true, true);
        } catch (RuntimeException e) {
            studentDao.delete(idStudent);
            throw e;
        }
        return idStudent;
    }

    public void update(Student student) {
        validate(student, false);
        Student existing = studentDao.findById(student.getIdStudent());
        if (existing == null) {
            throw new IllegalArgumentException("El estudiante no existe.");
        }
        Student byDocument = studentDao.findByDocument(student.getDocumentNumber());
        if (byDocument != null && byDocument.getIdStudent() != student.getIdStudent()) {
            throw new IllegalArgumentException("El número de documento ya está en uso.");
        }
        studentDao.update(student);
    }

    public void remove(int idStudent) {
        if (!studentDao.delete(idStudent)) {
            throw new IllegalArgumentException("No se encontró el estudiante a eliminar.");
        }
    }

    public Student findById(int idStudent) {
        return studentDao.findById(idStudent);
    }

    public Student findByDocument(String document) {
        return studentDao.findByDocument(document);
    }

    public List<Student> listAll() {
        return studentDao.findAll();
    }

    public List<Student> listActive() {
        return studentDao.findActive();
    }

    public List<Student> search(String text) {
        if (!Validator.isNotEmpty(text)) {
            throw new IllegalArgumentException("Ingrese un texto de búsqueda.");
        }
        return studentDao.search(text);
    }

    public void assignGuardian(int idStudent, int idParent, boolean main,
                               boolean emergency) {
        if (studentDao.findById(idStudent) == null) {
            throw new IllegalArgumentException("El estudiante no existe.");
        }
        if (parentDao.findById(idParent) == null) {
            throw new IllegalArgumentException("El acudiente no existe.");
        }
        studentDao.assignGuardian(idStudent, idParent, main, emergency);
    }

    private void validate(Student s, boolean creating) {
        if (s == null) {
            throw new IllegalArgumentException("Los datos del estudiante son obligatorios.");
        }
        if (!Validator.isOneOf(s.getDocumentType(), "TI", "CC", "CE", "Pasaporte")) {
            throw new IllegalArgumentException("Tipo de documento inválido.");
        }
        if (!Validator.isValidDocumentNumber(s.getDocumentNumber())) {
            throw new IllegalArgumentException("Número de documento inválido.");
        }
        if (!Validator.isValidName(s.getFirstName())) {
            throw new IllegalArgumentException("El primer nombre es inválido.");
        }
        if (!Validator.isValidName(s.getFirstLastName())) {
            throw new IllegalArgumentException("El primer apellido es inválido.");
        }
        if (!Validator.isValidDate(s.getBirthDate())) {
            throw new IllegalArgumentException("La fecha de nacimiento es inválida.");
        }
        if (LocalDate.parse(s.getBirthDate()).isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        if (!Validator.isOneOf(s.getGender(), "M", "F", "Otro")) {
            throw new IllegalArgumentException("El género es inválido.");
        }
        if (Validator.isNotEmpty(s.getEmail()) && !Validator.isValidEmail(s.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico es inválido.");
        }
        if (Validator.isNotEmpty(s.getPhone()) && !Validator.isValidPhone(s.getPhone())) {
            throw new IllegalArgumentException("El teléfono es inválido.");
        }
        if (!Validator.isValidDate(s.getEntryDate())) {
            throw new IllegalArgumentException("La fecha de ingreso es inválida.");
        }
        if (!Validator.isOneOf(s.getEnrollmentState(), "Activo", "Graduado",
                "Retirado", "Suspendido")) {
            throw new IllegalArgumentException("El estado del estudiante es inválido.");
        }
        if (!creating && s.getIdStudent() <= 0) {
            throw new IllegalArgumentException("Falta el identificador del estudiante.");
        }
    }
}
