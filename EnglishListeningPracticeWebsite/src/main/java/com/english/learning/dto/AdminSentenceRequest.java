package com.english.learning.dto;

import com.english.learning.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminSentenceRequest {
    @NotNull(message = "Lesson không được để trống.")
    private Long lessonId;
    @NotBlank(message = "Nội dung sentence không được để trống.")
    private String content;
    private String fileName;
    private String cloudAudioId;
    private Double startTime;
    private Double endTime;
    private Integer orderIndex = 0;
    private ContentStatus status = ContentStatus.DRAFT;
}
