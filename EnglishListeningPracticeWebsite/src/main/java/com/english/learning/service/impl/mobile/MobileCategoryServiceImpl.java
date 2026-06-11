package com.english.learning.service.impl.mobile;

import com.english.learning.dto.mobile.MobileCategoryCollectionResponse;
import com.english.learning.dto.mobile.MobileCategoryCollectionSectionDto;
import com.english.learning.dto.mobile.MobileCategoryResponse;
import com.english.learning.dto.mobile.MobileLessonResponse;
import com.english.learning.entity.Category;
import com.english.learning.enums.ContentStatus;
import com.english.learning.mapper.mobile.MobileResponseMapper;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.service.mobile.MobileCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileCategoryServiceImpl implements MobileCategoryService {

    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final MobileResponseMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<MobileCategoryResponse> getAllCategories() {
        return categoryRepository.findPublishedCategories(ContentStatus.PUBLISHED)
                .stream()
                .map(category -> {
                    MobileCategoryResponse response = mapper.toMobileCategory(category);
                    int actualCount = (int) lessonRepository.countBySection_Category_IdAndStatus(category.getId(), ContentStatus.PUBLISHED);
                    response.setTotalLessons(actualCount);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MobileCategoryCollectionResponse getCategoryWithSections(String slug) {
        Optional<Category> categoryOpt = categoryRepository.findPublishedBySlug(slug, ContentStatus.PUBLISHED);
        if (categoryOpt.isEmpty()) {
            return null;
        }

        Category category = categoryOpt.get();

        List<com.english.learning.entity.Section> sections = sectionRepository
                .findPublishedSectionsByCategoryId(category.getId(), ContentStatus.PUBLISHED);
        
        List<MobileCategoryCollectionSectionDto> sectionDtos = new java.util.ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            com.english.learning.entity.Section section = sections.get(i);
            List<MobileLessonResponse> lessons = lessonRepository
                    .findPublishedLessonsBySectionId(section.getId(), ContentStatus.PUBLISHED)
                    .stream()
                    .map(mapper::toMobileLesson)
                    .collect(Collectors.toList());
            sectionDtos.add(mapper.toMobileCategoryCollectionSection(section, lessons));
        }

        int actualTotalLessons = (int) lessonRepository.countBySection_Category_IdAndStatus(category.getId(), ContentStatus.PUBLISHED);

        return MobileCategoryCollectionResponse.builder()
                .categoryId(category.getId())
                .categorySlug(category.getSlug())
                .categoryName(category.getName())
                .description(category.getDescription())
                .totalLessons(actualTotalLessons)
                .sections(sectionDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MobileLessonResponse> getLessonsBySection(Long sectionId) {
        return lessonRepository.findPublishedLessonsBySectionId(sectionId, ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileLesson)
                .collect(Collectors.toList());
    }
}
