package com.english.learning.dto;

import com.english.learning.enums.UserProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionWithLessonsDTO {
    private SectionDTO section;
    private List<LessonDTO> lessons;
    private Map<Long, UserProgressStatus> lessonStatuses;
    private UserProgressStatus sectionStatus;
}
