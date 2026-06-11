package com.english.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InProgressLessonDTO {
    private Long lessonId;
    private String lessonTitle;
    private String categoryName;
    private String sectionName;
    private String practiceType; // LISTENING or SPEAKING
    private int totalSentences;
    private int completedSentences;
    private int progressPercent;
    private Long firstUncompletedSentenceId;
    private int firstUncompletedSentenceIndex;
}
