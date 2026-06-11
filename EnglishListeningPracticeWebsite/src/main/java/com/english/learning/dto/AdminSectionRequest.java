package com.english.learning.dto;

import com.english.learning.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminSectionRequest {
    @NotNull(message = "Danh mục không được để trống.")
    private Long categoryId;
    @NotBlank(message = "Tên section không được để trống.")
    private String name;
    private String folderName;
    private String description;
    private ContentStatus status = ContentStatus.DRAFT;
    private Integer orderIndex = 0;
}
