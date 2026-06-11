package com.english.learning.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JwtTokenProvider}.
 * Tests token generation, validation, extraction, and error handling.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    // Must be at least 64 bytes for HS512
    private static final String TEST_SECRET =
            "TestSecretKeyForJWTTokenProviderTestingMustBeAtLeast512BitsLongForHS512AlgorithmUsage!!";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationHours", 24L);
    }

    // ────────────── Token Generation ──────────────

    @Test
    void generateToken_returnsNonEmptyString() {
        String token = tokenProvider.generateToken(1L, "user@test.com", "USER");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateToken_differentUsersGetDifferentTokens() {
        String token1 = tokenProvider.generateToken(1L, "a@test.com", "USER");
        String token2 = tokenProvider.generateToken(2L, "b@test.com", "USER");
        assertNotEquals(token1, token2);
    }

    // ────────────── Token Validation ──────────────

    @Test
    void validateToken_returnsCorrectClaims() {
        String token = tokenProvider.generateToken(42L, "user@test.com", "ADMIN");

        Map<String, Object> claims = tokenProvider.validateToken(token);

        assertEquals(42L, claims.get("userId"));
        assertEquals("user@test.com", claims.get("email"));
        assertEquals("ADMIN", claims.get("role"));
        assertNotNull(claims.get("tokenId"));
        assertNotNull(claims.get("issuedAt"));
    }

    @Test
    void validateToken_invalidToken_throwsException() {
        assertThrows(InvalidJwtException.class, () ->
                tokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_tamperedToken_throwsException() {
        String token = tokenProvider.generateToken(1L, "user@test.com", "USER");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThrows(InvalidJwtException.class, () ->
                tokenProvider.validateToken(tampered));
    }

    @Test
    void validateToken_expired_throwsTokenExpired() {
        // Create a provider with 0-hour expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedProvider, "jwtExpirationHours", 0L);

        String token = shortLivedProvider.generateToken(1L, "user@test.com", "USER");

        InvalidJwtException ex = assertThrows(InvalidJwtException.class, () ->
                shortLivedProvider.validateToken(token));
        assertEquals(InvalidJwtException.Code.TOKEN_EXPIRED, ex.getCode());
    }

    // ────────────── Token Extraction ──────────────

    @Test
    void getUserIdFromToken_returnsCorrectId() {
        String token = tokenProvider.generateToken(99L, "user@test.com", "USER");
        Long userId = tokenProvider.getUserIdFromToken(token);
        assertEquals(99L, userId);
    }

    @Test
    void getTokenId_returnsNonNull() {
        String token = tokenProvider.generateToken(1L, "user@test.com", "USER");
        String tokenId = tokenProvider.getTokenId(token);
        assertNotNull(tokenId);
        assertFalse(tokenId.isBlank());
    }

    @Test
    void getTokenId_uniquePerGeneration() {
        String token1 = tokenProvider.generateToken(1L, "user@test.com", "USER");
        String token2 = tokenProvider.generateToken(1L, "user@test.com", "USER");

        assertNotEquals(tokenProvider.getTokenId(token1), tokenProvider.getTokenId(token2));
    }

    // ────────────── resolveToken ──────────────

    @Test
    void resolveToken_validBearerHeader_returnsToken() {
        String result = tokenProvider.resolveToken("Bearer my.jwt.token");
        assertEquals("my.jwt.token", result);
    }

    @Test
    void resolveToken_missingBearerPrefix_returnsNull() {
        assertNull(tokenProvider.resolveToken("my.jwt.token"));
    }

    @Test
    void resolveToken_nullHeader_returnsNull() {
        assertNull(tokenProvider.resolveToken(null));
    }

    @Test
    void resolveToken_emptyHeader_returnsNull() {
        assertNull(tokenProvider.resolveToken(""));
    }

    // ────────────── Different Secret Validation ──────────────

    @Test
    void validateToken_differentSecret_throwsException() {
        String token = tokenProvider.generateToken(1L, "user@test.com", "USER");

        JwtTokenProvider otherProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otherProvider, "jwtSecret",
                "AnotherSecretKeyForTestingPurposesThatMustAlsoBeAtLeast512BitsLongForHS512AlgorithmUsage!!");
        ReflectionTestUtils.setField(otherProvider, "jwtExpirationHours", 24L);

        assertThrows(InvalidJwtException.class, () ->
                otherProvider.validateToken(token));
    }
}
