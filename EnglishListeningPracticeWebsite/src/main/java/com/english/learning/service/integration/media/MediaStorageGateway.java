package com.english.learning.service.integration.media;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface MediaStorageGateway {
    Map<String, String> uploadFile(MultipartFile file) throws IOException;

    Map<String, String> uploadFile(MultipartFile file, String resourceType, String folder) throws IOException;

    Map<String, String> uploadFile(MultipartFile file, String resourceType, String folder, String publicId, boolean overwrite) throws IOException;

    Map<String, String> uploadAudio(byte[] audioData, String publicId) throws IOException;

    /**
     * Upload audio với type cụ thể: "lessons" hoặc "speaking"
     * - "lessons" → IMAGEKIT_AUDIO_LESSONS_PATH (EnglishListeningData/AudioLessons)
     * - "speaking" → IMAGEKIT_AUDIO_SPEAKING_PATH (EnglishListeningData/AudioSpeaking)
     */
    Map<String, String> uploadAudio(byte[] audioData, String publicId, String audioType) throws IOException;

    void deleteFile(String publicId) throws Exception;

    String getUrlEndpoint();
}

