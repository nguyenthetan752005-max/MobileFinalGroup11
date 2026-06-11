package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileAuthResponse;
import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.repository.UserRepository;
import com.english.learning.security.JwtTokenProvider;
import com.english.learning.service.auth.AuthService;
import com.english.learning.service.auth.GoogleAuthService;
import com.english.learning.service.auth.PasswordResetService;
import com.english.learning.service.auth.TokenBlacklistService;
import com.english.learning.service.settings.AppSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MobileAuthController}.
 * Uses direct method invocation with mocked dependencies — no Spring context.
 */
class MobileAuthControllerTest {

    private AuthService authService;
    private GoogleAuthService googleAuthService;
    private AppSettingService appSettingService;
    private JwtTokenProvider jwtTokenProvider;
    private UserRepository userRepository;
    private PasswordResetService passwordResetService;
    private MobileAuthController controller;

    private User activeUser;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        googleAuthService = mock(GoogleAuthService.class);
        appSettingService = mock(AppSettingService.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userRepository = mock(UserRepository.class);
        passwordResetService = mock(PasswordResetService.class);
        TokenBlacklistService blacklistService = mock(TokenBlacklistService.class);

        controller = new MobileAuthController(authService, googleAuthService,
                appSettingService, jwtTokenProvider, userRepository, passwordResetService,
                blacklistService);

        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setUsername("testuser");
        activeUser.setEmail("test@example.com");
        activeUser.setPassword("hashed");
        activeUser.setRole(Role.USER);
        activeUser.setIsActive(true);
        activeUser.setIsDeleted(false);

        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("jwt.test.token");
        when(jwtTokenProvider.resolveToken(any())).thenReturn("jwt.test.token");
    }

    // ────────────── Login ──────────────

    @Test
    void login_success_returnsTokenAndUserId() {
        when(authService.authenticateUser("testuser", "pass")).thenReturn(Optional.of(activeUser));

        var request = new com.english.learning.dto.mobile.MobileLoginRequest();
        request.setUsername("testuser");
        request.setPassword("pass");

        ResponseEntity<MobileAuthResponse> response = controller.login(request);
        MobileAuthResponse body = response.getBody();

        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(1L, body.getUserId());
        assertEquals("testuser", body.getUsername());
        assertEquals("jwt.test.token", body.getToken());
    }

    @Test
    void login_invalidCredentials_returnsFailure() {
        when(authService.authenticateUser("testuser", "wrong")).thenReturn(Optional.empty());

        var request = new com.english.learning.dto.mobile.MobileLoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong");

        ResponseEntity<MobileAuthResponse> response = controller.login(request);
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void login_bannedAccount_returnsAccountBannedCode() {
        activeUser.setIsDeleted(true);
        when(authService.authenticateUser("testuser", "pass")).thenReturn(Optional.of(activeUser));

        var request = new com.english.learning.dto.mobile.MobileLoginRequest();
        request.setUsername("testuser");
        request.setPassword("pass");

        MobileAuthResponse body = controller.login(request).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("ACCOUNT_BANNED", body.getCode());
    }

    @Test
    void login_inactiveAccount_returnsAccountInactiveCode() {
        activeUser.setIsActive(false);
        when(authService.authenticateUser("testuser", "pass")).thenReturn(Optional.of(activeUser));

        var request = new com.english.learning.dto.mobile.MobileLoginRequest();
        request.setUsername("testuser");
        request.setPassword("pass");

        MobileAuthResponse body = controller.login(request).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("ACCOUNT_INACTIVE", body.getCode());
    }

    // ────────────── Register ──────────────

    @Test
    void register_success_returnsToken() {
        when(appSettingService.isUserRegistrationAllowed()).thenReturn(true);
        when(authService.register(any())).thenReturn(activeUser);

        var request = new com.english.learning.dto.mobile.MobileRegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password");

        MobileAuthResponse body = controller.register(request).getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("jwt.test.token", body.getToken());
    }

    @Test
    void register_disabled_returnsFailure() {
        when(appSettingService.isUserRegistrationAllowed()).thenReturn(false);

        var request = new com.english.learning.dto.mobile.MobileRegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password");

        MobileAuthResponse body = controller.register(request).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
    }

    @Test
    void register_duplicateEmail_sanitizesError() {
        when(appSettingService.isUserRegistrationAllowed()).thenReturn(true);
        when(authService.register(any())).thenThrow(new RuntimeException("Email already exists"));

        var request = new com.english.learning.dto.mobile.MobileRegisterRequest();
        request.setUsername("newuser");
        request.setEmail("dup@example.com");
        request.setPassword("password");

        MobileAuthResponse body = controller.register(request).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Email"));
    }

    @Test
    void register_duplicateUsername_sanitizesError() {
        when(appSettingService.isUserRegistrationAllowed()).thenReturn(true);
        when(authService.register(any())).thenThrow(new RuntimeException("Username already taken"));

        var request = new com.english.learning.dto.mobile.MobileRegisterRequest();
        request.setUsername("dupuser");
        request.setEmail("new@example.com");
        request.setPassword("password");

        MobileAuthResponse body = controller.register(request).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
    }

    // ────────────── Forgot Password ──────────────

    @Test
    void forgotPassword_success_sendsEmail() {
        when(authService.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordResetService.createTokenForUser(any())).thenReturn("reset-token");

        MobileAuthResponse body = controller.forgotPassword(Map.of("email", "test@example.com")).getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        verify(passwordResetService).sendResetEmail(eq("test@example.com"), eq("reset-token"));
    }

    @Test
    void forgotPassword_unknownEmail_returnsFailure() {
        when(authService.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        MobileAuthResponse body = controller.forgotPassword(Map.of("email", "unknown@example.com")).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
    }

    @Test
    void forgotPassword_emptyEmail_returnsFailure() {
        MobileAuthResponse body = controller.forgotPassword(Map.of("email", "")).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
    }

    @Test
    void forgotPassword_emailSendingFails_returnsFailure() {
        when(authService.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordResetService.createTokenForUser(any())).thenReturn("token");
        doThrow(new RuntimeException("SMTP error")).when(passwordResetService).sendResetEmail(any(), any());

        MobileAuthResponse body = controller.forgotPassword(Map.of("email", "test@example.com")).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
    }

    // ────────────── Logout ──────────────

    @Test
    void logout_success() {
        when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn(1L);

        MobileAuthResponse body = controller.logout("Bearer jwt.test.token").getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
    }

    // ────────────── Google Auth ──────────────

    @Test
    void googleAuth_success() {
        when(googleAuthService.authenticateWithGoogle("google-id-token")).thenReturn(Optional.of(activeUser));

        var request = new com.english.learning.dto.mobile.MobileGoogleAuthRequest();
        request.setIdToken("google-id-token");

        MobileAuthResponse body = controller.googleAuth(request).getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("jwt.test.token", body.getToken());
    }

    @Test
    void googleAuth_failure_returnsFailure() {
        when(googleAuthService.authenticateWithGoogle("bad-token")).thenReturn(Optional.empty());

        var request = new com.english.learning.dto.mobile.MobileGoogleAuthRequest();
        request.setIdToken("bad-token");

        MobileAuthResponse body = controller.googleAuth(request).getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
    }
}
