package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileSentenceProgressResponse {
    private Long sentenceId;
    private Long lessonId;
    private String status;
}
