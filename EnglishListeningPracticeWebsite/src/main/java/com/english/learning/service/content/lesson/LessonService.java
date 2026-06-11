package com.english.learning.service.content.lesson;

import com.english.learning.entity.Lesson;

import com.english.learning.dto.LessonNavigationDTO;
import com.english.learning.enums.PracticeType;

import java.util.List;
import java.util.Optional;

public interface LessonService {
    List<Lesson> getLessonsBySectionId(Long sectionId);
    List<Lesson> getPublishedLessonsBySectionId(Long sectionId);
    Optional<Lesson> getLessonById(Long id);
    Optional<Lesson> getPublishedLessonById(Long id);
    LessonNavigationDTO getLessonNavigation(Lesson currentLesson, PracticeType practiceType);
    Lesson createLesson(com.english.learning.dto.AdminLessonRequest request);
    Lesson updateLesson(Long id, com.english.learning.dto.AdminLessonRequest request);
    void deleteLesson(Long id);
    void restoreLesson(Long id);
    void hardDeleteLesson(Long id);
}

