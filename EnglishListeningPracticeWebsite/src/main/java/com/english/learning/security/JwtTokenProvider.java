package com.english.learning.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token Provider cho Mobile Authentication.
 * Tạo và validate JWT tokens cho Android app.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:defaultSecretKeyForDevelopmentOnlyMustBeAtLeast512BitsLongForHS512Algorithm!!}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-hours:24}")
    private long jwtExpirationHours;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tạo JWT token cho user.
     */
    public String generateToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtExpirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .claim("role", role)
            .claim("tokenId", UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(getSigningKey(), Jwts.SIG.HS512)
            .compact();
    }

    /**
     * Validate và parse JWT token.
     */
    public Map<String, Object> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return Map.of(
                "userId", Long.parseLong(claims.getSubject()),
                "email", claims.get("email"),
                "role", claims.get("role"),
                "tokenId", claims.get("tokenId"),
                "issuedAt", claims.getIssuedAt().getTime()
            );
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired");
            throw new InvalidJwtException(InvalidJwtException.Code.TOKEN_EXPIRED, "Token đã hết hạn", e);
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new InvalidJwtException(InvalidJwtException.Code.INVALID_TOKEN, "Token không hợp lệ", e);
        }
    }

    /**
     * Extract token từ Authorization header.
     */
    public String resolveToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * Lấy userId từ token.
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Lấy tokenId (jti) để blacklist.
     */
    public String getTokenId(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return (String) claims.get("tokenId");
    }
}
