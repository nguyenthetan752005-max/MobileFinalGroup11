package com.english.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonNavigationDTO {
    private LessonDTO nextLesson;
    private boolean isLastLessonInSection;
}
