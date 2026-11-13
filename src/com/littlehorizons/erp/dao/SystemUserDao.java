package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AccessRole;
import com.littlehorizons.erp.model.SessionUser;

import java.util.List;

/** Persistence operations for console accounts and password lockout state. */
public interface SystemUserDao {
    long countUsers();
    UserCredential findCredential(String username);
    int createFirstAdministrator(String username, String displayName,
                                 byte[] salt, byte[] passwordHash);
    int create(String username, String displayName, AccessRole role,
               byte[] salt, byte[] passwordHash);
    List<SessionUser> findAll();
    void resetPassword(int idUser, byte[] salt, byte[] passwordHash);
    void activate(int idUser);
    boolean registerFailedAttempt(int idUser);
    void resetFailedAttempts(int idUser);
    void deactivate(int idUser, int currentUserId);
}
