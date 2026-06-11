package com.english.learning.service.content.lesson;

import com.english.learning.dto.LessonNavigationDTO;
import com.english.learning.entity.Lesson;
import com.english.learning.enums.PracticeType;

public interface LessonNavigationService {
    LessonNavigationDTO getLessonNavigation(Lesson currentLesson, PracticeType practiceType);
}

