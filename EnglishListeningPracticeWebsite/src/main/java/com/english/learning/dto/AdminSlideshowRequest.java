package com.english.learning.dto;

import com.english.learning.enums.SlideshowPosition;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminSlideshowRequest {
    @NotBlank(message = "Tiêu đề slideshow không được để trống.")
    private String title;

    @NotBlank(message = "Ảnh slideshow không được để trống.")
    private String imageUrl;

    private String cloudImageId;
    private String linkUrl;
    private Integer displayOrder = 0;
    private Boolean isActive = true;
    private SlideshowPosition position = SlideshowPosition.HOME;
}
