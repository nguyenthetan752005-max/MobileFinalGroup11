package com.english.learning.service.impl.auth;

import com.english.learning.service.auth.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-Memory Implementation của TokenBlacklistService.
 * Lưu ý: Dùng cho development. Production nên dùng Redis.
 */
@Slf4j
@Service
public class InMemoryTokenBlacklistServiceImpl implements TokenBlacklistService {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final Map<Long, Long> revokedUsers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public InMemoryTokenBlacklistServiceImpl() {
        // Cleanup tokens đã hết hạn mỗi giờ
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    @Override
    public void blacklistToken(String tokenId, Duration ttl) {
        long expiryTime = Instant.now().plus(ttl).toEpochMilli();
        blacklistedTokens.put(tokenId, expiryTime);
        log.info("Token blacklisted: {}", tokenId);
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        Long expiry = blacklistedTokens.get(tokenId);
        if (expiry == null) return false;
        
        if (Instant.now().toEpochMilli() > expiry) {
            blacklistedTokens.remove(tokenId);
            return false;
        }
        return true;
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        revokedUsers.put(userId, Instant.now().toEpochMilli());
        log.info("All tokens revoked for user: {}", userId);
    }

    @Override
    public boolean isUserTokensRevoked(Long userId, long tokenIssuedAt) {
        Long revokedAt = revokedUsers.get(userId);
        if (revokedAt == null) return false;
        return tokenIssuedAt < revokedAt;
    }

    @Override
    public void clearUserRevocation(Long userId) {
        revokedUsers.remove(userId);
        log.info("User revocation cleared: {}", userId);
    }

    private void cleanupExpiredTokens() {
        long now = Instant.now().toEpochMilli();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
        log.debug("Cleaned up expired blacklisted tokens");
    }
}
