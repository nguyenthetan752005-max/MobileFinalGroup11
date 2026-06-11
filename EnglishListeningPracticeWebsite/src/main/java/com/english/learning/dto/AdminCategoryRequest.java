package com.english.learning.dto;

import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.enums.PracticeType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống.")
    private String name;
    private String imageUrl;
    private String cloudImageId;
    private String levelRange;
    private LessonType type = LessonType.AUDIO;
    private PracticeType practiceType = PracticeType.LISTENING;
    private String folderName;
    private String description;
    private ContentStatus status = ContentStatus.DRAFT;
    private Integer orderIndex = 0;
}
