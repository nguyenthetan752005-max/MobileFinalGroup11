package com.english.learning.controller.api.mobile;

import com.english.learning.entity.UserProgress;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.service.progress.UserProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MobileProgressController}.
 * Direct invocation with mocked SecurityContext and service layer.
 */
class MobileProgressControllerTest {

    private UserProgressService progressService;
    private MobileProgressController controller;

    @BeforeEach
    void setUp() {
        progressService = mock(UserProgressService.class);
        controller = new MobileProgressController(progressService);
    }

    private void authenticateAs(Long userId) {
        var auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ────────────── In-Progress ──────────────

    @Test
    void getInProgressLessons_withUserId_returns200() {
        when(progressService.getInProgressLessons(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = controller.getInProgressLessons(1L);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getInProgressLessons_noAuth_returns401() {
        SecurityContextHolder.clearContext();
        ResponseEntity<?> response = controller.getInProgressLessons(null);
        assertEquals(401, response.getStatusCode().value());
    }

    // ────────────── Update Progress ──────────────

    @Test
    @SuppressWarnings("unchecked")
    void updateProgress_success() {
        authenticateAs(1L);
        UserProgress mockProgress = mock(UserProgress.class);
        when(mockProgress.getStatus()).thenReturn(UserProgressStatus.IN_PROGRESS);
        when(progressService.updateProgress(eq(1L), eq(100L))).thenReturn(mockProgress);

        ResponseEntity<?> response = controller.updateProgress(Map.of("userId", 1L, "sentenceId", 100L));
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
    }

    @Test
    void updateProgress_missingSentenceId_returns400() {
        authenticateAs(1L);
        ResponseEntity<?> response = controller.updateProgress(Map.of("userId", 1L));
        assertEquals(400, response.getStatusCode().value());
    }

    // ────────────── Complete ──────────────

    @Test
    @SuppressWarnings("unchecked")
    void completeSentence_success() {
        authenticateAs(1L);
        UserProgress mockProgress = mock(UserProgress.class);
        when(mockProgress.getStatus()).thenReturn(UserProgressStatus.COMPLETED);
        when(progressService.completeSentence(eq(1L), eq(100L))).thenReturn(mockProgress);

        ResponseEntity<?> response = controller.completeSentence(Map.of("userId", 1L, "sentenceId", 100L));
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("COMPLETED", body.get("status"));
    }

    // ────────────── Skip ──────────────

    @Test
    @SuppressWarnings("unchecked")
    void skipSentence_success() {
        authenticateAs(1L);
        UserProgress mockProgress = mock(UserProgress.class);
        when(mockProgress.getStatus()).thenReturn(UserProgressStatus.SKIPPED);
        when(progressService.skipSentence(eq(1L), eq(100L))).thenReturn(mockProgress);

        ResponseEntity<?> response = controller.skipSentence(Map.of("userId", 1L, "sentenceId", 100L));
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("SKIPPED", body.get("status"));
    }
}
