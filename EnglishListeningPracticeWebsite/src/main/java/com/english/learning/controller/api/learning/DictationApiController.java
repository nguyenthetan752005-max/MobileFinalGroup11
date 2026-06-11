// REST API TIẾP NHẬN YÊU CẦU CHẤM CHÍNH TẢ TỪ TRÌNH DUYỆT.
// - Nhận sentenceId + userInput qua HTTP POST.
// - Gọi DictationService để xử lý logic.
// - Trả kết quả JSON (DictationResultDTO) về cho JavaScript.

package com.english.learning.controller.api.learning;

import com.english.learning.dto.CheckDictationRequest;
import com.english.learning.dto.DictationResultDTO;
import com.english.learning.dto.SkipSentenceRequest;
import com.english.learning.service.learning.dictation.DictationService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dictation")
public class DictationApiController {

    private final DictationService dictationService;

    public DictationApiController(DictationService dictationService) {
        this.dictationService = dictationService;
    }

    /**
     * API Chấm đáp án chép chính tả.
     * POST /api/dictation/check
     * Body: { "sentenceId": 123, "userInput": "it snowed all" }
     * Response: { "correct": false, "matchedCount": 3, "hint": "it snowed all *** ***", ... }
     */
    @PostMapping("/check")
    public ResponseEntity<DictationResultDTO> checkAnswer(@Valid @RequestBody CheckDictationRequest request) {
        DictationResultDTO result = dictationService.checkAnswer(request.getSentenceId(), request.getUserInput());
        return ResponseEntity.ok(result);
    }

    /**
     * API Bỏ qua câu (Skip) - trả về đáp án đầy đủ.
     * POST /api/dictation/skip
     * Body: { "sentenceId": 123 }
     * Response: { "correct": false, "hint": "It snowed all day today.", "correctSentence": "..." }
     */
    @PostMapping("/skip")
    public ResponseEntity<DictationResultDTO> skipSentence(@Valid @RequestBody SkipSentenceRequest request) {
        DictationResultDTO result = dictationService.skipSentence(request.getSentenceId());
        return ResponseEntity.ok(result);
    }
}
