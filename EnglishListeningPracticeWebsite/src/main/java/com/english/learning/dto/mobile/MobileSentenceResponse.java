package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileSentenceResponse {
    private Long id;
    private Long lessonId;
    private String audioUrl;
    private String content;
    private String hintText;
    private Integer durationMillis;
    private Double startTime;
    private Double endTime;
    private Integer orderIndex;
    private java.util.List<String> properNouns;
}
