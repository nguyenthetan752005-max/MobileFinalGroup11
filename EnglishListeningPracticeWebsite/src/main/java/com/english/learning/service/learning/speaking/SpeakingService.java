package com.english.learning.service.learning.speaking;

import com.english.learning.dto.SpeakingResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface SpeakingService {

    /**
     * ÄÃ¡nh giÃ¡ speaking: transcribe â†’ AI score â†’ upload media storage â†’ lÆ°u DB.
     * Náº¿u userId == null (chÆ°a Ä‘Äƒng nháº­p) thÃ¬ váº«n cháº¥m Ä‘iá»ƒm nhÆ°ng khÃ´ng lÆ°u.
     */
    SpeakingResultDTO evaluateSpeaking(MultipartFile audio, String referenceText, Long userId, Long sentenceId);

    /**
     * Láº¥y káº¿t quáº£ BEST + CURRENT Ä‘Ã£ lÆ°u cho 1 cÃ¢u (khi chuyá»ƒn cÃ¢u, load láº¡i káº¿t quáº£ cÅ©).
     */
    SpeakingResultDTO getSavedResults(Long userId, Long sentenceId);
}

