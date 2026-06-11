package com.english.learning.dto;

import com.english.learning.enums.LessonType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDTO {
    private Long id;
    private Long sectionId;
    private LessonType type;
    private String youtubeVideoId;
    private String title;
    private String level;
    private Integer totalSentences;
}
