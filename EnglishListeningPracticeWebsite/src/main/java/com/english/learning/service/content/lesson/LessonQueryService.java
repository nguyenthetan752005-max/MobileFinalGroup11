package com.english.learning.service.content.lesson;

import com.english.learning.entity.Lesson;

import java.util.List;
import java.util.Optional;

public interface LessonQueryService {
    List<Lesson> getLessonsBySectionId(Long sectionId);

    List<Lesson> getPublishedLessonsBySectionId(Long sectionId);

    Optional<Lesson> getLessonById(Long id);

    Optional<Lesson> getPublishedLessonById(Long id);
}

