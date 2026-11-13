package com.littlehorizons.erp.dao;

/** Credential material returned only to the authentication controller. */
public record UserCredential(int id, String username, String displayName, String role,
                             boolean active, byte[] salt, byte[] passwordHash,
                             boolean locked) {
    @Override
    public String toString() {
        return "UserCredential[id=" + id + ", username=" + username
                + ", role=" + role + ", credentials=redacted]";
    }
}
