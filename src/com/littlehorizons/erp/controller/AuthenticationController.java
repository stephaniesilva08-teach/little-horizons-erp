package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.SystemUserDao;
import com.littlehorizons.erp.dao.UserCredential;
import com.littlehorizons.erp.model.AccessRole;
import com.littlehorizons.erp.model.SessionUser;
import com.littlehorizons.erp.util.AuthenticationException;
import com.littlehorizons.erp.util.PasswordHasher;

import java.util.Locale;

/** Login and one-time first administrator setup. */
public final class AuthenticationController {

    private static final byte[] DUMMY_SALT = "fixed-dummy-salt".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static final byte[] DUMMY_HASH = new byte[32];

    private final SystemUserDao userDao;
    private final PasswordHasher passwordHasher;

    public AuthenticationController(SystemUserDao userDao, PasswordHasher passwordHasher) {
        this.userDao = userDao;
        this.passwordHasher = passwordHasher;
    }

    public boolean hasAccounts() {
        return userDao.countUsers() > 0;
    }

    public SessionUser createFirstAdministrator(String username, String displayName,
                                                char[] password) {
        try {
            validateIdentity(username, displayName);
            validatePassword(password);
            PasswordHasher.HashedPassword credentials = passwordHasher.hash(password);
            int id = userDao.createFirstAdministrator(normalize(username), displayName.trim(),
                    credentials.salt(), credentials.hash());
            return new SessionUser(id, normalize(username), displayName.trim(),
                    AccessRole.ADMINISTRADOR, true);
        } finally {
            PasswordHasher.clear(password);
        }
    }

    public SessionUser authenticate(String username, char[] password) {
        String normalized = normalize(username);
        try {
            UserCredential credential = userDao.findCredential(normalized);
            if (credential == null) {
                passwordHasher.verify(password, DUMMY_SALT, DUMMY_HASH);
                throw new AuthenticationException();
            }
            boolean passwordMatches = passwordHasher.verify(password,
                    credential.salt(), credential.passwordHash());
            if (!credential.active() || credential.locked() || !passwordMatches) {
                if (credential.active() && !credential.locked() && !passwordMatches) {
                    userDao.registerFailedAttempt(credential.id());
                }
                throw new AuthenticationException();
            }
            userDao.resetFailedAttempts(credential.id());
            return new SessionUser(credential.id(), credential.username(),
                    credential.displayName(), AccessRole.valueOf(credential.role()), true);
        } finally {
            PasswordHasher.clear(password);
        }
    }

    private void validateIdentity(String username, String displayName) {
        if (username == null || !username.trim().matches("[A-Za-z0-9._-]{3,40}")) {
            throw new IllegalArgumentException("El usuario debe tener 3 a 40 caracteres "
                    + "(letras, números, punto, guion o guion bajo).");
        }
        if (!com.littlehorizons.erp.util.Validator.isValidName(displayName)) {
            throw new IllegalArgumentException("El nombre visible no es válido.");
        }
    }

    private void validatePassword(char[] password) {
        if (!passwordHasher.meetsPolicy(password)) {
            throw new IllegalArgumentException("La contraseña debe tener entre 12 y 128 "
                    + "caracteres e incluir mayúscula, minúscula, número y símbolo.");
        }
    }

    static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
