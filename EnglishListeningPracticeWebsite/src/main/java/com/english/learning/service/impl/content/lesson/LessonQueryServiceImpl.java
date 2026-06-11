package com.english.learning.service.impl.content.lesson;

import com.english.learning.entity.Lesson;
import com.english.learning.enums.ContentStatus;
import com.english.learning.repository.LessonRepository;
import com.english.learning.service.content.lesson.LessonQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonQueryServiceImpl implements LessonQueryService {

    private final LessonRepository lessonRepository;

    @Override
    public List<Lesson> getLessonsBySectionId(Long sectionId) {
        return lessonRepository.findBySection_IdOrderByOrderIndexAscIdAsc(sectionId);
    }

    @Override
    public List<Lesson> getPublishedLessonsBySectionId(Long sectionId) {
        return lessonRepository.findBySection_IdAndStatusOrderByOrderIndexAscIdAsc(sectionId, ContentStatus.PUBLISHED);
    }

    @Override
    public Optional<Lesson> getLessonById(Long id) {
        return lessonRepository.findById(id);
    }

    @Override
    public Optional<Lesson> getPublishedLessonById(Long id) {
        return lessonRepository.findPublishedById(id, ContentStatus.PUBLISHED);
    }
}

