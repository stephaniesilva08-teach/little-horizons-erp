package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ParentDao;
import com.littlehorizons.erp.model.Parent;
import com.littlehorizons.erp.util.Validator;

import java.util.List;

/**
 * Business rules for guardians.
 */
public class ParentController {

    private final ParentDao parentDao = new ParentDao();

    public int register(Parent parent) {
        validate(parent, true);
        if (parentDao.findByDocument(parent.getDocumentNumber()) != null) {
            throw new IllegalArgumentException("Ya existe un acudiente con ese documento.");
        }
        return parentDao.insert(parent);
    }

    public void update(Parent parent) {
        validate(parent, false);
        if (parentDao.findById(parent.getIdParent()) == null) {
            throw new IllegalArgumentException("El acudiente no existe.");
        }
        parentDao.update(parent);
    }

    public void remove(int idParent) {
        if (!parentDao.delete(idParent)) {
            throw new IllegalArgumentException("No se encontró el acudiente a eliminar.");
        }
    }

    public Parent findById(int idParent) {
        return parentDao.findById(idParent);
    }

    public Parent findByDocument(String document) {
        return parentDao.findByDocument(document);
    }

    public List<Parent> listAll() {
        return parentDao.findAll();
    }

    public List<Parent> listByStudent(int idStudent) {
        return parentDao.findByStudent(idStudent);
    }

    private void validate(Parent p, boolean creating) {
        if (p == null) {
            throw new IllegalArgumentException("Los datos del acudiente son obligatorios.");
        }
        if (!Validator.isOneOf(p.getDocumentType(), "CC", "CE", "Pasaporte")) {
            throw new IllegalArgumentException("Tipo de documento inválido.");
        }
        if (!Validator.isValidDocumentNumber(p.getDocumentNumber())) {
            throw new IllegalArgumentException("Número de documento inválido.");
        }
        if (!Validator.isValidName(p.getFirstName())
                || !Validator.isValidName(p.getFirstLastName())) {
            throw new IllegalArgumentException("Nombre o apellido inválido.");
        }
        if (!Validator.isNotEmpty(p.getRelationship())) {
            throw new IllegalArgumentException("El parentesco es obligatorio.");
        }
        if (!Validator.isValidPhone(p.getPhonePrimary())) {
            throw new IllegalArgumentException("El teléfono principal es inválido.");
        }
        if (Validator.isNotEmpty(p.getPhoneSecondary())
                && !Validator.isValidPhone(p.getPhoneSecondary())) {
            throw new IllegalArgumentException("El teléfono secundario es inválido.");
        }
        if (Validator.isNotEmpty(p.getEmail()) && !Validator.isValidEmail(p.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico es inválido.");
        }
        if (!creating && p.getIdParent() <= 0) {
            throw new IllegalArgumentException("Falta el identificador del acudiente.");
        }
    }
}
