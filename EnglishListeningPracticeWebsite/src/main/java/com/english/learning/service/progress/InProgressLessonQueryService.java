package com.english.learning.service.progress;

import com.english.learning.dto.InProgressLessonDTO;

import java.util.List;

public interface InProgressLessonQueryService {
    List<InProgressLessonDTO> getInProgressLessons(Long userId);
}

