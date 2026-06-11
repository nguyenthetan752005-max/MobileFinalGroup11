package com.english.learning.service.impl.content.category;

import com.english.learning.dto.LessonDTO;
import com.english.learning.dto.SectionDTO;
import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.entity.Category;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.entity.User;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.PracticeType;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.service.content.category.CategoryViewService;
import com.english.learning.service.content.lesson.LessonService;
import com.english.learning.service.content.section.SectionService;
import com.english.learning.service.progress.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryViewServiceImpl implements CategoryViewService {

    private final CategoryRepository categoryRepository;
    private final SectionService sectionService;
    private final LessonService lessonService;
    private final UserProgressService userProgressService;
    private final LessonRepository lessonRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByOrderIndexAscIdAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getPublishedCategories() {
        List<Category> categories = categoryRepository.findByStatusOrderByOrderIndexAscIdAsc(ContentStatus.PUBLISHED);
        categories.forEach(category -> {
            int actualCount = (int) lessonRepository.countBySection_Category_IdAndStatus(category.getId(),
                    ContentStatus.PUBLISHED);
            category.setTotalLessons(actualCount);
        });
        return categories;
    }

    @Override
    public List<Category> getCategoriesByPracticeType(PracticeType practiceType) {
        return categoryRepository.findByPracticeType(practiceType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getPublishedCategoriesByPracticeType(PracticeType practiceType) {
        List<Category> categories = categoryRepository
                .findByPracticeTypeAndStatusOrderByOrderIndexAscIdAsc(practiceType, ContentStatus.PUBLISHED);
        categories.forEach(category -> {
            int actualCount = (int) lessonRepository.countBySection_Category_IdAndStatus(category.getId(),
                    ContentStatus.PUBLISHED);
            category.setTotalLessons(actualCount);
        });
        return categories;
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> getPublishedCategoryById(Long id) {
        return categoryRepository.findByIdAndStatus(id, ContentStatus.PUBLISHED);
    }

    @Override
    public List<String> getExpandedLevels(String levelRange) {
        List<String> allLevels = List.of("A1", "A2", "B1", "B2", "C1", "C2");
        List<String> expandedLevels = new java.util.ArrayList<>();

        if (levelRange != null && levelRange.contains("-")) {
            String[] parts = levelRange.split("-");
            String start = parts[0].trim().toUpperCase();
            String end = parts[1].trim().toUpperCase();
            int startIdx = allLevels.indexOf(start);
            int endIdx = allLevels.indexOf(end);
            if (startIdx >= 0 && endIdx >= 0 && startIdx <= endIdx) {
                expandedLevels = allLevels.subList(startIdx, endIdx + 1);
            }
        } else if (levelRange != null && !levelRange.trim().isEmpty()) {
            expandedLevels = List.of(levelRange.trim().toUpperCase());
        }

        return expandedLevels;
    }

    @Override
    public List<SectionWithLessonsDTO> getSectionWithLessonsDTOs(Long categoryId, User user,
            PracticeType practiceType) {
        List<Section> sections = sectionService.getPublishedSectionsByCategoryId(categoryId);
        return sections.stream()
                .map(sec -> {
                    List<Lesson> filteredLessons = lessonService.getPublishedLessonsBySectionId(sec.getId()).stream()
                            .filter(l -> l.getSection().getCategory().getPracticeType() == practiceType)
                            .toList();

                    Map<Long, UserProgressStatus> lessonStatuses = new HashMap<>();
                    UserProgressStatus sectionStatus = null;

                    if (user != null) {
                        for (Lesson lesson : filteredLessons) {
                            UserProgressStatus lessonStatus = userProgressService.getLessonStatus(user.getId(),
                                    lesson.getId());
                            if (lessonStatus != null) {
                                lessonStatuses.put(lesson.getId(), lessonStatus);
                            }
                        }
                        sectionStatus = userProgressService.getSectionStatus(user.getId(), sec.getId());
                    }

                    SectionDTO sectionDto = SectionDTO.builder()
                            .id(sec.getId())
                            .categoryId(sec.getCategory() != null ? sec.getCategory().getId() : null)
                            .name(sec.getName())
                            .description(sec.getDescription())
                            .build();

                    List<LessonDTO> lessonDtos = filteredLessons.stream()
                            .map(lesson -> LessonDTO.builder()
                                    .id(lesson.getId())
                                    .sectionId(lesson.getSection() != null ? lesson.getSection().getId() : null)
                                    .type(lesson.getSection() != null && lesson.getSection().getCategory() != null
                                            ? lesson.getSection().getCategory().getType()
                                            : null)
                                    .youtubeVideoId(lesson.getYoutubeVideoId())
                                    .title(lesson.getTitle())
                                    .level(lesson.getLevel())
                                    .totalSentences(lesson.getTotalSentences())
                                    .build())
                            .toList();

                    return new SectionWithLessonsDTO(sectionDto, lessonDtos, lessonStatuses, sectionStatus);
                })
                .filter(dto -> !dto.getLessons().isEmpty())
                .toList();
    }
}
