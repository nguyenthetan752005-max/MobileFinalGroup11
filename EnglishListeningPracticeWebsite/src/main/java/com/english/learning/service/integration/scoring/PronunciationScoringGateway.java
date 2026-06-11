package com.english.learning.service.integration.scoring;

import com.english.learning.entity.SpeakingResult;

public interface PronunciationScoringGateway {
    SpeakingResult scoreSpeaking(String expectedText, String userText);
}

