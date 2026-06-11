package com.english.learning.service.learning.speaking;

import org.springframework.web.multipart.MultipartFile;

public interface SpeakingEvaluationService {
    SpeakingEvaluation evaluate(MultipartFile audio, String referenceText);
}

