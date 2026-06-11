package com.english.learning.service.auth;

import com.english.learning.service.impl.auth.InMemoryTokenBlacklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryTokenBlacklistServiceImpl.
 */
class InMemoryTokenBlacklistServiceTest {

    private InMemoryTokenBlacklistServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InMemoryTokenBlacklistServiceImpl();
    }

    // --- blacklistToken / isBlacklisted ---

    @Test
    void blacklistToken_makesTokenBlacklisted() {
        service.blacklistToken("token-1", Duration.ofHours(1));
        assertTrue(service.isBlacklisted("token-1"));
    }

    @Test
    void isBlacklisted_returnsFalseForUnknownToken() {
        assertFalse(service.isBlacklisted("unknown-token"));
    }

    @Test
    void blacklistToken_withZeroDuration_expiredImmediately() throws InterruptedException {
        // Blacklist with a 1ms TTL, then wait briefly
        service.blacklistToken("short-lived", Duration.ofMillis(1));
        Thread.sleep(10);
        assertFalse(service.isBlacklisted("short-lived"));
    }

    @Test
    void blacklistToken_multipleTimes_allBlacklisted() {
        service.blacklistToken("a", Duration.ofHours(1));
        service.blacklistToken("b", Duration.ofHours(1));
        service.blacklistToken("c", Duration.ofHours(1));
        assertTrue(service.isBlacklisted("a"));
        assertTrue(service.isBlacklisted("b"));
        assertTrue(service.isBlacklisted("c"));
    }

    // --- revokeAllUserTokens / isUserTokensRevoked ---

    @Test
    void revokeAllUserTokens_revokesTokensIssuedBefore() {
        long issuedBefore = System.currentTimeMillis() - 1000;
        service.revokeAllUserTokens(42L);
        assertTrue(service.isUserTokensRevoked(42L, issuedBefore));
    }

    @Test
    void revokeAllUserTokens_doesNotRevokeTokensIssuedAfter() {
        service.revokeAllUserTokens(42L);
        long issuedAfter = System.currentTimeMillis() + 5000;
        assertFalse(service.isUserTokensRevoked(42L, issuedAfter));
    }

    @Test
    void isUserTokensRevoked_returnsFalseForNonRevokedUser() {
        assertFalse(service.isUserTokensRevoked(99L, System.currentTimeMillis()));
    }

    // --- clearUserRevocation ---

    @Test
    void clearUserRevocation_removesRevocation() {
        long issuedBefore = System.currentTimeMillis() - 1000;
        service.revokeAllUserTokens(42L);
        assertTrue(service.isUserTokensRevoked(42L, issuedBefore));

        service.clearUserRevocation(42L);
        assertFalse(service.isUserTokensRevoked(42L, issuedBefore));
    }
}
