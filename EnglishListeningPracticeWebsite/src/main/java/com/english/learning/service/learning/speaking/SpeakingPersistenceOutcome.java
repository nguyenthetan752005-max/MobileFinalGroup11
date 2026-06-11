package com.english.learning.service.learning.speaking;

import com.english.learning.dto.SpeakingResultDTO;

public record SpeakingPersistenceOutcome(
        String currentAudioUrl,
        SpeakingResultDTO.BestResult bestResult
) {
}

