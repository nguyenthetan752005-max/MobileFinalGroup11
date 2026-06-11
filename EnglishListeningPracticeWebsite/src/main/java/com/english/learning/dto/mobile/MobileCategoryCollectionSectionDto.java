package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobileCategoryCollectionSectionDto {
    private Long id;
    private String name;
    private String description;
    private Integer orderIndex;
    private List<MobileLessonResponse> lessons;
}
