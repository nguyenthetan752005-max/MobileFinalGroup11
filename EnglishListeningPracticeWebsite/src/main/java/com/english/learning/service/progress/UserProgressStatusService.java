package com.english.learning.service.progress;

import com.english.learning.enums.UserProgressStatus;

public interface UserProgressStatusService {
    UserProgressStatus getLessonStatus(Long userId, Long lessonId);

    UserProgressStatus getSectionStatus(Long userId, Long sectionId);

    UserProgressStatus getCategoryStatus(Long userId, Long categoryId);
}

