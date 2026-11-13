package com.littlehorizons.erp.util;

/** Generic authentication failure that does not reveal account state. */
public final class AuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException() {
        super("Usuario o contraseña incorrectos, o cuenta temporalmente bloqueada.");
    }
}
