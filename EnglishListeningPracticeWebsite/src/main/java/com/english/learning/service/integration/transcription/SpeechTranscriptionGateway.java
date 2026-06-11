package com.english.learning.service.integration.transcription;

import org.springframework.web.multipart.MultipartFile;

public interface SpeechTranscriptionGateway {
    String transcribeAudio(MultipartFile audioFile) throws Exception;
}

