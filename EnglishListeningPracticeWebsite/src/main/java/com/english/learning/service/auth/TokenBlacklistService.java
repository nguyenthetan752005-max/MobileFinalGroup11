package com.english.learning.service.auth;

import java.time.Duration;

/**
 * Service interface cho Token Blacklist.
 * Dùng để revoke JWT tokens khi admin khóa tài khoản hoặc user đăng xuất.
 */
public interface TokenBlacklistService {

    /**
     * Thêm token vào blacklist.
     * @param token JWT token cần blacklist
     * @param ttl thời gian sống (thường bằng token expiration)
     */
    void blacklistToken(String token, Duration ttl);

    /**
     * Kiểm tra token có trong blacklist không.
     */
    boolean isBlacklisted(String token);

    /**
     * Revoke tất cả tokens của một user.
     * Dùng khi admin khóa tài khoản.
     */
    void revokeAllUserTokens(Long userId);

    /**
     * Kiểm tra tokens của user có bị revoke không.
     * @param userId ID của user
     * @param tokenIssuedAt thời điểm token được tạo (epoch millis)
     */
    boolean isUserTokensRevoked(Long userId, long tokenIssuedAt);

    /**
     * Xóa revoked status của user (khi mở khóa tài khoản).
     */
    void clearUserRevocation(Long userId);
}
