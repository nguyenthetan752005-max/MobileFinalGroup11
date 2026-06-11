package com.english.learning.security;

import com.english.learning.entity.User;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.auth.TokenBlacklistService;
import com.english.learning.service.settings.AppSettingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * JWT Authentication Filter cho Mobile API.
 * Kiểm tra token và account status cho mỗi request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService blacklistService;
    private final UserRepository userRepository;
    private final AppSettingService appSettingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String token = tokenProvider.resolveToken(request.getHeader("Authorization"));
        
        if (token != null) {
            try {
                // Validate token
                Map<String, Object> tokenData = tokenProvider.validateToken(token);
                Long userId = (Long) tokenData.get("userId");
                long issuedAt = (Long) tokenData.get("issuedAt");
                String tokenId = (String) tokenData.get("tokenId");

                // Check blacklist
                if (blacklistService.isBlacklisted(tokenId) || 
                    blacklistService.isUserTokensRevoked(userId, issuedAt)) {
                    sendError(response, 401, "TOKEN_REVOKED", "Token đã bị thu hồi");
                    return;
                }

                // Check account status
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    sendError(response, 401, "USER_NOT_FOUND", "Người dùng không tồn tại");
                    return;
                }

                User user = userOpt.get();
                // Kiểm tra trạng thái tài khoản
                // 3 trạng thái: Hoạt động (active=true, deleted=false), Không hoạt động (active=false), Bị cấm (deleted=true)
                if (Boolean.TRUE.equals(user.getIsDeleted())) {
                    sendError(response, 401, "ACCOUNT_BANNED", 
                        "Tài khoản bị cấm" + (user.getSuspensionReason() != null ? ": " + user.getSuspensionReason() : ""));
                    return;
                }
                if (Boolean.FALSE.equals(user.getIsActive())) {
                    sendError(response, 401, "ACCOUNT_INACTIVE", 
                        "Tài khoản không hoạt động" + (user.getSuspensionReason() != null ? ": " + user.getSuspensionReason() : ""));
                    return;
                }

                // Cập nhật nhịp đập hoạt động cuối (Online Status Heartbeat)
                int timeoutMins = appSettingService.getOnlineTimeoutMinutes();
                LocalDateTime now = LocalDateTime.now();
                if (user.getLastActiveAt() == null || user.getLastActiveAt().isBefore(now.minusMinutes(timeoutMins))) {
                    user.setLastActiveAt(now);
                    userRepository.save(user);
                }

                // Set authentication
                String role = (String) tokenData.get("role");
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (InvalidJwtException e) {
                if (e.getCode() == InvalidJwtException.Code.TOKEN_EXPIRED) {
                    sendError(response, 401, "TOKEN_EXPIRED", "Token đã hết hạn");
                } else {
                    sendError(response, 401, "INVALID_TOKEN", "Token không hợp lệ");
                }
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, 
                          String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        String json = String.format(
            "{\"success\":false,\"code\":\"%s\",\"message\":\"%s\"}",
            code, message
        );
        response.getWriter().write(json);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Public endpoints không cần JWT
        return path.startsWith("/api/health") ||
               path.startsWith("/api/mobile/auth") ||
               path.startsWith("/api/mobile/catalog") ||
               path.startsWith("/api/mobile/app-settings/reminder") ||
               path.startsWith("/api/mobile/media") ||
               path.startsWith("/login") ||
               path.startsWith("/oauth2") ||
               path.startsWith("/web") ||
               path.equals("/") ||
               path.contains("."); // Static resources
    }
}
