package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileLessonResponse {
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
}
