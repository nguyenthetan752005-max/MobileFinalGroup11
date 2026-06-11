package com.english.learning.service.impl.learning.speaking;

import com.english.learning.dto.SpeakingResultDTO;
import com.english.learning.service.learning.speaking.SpeakingEvaluation;
import com.english.learning.service.learning.speaking.SpeakingEvaluationService;
import com.english.learning.service.learning.speaking.SpeakingPersistenceOutcome;
import com.english.learning.service.learning.speaking.SpeakingResultStoreService;
import com.english.learning.service.learning.speaking.SpeakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SpeakingServiceImpl implements SpeakingService {

    private final SpeakingEvaluationService speakingEvaluationService;
    private final SpeakingResultStoreService speakingResultStoreService;

    @Override
    public SpeakingResultDTO evaluateSpeaking(MultipartFile audio, String referenceText, Long userId, Long sentenceId) {
        SpeakingEvaluation evaluation = speakingEvaluationService.evaluate(audio, referenceText);

        SpeakingResultDTO dto = new SpeakingResultDTO();
        dto.setReferenceText(evaluation.referenceText());
        dto.setTranscribedText(evaluation.transcribedText());
        dto.setScore(evaluation.score());
        dto.setFeedback(evaluation.feedback());

        if (userId != null && sentenceId != null) {
            try {
                SpeakingPersistenceOutcome outcome = speakingResultStoreService
                        .saveEvaluation(audio.getBytes(), userId, sentenceId, evaluation);
                dto.setAudioUrl(outcome.currentAudioUrl());
                dto.setBestResult(outcome.bestResult());
            } catch (Exception e) {
                System.err.println("============= LOI UPLOAD/LUU SPEAKING RESULT =============");
                System.err.println("Ly do loi: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return dto;
    }

    @Override
    public SpeakingResultDTO getSavedResults(Long userId, Long sentenceId) {
        return speakingResultStoreService.getSavedResults(userId, sentenceId);
    }
}

