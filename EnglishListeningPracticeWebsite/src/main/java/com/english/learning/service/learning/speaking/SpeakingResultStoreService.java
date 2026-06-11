package com.english.learning.service.learning.speaking;

import com.english.learning.dto.SpeakingResultDTO;

public interface SpeakingResultStoreService {
    SpeakingPersistenceOutcome saveEvaluation(byte[] audioBytes, Long userId, Long sentenceId, SpeakingEvaluation evaluation) throws Exception;

    SpeakingResultDTO getSavedResults(Long userId, Long sentenceId);
}

