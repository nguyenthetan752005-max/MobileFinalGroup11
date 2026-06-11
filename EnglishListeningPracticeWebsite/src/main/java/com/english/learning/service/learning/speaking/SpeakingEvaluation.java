package com.english.learning.service.learning.speaking;

public record SpeakingEvaluation(
        String referenceText,
        String transcribedText,
        int score,
        String feedback
) {
}

