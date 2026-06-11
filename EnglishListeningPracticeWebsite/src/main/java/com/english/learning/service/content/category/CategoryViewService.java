package com.english.learning.service.content.category;

import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.entity.Category;
import com.english.learning.entity.User;
import com.english.learning.enums.PracticeType;

import java.util.List;
import java.util.Optional;

public interface CategoryViewService {
    List<Category> getAllCategories();

    List<Category> getPublishedCategories();

    List<Category> getCategoriesByPracticeType(PracticeType practiceType);

    List<Category> getPublishedCategoriesByPracticeType(PracticeType practiceType);

    Optional<Category> getCategoryById(Long id);

    Optional<Category> getPublishedCategoryById(Long id);

    List<String> getExpandedLevels(String levelRange);

    List<SectionWithLessonsDTO> getSectionWithLessonsDTOs(Long categoryId, User user, PracticeType practiceType);
}

