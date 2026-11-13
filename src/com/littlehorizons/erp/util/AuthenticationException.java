package com.littlehorizons.erp.util;

/** Generic authentication failure that does not reveal account state. */
public final class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
        super("Usuario o contraseña incorrectos, o cuenta temporalmente bloqueada.");
    }
}
