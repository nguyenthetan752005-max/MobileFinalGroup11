package com.english.learning.service.content.lesson;

import com.english.learning.dto.AdminLessonRequest;
import com.english.learning.entity.Lesson;

public interface LessonAdminService {
    Lesson createLesson(AdminLessonRequest request);

    Lesson updateLesson(Long id, AdminLessonRequest request);

    void deleteLesson(Long id);

    void restoreLesson(Long id);

    void hardDeleteLesson(Long id);
}

