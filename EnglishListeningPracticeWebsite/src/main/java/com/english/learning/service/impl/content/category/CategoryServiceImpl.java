package com.english.learning.service.impl.content.category;

import com.english.learning.dto.AdminCategoryRequest;
import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.entity.Category;
import com.english.learning.entity.User;
import com.english.learning.enums.PracticeType;
import com.english.learning.service.content.category.CategoryAdminService;
import com.english.learning.service.content.category.CategoryService;
import com.english.learning.service.content.category.CategoryViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryViewService categoryViewService;
    private final CategoryAdminService categoryAdminService;

    @Override
    public List<Category> getAllCategories() {
        return categoryViewService.getAllCategories();
    }

    @Override
    public List<Category> getPublishedCategories() {
        return categoryViewService.getPublishedCategories();
    }

    @Override
    public List<Category> getCategoriesByPracticeType(PracticeType practiceType) {
        return categoryViewService.getCategoriesByPracticeType(practiceType);
    }

    @Override
    public List<Category> getPublishedCategoriesByPracticeType(PracticeType practiceType) {
        return categoryViewService.getPublishedCategoriesByPracticeType(practiceType);
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryViewService.getCategoryById(id);
    }

    @Override
    public Optional<Category> getPublishedCategoryById(Long id) {
        return categoryViewService.getPublishedCategoryById(id);
    }

    @Override
    public List<String> getExpandedLevels(String levelRange) {
        return categoryViewService.getExpandedLevels(levelRange);
    }

    @Override
    public List<SectionWithLessonsDTO> getSectionWithLessonsDTOs(Long categoryId, User user, PracticeType practiceType) {
        return categoryViewService.getSectionWithLessonsDTOs(categoryId, user, practiceType);
    }

    @Override
    public Category createCategory(AdminCategoryRequest request) {
        return categoryAdminService.createCategory(request);
    }

    @Override
    public Category updateCategory(Long id, AdminCategoryRequest request) {
        return categoryAdminService.updateCategory(id, request);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryAdminService.deleteCategory(id);
    }

    @Override
    public void restoreCategory(Long id) {
        categoryAdminService.restoreCategory(id);
    }

    @Override
    public void hardDeleteCategory(Long id) throws Exception {
        categoryAdminService.hardDeleteCategory(id);
    }
}

