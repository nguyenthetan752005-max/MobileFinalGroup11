package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Lesson detail response: lesson metadata + all sentences.
 * Returned by GET /api/mobile/lessons/{id}.
 */
@Data
@Builder
public class MobileLessonDetailResponse {
    private Long id;
    private Long sectionId;
    private String title;
    private String level;
    private String displayType;
    private String contentType;
    private Integer totalSentences;
    private Integer passThreshold;
    private String youtubeVideoId;
    private Integer orderIndex;
    private List<MobileSentenceResponse> sentences;
}
