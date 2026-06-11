package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobileCategoryCollectionResponse {
    private Long categoryId;
    private String categorySlug;
    private String categoryName;
    private String description;
    private Integer totalLessons;
    private List<MobileCategoryCollectionSectionDto> sections;
}
