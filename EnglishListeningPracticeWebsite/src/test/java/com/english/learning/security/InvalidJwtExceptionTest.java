package com.english.learning.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvalidJwtException.
 */
class InvalidJwtExceptionTest {

    @Test
    void tokenExpired_hasCorrectCode() {
        InvalidJwtException ex = new InvalidJwtException(
                InvalidJwtException.Code.TOKEN_EXPIRED, "expired");
        assertEquals(InvalidJwtException.Code.TOKEN_EXPIRED, ex.getCode());
        assertEquals("expired", ex.getMessage());
    }

    @Test
    void invalidToken_hasCorrectCode() {
        InvalidJwtException ex = new InvalidJwtException(
                InvalidJwtException.Code.INVALID_TOKEN, "bad token");
        assertEquals(InvalidJwtException.Code.INVALID_TOKEN, ex.getCode());
    }

    @Test
    void constructorWithCause_preservesCause() {
        RuntimeException cause = new RuntimeException("original");
        InvalidJwtException ex = new InvalidJwtException(
                InvalidJwtException.Code.TOKEN_EXPIRED, "expired", cause);
        assertSame(cause, ex.getCause());
    }
}
