package com.english.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeakingResultDTO {
    private String referenceText;
    private String transcribedText;
    private int score; // 0-100
    private String feedback;
    private String audioUrl; // Storage URL of current audio

    // Nested: Kết quả best (nếu có)
    private BestResult bestResult;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BestResult {
        private int score;
        private String transcribedText;
        private String feedback;
        private String audioUrl;
    }
}
