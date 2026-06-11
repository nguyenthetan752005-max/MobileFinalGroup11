package com.english.learning.controller.api.mobile;

import com.english.learning.dto.SpeakingResultDTO;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.enums.SpeakingResultType;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.service.learning.speaking.SpeakingService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

/**
 * REST Controller: Mobile Speaking API.
 * Provides JSON endpoints for Android speaking evaluation.
 * userId được lấy tự động từ JWT token (SecurityContext).
 */
@RestController
@RequestMapping("/api/mobile/speaking")
@RequiredArgsConstructor
@Slf4j
public class MobileSpeakingController {

    private final SpeakingService speakingService;
    private final SpeakingResultRepository speakingResultRepository;
    private final RestTemplate speakingAudioRestTemplate = new RestTemplate();

    /**
     * POST /api/mobile/speaking/evaluate
     * Multipart form: audio file + referenceText + sentenceId
     * userId tự lấy từ JWT token.
     */
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateSpeaking(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("referenceText") String referenceText,
            @RequestParam(value = "sentenceId", required = false) Long sentenceId) {
        try {
            Long userId = getAuthenticatedUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }
            SpeakingResultDTO result = speakingService.evaluateSpeaking(audio, referenceText, userId, sentenceId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Không thể chấm speaking lúc này."));
        }
    }

    /**
     * GET /api/mobile/speaking/results?sentenceId={sentenceId}
     * Returns BEST + CURRENT speaking results cho câu hiện tại.
     * userId tự lấy từ JWT token.
     */
    @GetMapping("/results")
    public ResponseEntity<SpeakingResultDTO> getSavedResults(
            @RequestParam("sentenceId") Long sentenceId) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        SpeakingResultDTO result = speakingService.getSavedResults(userId, sentenceId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/mobile/speaking/audio/current?sentenceId={sentenceId}
     * Proxy: tải audio CURRENT từ ImageKit và stream về Android.
     */
    @GetMapping("/audio/current")
    public void streamCurrentAudio(
            @RequestParam("sentenceId") Long sentenceId,
            HttpServletResponse response) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        streamSpeakingAudio(userId, sentenceId, SpeakingResultType.CURRENT, response);
    }

    /**
     * GET /api/mobile/speaking/audio/best?sentenceId={sentenceId}
     * Proxy: tải audio BEST từ ImageKit và stream về Android.
     */
    @GetMapping("/audio/best")
    public void streamBestAudio(
            @RequestParam("sentenceId") Long sentenceId,
            HttpServletResponse response) {
        Long userId = getAuthenticatedUserId();
        if (userId == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        streamSpeakingAudio(userId, sentenceId, SpeakingResultType.BEST, response);
    }

    /**
     * Tải audio speaking từ ImageKit và stream trực tiếp về client.
     */
    private void streamSpeakingAudio(Long userId, Long sentenceId, SpeakingResultType type, HttpServletResponse response) {
        Optional<SpeakingResult> resultOpt = speakingResultRepository
                .findByUser_IdAndSentence_IdAndResultType(userId, sentenceId, type);

        if (resultOpt.isEmpty() || resultOpt.get().getUserAudioUrl() == null) {
            log.warn("Speaking audio not found: userId={}, sentenceId={}, type={}", userId, sentenceId, type);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        String audioUrl = resultOpt.get().getUserAudioUrl();
        try {
            speakingAudioRestTemplate.execute(audioUrl, HttpMethod.GET, null, clientHttpResponse -> {
                response.setStatus(clientHttpResponse.getStatusCode().value());
                response.setContentType("audio/wav");
                response.setHeader("Cache-Control", "public, max-age=86400");
                StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
                return null;
            });
        } catch (Exception e) {
            log.error("Speaking audio proxy error: userId={}, sentenceId={}, type={}: {}", userId, sentenceId, type, e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Lấy userId từ JWT SecurityContext (đã set bởi JwtAuthenticationFilter).
     */
    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }
}
