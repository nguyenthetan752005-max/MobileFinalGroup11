package com.english.learning.service.content.category;

import com.english.learning.entity.Category;

import com.english.learning.enums.PracticeType;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    List<Category> getPublishedCategories();
    List<Category> getCategoriesByPracticeType(PracticeType practiceType);
    List<Category> getPublishedCategoriesByPracticeType(PracticeType practiceType);
    Optional<Category> getCategoryById(Long id);
    Optional<Category> getPublishedCategoryById(Long id);
    List<String> getExpandedLevels(String levelRange);
    List<com.english.learning.dto.SectionWithLessonsDTO> getSectionWithLessonsDTOs(Long categoryId, com.english.learning.entity.User user, PracticeType practiceType);
    Category createCategory(com.english.learning.dto.AdminCategoryRequest request);
    Category updateCategory(Long id, com.english.learning.dto.AdminCategoryRequest request);
    void deleteCategory(Long id);
    void restoreCategory(Long id);
    void hardDeleteCategory(Long id) throws Exception;
}

