package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileAuthResponse;
import com.english.learning.dto.mobile.MobileLoginRequest;
import com.english.learning.dto.mobile.MobileRegisterRequest;
import com.english.learning.dto.mobile.MobileGoogleAuthRequest;
import com.english.learning.entity.User;
import com.english.learning.repository.UserRepository;
import com.english.learning.security.JwtTokenProvider;
import com.english.learning.service.auth.AuthService;
import com.english.learning.service.auth.GoogleAuthService;
import com.english.learning.service.settings.AppSettingService;
import com.english.learning.service.auth.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * REST Controller: Mobile Authentication API.
 * Provides JSON login/register for Android app (stateless, no session).
 */
@RestController
@RequestMapping("/api/mobile/auth")
@RequiredArgsConstructor
public class MobileAuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final AppSettingService appSettingService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final com.english.learning.service.auth.PasswordResetService passwordResetService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * POST /api/mobile/auth/login
     * Body: { "username": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<MobileAuthResponse> login(@RequestBody MobileLoginRequest request) {
        Optional<User> userOpt = authService.authenticateUser(request.getUsername(), request.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Kiểm tra trạng thái tài khoản (3 trạng thái: Hoạt động, Không hoạt động, Bị cấm)
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                return ResponseEntity.ok(MobileAuthResponse.builder()
                        .success(false)
                        .code("ACCOUNT_BANNED")
                        .message("Tài khoản đã bị khóa.")
                        .build());
            }
            if (Boolean.FALSE.equals(user.getIsActive())) {
                return ResponseEntity.ok(MobileAuthResponse.builder()
                        .success(false)
                        .code("ACCOUNT_INACTIVE")
                        .message("Tài khoản hiện không hoạt động.")
                        .build());
            }
            // Tạo JWT token
            String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
            
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .avatarUrl(user.getAvatarUrl())
                    .token(token)
                    .build());
        }

        return ResponseEntity.ok(MobileAuthResponse.builder()
                .success(false)
                .message("Tài khoản hoặc mật khẩu không chính xác!")
                .build());
    }

    /**
     * POST /api/mobile/auth/register
     * Body: { "username": "...", "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<MobileAuthResponse> register(@RequestBody MobileRegisterRequest request) {
        if (!appSettingService.isUserRegistrationAllowed()) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message("Tính năng đăng ký tài khoản hiện đang bị khóa.")
                    .build());
        }

        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            User savedUser = authService.register(user);

            // Tạo JWT token cho user mới
            String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
            
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(true)
                    .message("Registration successful")
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole().name())
                    .avatarUrl(savedUser.getAvatarUrl())
                    .token(token)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message(sanitizeRegistrationError(e))
                    .build());
        }
    }

    /**
     * POST /api/mobile/auth/google
     * Body: { "idToken": "..." }
     */
    @PostMapping("/google")
    public ResponseEntity<MobileAuthResponse> googleAuth(@RequestBody MobileGoogleAuthRequest request) {
        try {
            Optional<User> userOpt = googleAuthService.authenticateWithGoogle(request.getIdToken());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Kiểm tra trạng thái tài khoản (3 trạng thái: Hoạt động, Không hoạt động, Bị cấm)
                if (Boolean.TRUE.equals(user.getIsDeleted())) {
                    return ResponseEntity.ok(MobileAuthResponse.builder()
                            .success(false)
                            .code("ACCOUNT_BANNED")
                            .message("Tài khoản đã bị khóa.")
                            .build());
                }
                if (Boolean.FALSE.equals(user.getIsActive())) {
                    return ResponseEntity.ok(MobileAuthResponse.builder()
                            .success(false)
                            .code("ACCOUNT_INACTIVE")
                            .message("Tài khoản hiện không hoạt động.")
                            .build());
                }
                // Tạo JWT token
                String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
                
                return ResponseEntity.ok(MobileAuthResponse.builder()
                        .success(true)
                        .message("Đăng nhập Google thành công.")
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .avatarUrl(user.getAvatarUrl())
                        .token(token)
                        .build());
            } else {
                return ResponseEntity.ok(MobileAuthResponse.builder()
                        .success(false)
                        .message("Không thể xác thực với Google.")
                        .build());
            }
        } catch (RuntimeException e) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message("Không thể xác thực với Google lúc này. Vui lòng thử lại sau.")
                    .build());
        }
    }

    /**
     * POST /api/mobile/auth/logout
     * Đăng xuất - đánh dấu user không còn đăng nhập
     */
    @PostMapping("/logout")
    public ResponseEntity<MobileAuthResponse> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenProvider.resolveToken(authHeader);
            if (token != null) {
                String tokenId = jwtTokenProvider.getTokenId(token);
                tokenBlacklistService.blacklistToken(tokenId, Duration.ofHours(24));
            }
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(true)
                    .message("Đăng xuất thành công.")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message("Không thể đăng xuất lúc này. Vui lòng thử lại.")
                    .build());
        }
    }

    /**
     * POST /api/mobile/auth/forgot-password
     * Body: { "email": "..." }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MobileAuthResponse> forgotPassword(@RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message("Vui lòng nhập địa chỉ email.")
                    .build());
        }

        Optional<User> userOpt = authService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message("Không tìm thấy tài khoản với email này!")
                    .build());
        }

        try {
            User user = userOpt.get();
            String token = passwordResetService.createTokenForUser(user);
            passwordResetService.sendResetEmail(user.getEmail(), token);
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(true)
                    .message("Đã gửi link đặt lại mật khẩu tới email của bạn. Vui lòng kiểm tra hộp thư!")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message("Có lỗi xảy ra khi gửi email. Vui lòng thử lại sau!")
                    .build());
        }
    }

    private String sanitizeRegistrationError(RuntimeException exception) {
        String message = exception == null || exception.getMessage() == null
                ? ""
                : exception.getMessage().toLowerCase(Locale.US);
        if (message.contains("email")) {
            return "Email đã tồn tại.";
        }
        if (message.contains("username")) {
            return "Tên đăng nhập đã tồn tại.";
        }
        return "Không thể tạo tài khoản với thông tin đã nhập.";
    }
}
