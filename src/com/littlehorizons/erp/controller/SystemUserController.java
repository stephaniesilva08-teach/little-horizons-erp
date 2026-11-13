package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.SystemUserDao;
import com.littlehorizons.erp.model.AccessRole;
import com.littlehorizons.erp.model.SessionUser;
import com.littlehorizons.erp.util.PasswordHasher;
import com.littlehorizons.erp.util.Validator;

import java.util.List;

/** Account administration for authenticated administrators. */
public final class SystemUserController {

    private final SystemUserDao userDao;
    private final PasswordHasher passwordHasher;
    private final SessionUser currentUser;

    public SystemUserController(SystemUserDao userDao, PasswordHasher passwordHasher,
                                SessionUser currentUser) {
        this.userDao = userDao;
        this.passwordHasher = passwordHasher;
        this.currentUser = currentUser;
    }

    public List<SessionUser> listUsers() {
        requireAdministrator();
        return userDao.findAll();
    }

    public int createUser(String username, String displayName, AccessRole role,
                          char[] password) {
        try {
            requireAdministrator();
            if (username == null || !username.trim().matches("[A-Za-z0-9._-]{3,40}")) {
                throw new IllegalArgumentException("El usuario debe tener 3 a 40 caracteres "
                        + "(letras, números, punto, guion o guion bajo).");
            }
            if (!Validator.isValidName(displayName)) {
                throw new IllegalArgumentException("El nombre visible no es válido.");
            }
            if (role == null || !passwordHasher.meetsPolicy(password)) {
                throw new IllegalArgumentException("Seleccione un rol y use una contraseña de "
                        + "12 a 128 caracteres con mayúscula, minúscula, número y símbolo.");
            }
            PasswordHasher.HashedPassword credentials = passwordHasher.hash(password);
            return userDao.create(username.trim().toLowerCase(java.util.Locale.ROOT),
                    displayName.trim(), role, credentials.salt(), credentials.hash());
        } finally {
            PasswordHasher.clear(password);
        }
    }

    public void deactivate(int idUser) {
        requireAdministrator();
        userDao.deactivate(idUser, currentUser.id());
    }

    public void resetPassword(int idUser, char[] password) {
        try {
            requireAdministrator();
            if (!passwordHasher.meetsPolicy(password)) {
                throw new IllegalArgumentException("Use una contraseña de 12 a 128 caracteres "
                        + "con mayúscula, minúscula, número y símbolo.");
            }
            PasswordHasher.HashedPassword credentials = passwordHasher.hash(password);
            userDao.resetPassword(idUser, credentials.salt(), credentials.hash());
        } finally {
            PasswordHasher.clear(password);
        }
    }

    public void activate(int idUser) {
        requireAdministrator();
        userDao.activate(idUser);
    }

    private void requireAdministrator() {
        if (currentUser == null || currentUser.role() != AccessRole.ADMINISTRATOR) {
            throw new IllegalArgumentException("Solo un administrador puede gestionar cuentas.");
        }
    }
}
