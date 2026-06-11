package com.english.learning.controller.api.learning;

import com.english.learning.dto.SpeakingResultDTO;
import com.english.learning.entity.User;
import com.english.learning.service.learning.speaking.SpeakingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/speaking")
public class SpeakingApiController {

    private final SpeakingService speakingService;

    public SpeakingApiController(SpeakingService speakingService) {
        this.speakingService = speakingService;
    }

    /**
     * Chấm điểm speaking: nhận audio + referenceText + sentenceId.
     * Tự động lấy userId từ session.
     */
    @PostMapping("/evaluate")
    public ResponseEntity<SpeakingResultDTO> evaluateSpeaking(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("referenceText") String referenceText,
            @RequestParam(value = "sentenceId", required = false) Long sentenceId,
            HttpSession session) {
        try {
            // Lấy userId từ session (có thể null nếu chưa đăng nhập)
            Long userId = null;
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser != null) {
                userId = loggedInUser.getId();
            }

            SpeakingResultDTO result = speakingService.evaluateSpeaking(audio, referenceText, userId, sentenceId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Lấy kết quả BEST + CURRENT đã lưu cho 1 câu (khi chuyển câu).
     */
    @GetMapping("/results")
    public ResponseEntity<SpeakingResultDTO> getSavedResults(
            @RequestParam("sentenceId") Long sentenceId,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.ok(null);
        }

        SpeakingResultDTO result = speakingService.getSavedResults(loggedInUser.getId(), sentenceId);
        return ResponseEntity.ok(result);
    }
}
