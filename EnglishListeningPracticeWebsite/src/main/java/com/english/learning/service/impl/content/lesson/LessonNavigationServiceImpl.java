package com.english.learning.service.impl.content.lesson;

import com.english.learning.dto.LessonDTO;
import com.english.learning.dto.LessonNavigationDTO;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.enums.PracticeType;
import com.english.learning.service.content.lesson.LessonNavigationService;
import com.english.learning.service.content.lesson.LessonQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonNavigationServiceImpl implements LessonNavigationService {

    private final LessonQueryService lessonQueryService;

    @Override
    public LessonNavigationDTO getLessonNavigation(Lesson currentLesson, PracticeType practiceType) {
        Section section = currentLesson.getSection();
        Lesson nextLesson = null;
        boolean isLastLessonInSection = false;

        if (section != null) {
            List<Lesson> sectionLessons = lessonQueryService.getPublishedLessonsBySectionId(section.getId()).stream()
                    .filter(lesson -> lesson.getSection().getCategory().getPracticeType() == practiceType)
                    .sorted(Comparator.comparing(Lesson::getId))
                    .toList();

            int currentIndex = -1;
            for (int i = 0; i < sectionLessons.size(); i++) {
                if (sectionLessons.get(i).getId().equals(currentLesson.getId())) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex >= 0 && currentIndex < sectionLessons.size() - 1) {
                nextLesson = sectionLessons.get(currentIndex + 1);
            } else if (currentIndex == sectionLessons.size() - 1) {
                isLastLessonInSection = true;
            }
        }

        LessonDTO nextLessonDto = null;
        if (nextLesson != null) {
            nextLessonDto = LessonDTO.builder()
                    .id(nextLesson.getId())
                    .sectionId(nextLesson.getSection() != null ? nextLesson.getSection().getId() : null)
                    .type(nextLesson.getSection() != null && nextLesson.getSection().getCategory() != null
                            ? nextLesson.getSection().getCategory().getType()
                            : null)
                    .youtubeVideoId(nextLesson.getYoutubeVideoId())
                    .title(nextLesson.getTitle())
                    .level(nextLesson.getLevel())
                    .totalSentences(nextLesson.getTotalSentences())
                    .build();
        }

        return new LessonNavigationDTO(nextLessonDto, isLastLessonInSection);
    }
}

