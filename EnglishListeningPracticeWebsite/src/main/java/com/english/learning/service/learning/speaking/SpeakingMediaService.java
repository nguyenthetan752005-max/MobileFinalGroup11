package com.english.learning.service.learning.speaking;

import java.io.IOException;
import java.util.Map;

public interface SpeakingMediaService {
    Map<String, String> uploadCurrentAudio(byte[] audioBytes, Long userId, Long sentenceId) throws IOException;

    Map<String, String> uploadBestAudio(byte[] audioBytes, Long userId, Long sentenceId) throws IOException;
}

