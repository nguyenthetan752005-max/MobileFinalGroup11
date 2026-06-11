package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileCategoryResponse {
    private Long id;
    private String slug;
    private String name;
    private String imageUrl;
    private String levelRange;
    private String contentType;
    private String practiceType;
    private Integer totalLessons;
    private String description;
    private Integer orderIndex;
}
