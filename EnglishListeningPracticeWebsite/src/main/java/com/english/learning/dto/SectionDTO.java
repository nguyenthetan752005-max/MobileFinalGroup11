package com.english.learning.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionDTO {
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
}
