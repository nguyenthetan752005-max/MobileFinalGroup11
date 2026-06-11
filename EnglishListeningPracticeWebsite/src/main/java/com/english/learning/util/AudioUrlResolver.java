package com.english.learning.util;

import com.english.learning.entity.Sentence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Derives the full audio URL for a Sentence from the folder_name hierarchy
 * (Category → Section → Lesson) plus the sentence's file_name,
 * an optional base path, and the ImageKit url-endpoint.
 *
 * Pattern: {urlEndpoint}/{audioBasePath}/{category.folderName}/{section.folderName}/{lesson.folderName}/{sentence.fileName}
 */
@Component
public class AudioUrlResolver {

    private final String urlEndpoint;
    private final String audioBasePath;

    public AudioUrlResolver(
            @Value("${imagekit.url-endpoint:}") String urlEndpoint,
            @Value("${imagekit.audio-base-path:}") String audioBasePath) {
        this.urlEndpoint = normalizeEndpoint(urlEndpoint);
        this.audioBasePath = normalize(audioBasePath);
    }

    /**
     * Build the full audio URL for the given sentence by walking up the entity
     * hierarchy: sentence → lesson → section → category.
     *
     * @return the resolved URL, or {@code null} if any required part is missing.
     */
    public String resolve(Sentence sentence) {
        if (sentence == null) return null;
        String fileName = normalize(sentence.getFileName());
        if (fileName == null) return null;
        if (sentence.getLesson() == null) return null;

        String lessonFolder = normalize(sentence.getLesson().getFolderName());
        if (sentence.getLesson().getSection() == null) return null;

        String sectionFolder = normalize(sentence.getLesson().getSection().getFolderName());
        if (sentence.getLesson().getSection().getCategory() == null) return null;

        String categoryFolder = normalize(sentence.getLesson().getSection().getCategory().getFolderName());

        if (urlEndpoint == null) return null;

        StringBuilder sb = new StringBuilder(urlEndpoint);
        appendSegment(sb, audioBasePath);
        appendSegment(sb, categoryFolder);
        appendSegment(sb, sectionFolder);
        appendSegment(sb, lessonFolder);
        appendSegment(sb, fileName);
        return sb.toString();
    }

    private void appendSegment(StringBuilder sb, String segment) {
        if (segment == null || segment.isEmpty()) return;
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        sb.append(segment);
    }

    private String normalizeEndpoint(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
