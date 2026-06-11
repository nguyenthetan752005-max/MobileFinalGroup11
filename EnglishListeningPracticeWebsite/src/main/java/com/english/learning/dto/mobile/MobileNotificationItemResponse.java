package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MobileNotificationItemResponse {
    private Long id;
    private String type;
    private String title;
    private String body;
    private String meta;
    private String timeAgo;
    private Boolean read;
    private LocalDateTime eventAt;
    private Long targetLessonId;
    private Long targetSentenceId;
    private Long targetCommentId;
}
