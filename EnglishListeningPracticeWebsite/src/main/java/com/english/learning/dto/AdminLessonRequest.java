package com.english.learning.dto;

import com.english.learning.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminLessonRequest {
    @NotNull(message = "Section không được để trống.")
    private Long sectionId;
    @NotBlank(message = "Tiêu đề bài học không được để trống.")
    private String title;
    private String youtubeVideoId;
    private String folderName;
    private String level;
    private ContentStatus status = ContentStatus.DRAFT;
    private Integer orderIndex = 0;
}
