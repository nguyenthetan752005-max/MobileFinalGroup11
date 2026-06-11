package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileAuthResponse;
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

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests that logout properly blacklists the JWT token.
 */
class MobileAuthControllerLogoutTest {

    private JwtTokenProvider jwtTokenProvider;
    private TokenBlacklistService blacklistService;
    private MobileAuthController controller;

    @BeforeEach
    void setUp() {
        AuthService authService = mock(AuthService.class);
        GoogleAuthService googleAuthService = mock(GoogleAuthService.class);
        AppSettingService appSettingService = mock(AppSettingService.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordResetService passwordResetService = mock(PasswordResetService.class);
        blacklistService = mock(TokenBlacklistService.class);

        controller = new MobileAuthController(
                authService, googleAuthService, appSettingService,
                jwtTokenProvider, userRepository, passwordResetService,
                blacklistService
        );
    }

    @Test
    void logout_blacklistsTokenSuccessfully() {
        when(jwtTokenProvider.resolveToken("Bearer abc.def.ghi")).thenReturn("abc.def.ghi");
        when(jwtTokenProvider.getTokenId("abc.def.ghi")).thenReturn("token-id-123");

        ResponseEntity<MobileAuthResponse> response = controller.logout("Bearer abc.def.ghi");

        assertTrue(response.getBody().isSuccess());
        verify(blacklistService).blacklistToken(eq("token-id-123"), any(Duration.class));
    }

    @Test
    void logout_withNullToken_returnsSuccessWithoutBlacklisting() {
        when(jwtTokenProvider.resolveToken("InvalidHeader")).thenReturn(null);

        ResponseEntity<MobileAuthResponse> response = controller.logout("InvalidHeader");

        assertTrue(response.getBody().isSuccess());
        verifyNoInteractions(blacklistService);
    }

    @Test
    void logout_onException_returnsFalseWithMessage() {
        when(jwtTokenProvider.resolveToken(anyString())).thenThrow(new RuntimeException("boom"));

        ResponseEntity<MobileAuthResponse> response = controller.logout("Bearer broken");

        assertFalse(response.getBody().isSuccess());
    }
}
