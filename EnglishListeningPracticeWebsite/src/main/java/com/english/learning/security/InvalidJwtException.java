package com.english.learning.security;

/**
 * Typed exception for JWT validation failures.
 * Replaces raw RuntimeException in JwtTokenProvider so the filter
 * can distinguish expired vs. invalid tokens without string matching.
 */
public class InvalidJwtException extends RuntimeException {

    public enum Code {
        TOKEN_EXPIRED,
        INVALID_TOKEN
    }

    private final Code code;

    public InvalidJwtException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public InvalidJwtException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
