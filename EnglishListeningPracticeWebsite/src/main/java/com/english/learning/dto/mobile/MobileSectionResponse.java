package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileSectionResponse {
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private Integer orderIndex;
}
