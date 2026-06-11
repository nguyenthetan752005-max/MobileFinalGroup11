package com.english.learning.service.impl.learning.speaking;

import com.english.learning.entity.SpeakingResult;
import com.english.learning.service.integration.scoring.PronunciationScoringGateway;
import com.english.learning.service.learning.speaking.SpeakingEvaluation;
import com.english.learning.service.learning.speaking.SpeakingEvaluationService;
import com.english.learning.service.integration.transcription.SpeechTranscriptionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SpeakingEvaluationServiceImpl implements SpeakingEvaluationService {

    private final SpeechTranscriptionGateway speechTranscriptionGateway;
    private final PronunciationScoringGateway pronunciationScoringGateway;

    @Override
    public SpeakingEvaluation evaluate(MultipartFile audio, String referenceText) {
        String transcribedText = "";
        try {
            transcribedText = speechTranscriptionGateway.transcribeAudio(audio);
        } catch (Exception e) {
            System.err.println("Error transcribing audio (silent or noisy?): " + e.getMessage());
        }

        SpeakingResult scoredResult = pronunciationScoringGateway.scoreSpeaking(referenceText, transcribedText);
        return new SpeakingEvaluation(
                referenceText,
                transcribedText,
                scoredResult.getScore() != null ? scoredResult.getScore() : 0,
                scoredResult.getFeedback()
        );
    }
}

