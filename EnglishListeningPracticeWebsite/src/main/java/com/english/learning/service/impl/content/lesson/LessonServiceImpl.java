package com.english.learning.service.impl.content.lesson;

import com.english.learning.dto.AdminLessonRequest;
import com.english.learning.dto.LessonNavigationDTO;
import com.english.learning.entity.Lesson;
import com.english.learning.enums.PracticeType;
import com.english.learning.service.content.lesson.LessonAdminService;
import com.english.learning.service.content.lesson.LessonNavigationService;
import com.english.learning.service.content.lesson.LessonQueryService;
import com.english.learning.service.content.lesson.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonQueryService lessonQueryService;
    private final LessonNavigationService lessonNavigationService;
    private final LessonAdminService lessonAdminService;

    @Override
    public List<Lesson> getLessonsBySectionId(Long sectionId) {
        return lessonQueryService.getLessonsBySectionId(sectionId);
    }

    @Override
    public List<Lesson> getPublishedLessonsBySectionId(Long sectionId) {
        return lessonQueryService.getPublishedLessonsBySectionId(sectionId);
    }

    @Override
    public Optional<Lesson> getLessonById(Long id) {
        return lessonQueryService.getLessonById(id);
    }

    @Override
    public Optional<Lesson> getPublishedLessonById(Long id) {
        return lessonQueryService.getPublishedLessonById(id);
    }

    @Override
    public LessonNavigationDTO getLessonNavigation(Lesson currentLesson, PracticeType practiceType) {
        return lessonNavigationService.getLessonNavigation(currentLesson, practiceType);
    }

    @Override
    public Lesson createLesson(AdminLessonRequest request) {
        return lessonAdminService.createLesson(request);
    }

    @Override
    public Lesson updateLesson(Long id, AdminLessonRequest request) {
        return lessonAdminService.updateLesson(id, request);
    }

    @Override
    public void deleteLesson(Long id) {
        lessonAdminService.deleteLesson(id);
    }

    @Override
    public void restoreLesson(Long id) {
        lessonAdminService.restoreLesson(id);
    }

    @Override
    public void hardDeleteLesson(Long id) {
        lessonAdminService.hardDeleteLesson(id);
    }
}

