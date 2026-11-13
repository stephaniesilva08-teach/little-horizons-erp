package com.littlehorizons.erp.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/** PBKDF2 password hashing with a random per-account salt. */
public final class PasswordHasher {

    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public record HashedPassword(byte[] salt, byte[] hash) {
    }

    public HashedPassword hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return new HashedPassword(salt, derive(password, salt));
    }

    public boolean verify(char[] password, byte[] salt, byte[] expectedHash) {
        return MessageDigest.isEqual(derive(password, salt), expectedHash);
    }

    private byte[] derive(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo proteger la contraseña.", e);
        } finally {
            spec.clearPassword();
        }
    }

    public boolean meetsPolicy(char[] password) {
        if (password == null || password.length < 12 || password.length > 128) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
        for (char character : password) {
            upper |= Character.isUpperCase(character);
            lower |= Character.isLowerCase(character);
            digit |= Character.isDigit(character);
            symbol |= !Character.isLetterOrDigit(character) && !Character.isWhitespace(character);
        }
        return upper && lower && digit && symbol;
    }

    public static void clear(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
