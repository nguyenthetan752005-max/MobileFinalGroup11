package com.english.learning.service.impl.learning.speaking;

import com.english.learning.service.integration.media.MediaStorageGateway;
import com.english.learning.service.learning.speaking.SpeakingMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpeakingMediaServiceImpl implements SpeakingMediaService {

    private final MediaStorageGateway mediaStorageGateway;

    @Override
    public Map<String, String> uploadCurrentAudio(byte[] audioBytes, Long userId, Long sentenceId) throws IOException {
        String currentPublicId = "user_" + userId + "_sentence_" + sentenceId + "_current";
        // Dùng path "speaking" cho user audio
        return mediaStorageGateway.uploadAudio(audioBytes, currentPublicId, "speaking");
    }

    @Override
    public Map<String, String> uploadBestAudio(byte[] audioBytes, Long userId, Long sentenceId) throws IOException {
        String bestPublicId = "user_" + userId + "_sentence_" + sentenceId + "_best";
        // Dùng path "speaking" cho user audio
        return mediaStorageGateway.uploadAudio(audioBytes, bestPublicId, "speaking");
    }
}

