package com.english.learning.controller.api.mobile;

import com.english.learning.dto.CheckDictationRequest;
import com.english.learning.dto.DictationResultDTO;
import com.english.learning.dto.SkipSentenceRequest;
import com.english.learning.service.learning.dictation.DictationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller: Mobile Dictation API.
 * Provides JSON endpoints for Android dictation check/skip.
 */
@RestController
@RequestMapping("/api/mobile/dictation")
@RequiredArgsConstructor
public class MobileDictationController {

    private final DictationService dictationService;

    /**
     * POST /api/mobile/dictation/check
     * Body: { "sentenceId": 123, "userInput": "it snowed all" }
     */
    @PostMapping("/check")
    public ResponseEntity<DictationResultDTO> checkAnswer(@RequestBody CheckDictationRequest request) {
        DictationResultDTO result = dictationService.checkAnswer(request.getSentenceId(), request.getUserInput());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mobile/dictation/skip
     * Body: { "sentenceId": 123 }
     */
    @PostMapping("/skip")
    public ResponseEntity<DictationResultDTO> skipSentence(@RequestBody SkipSentenceRequest request) {
        DictationResultDTO result = dictationService.skipSentence(request.getSentenceId());
        return ResponseEntity.ok(result);
    }
}
