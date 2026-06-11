package com.english.learning.service.content.category;

import com.english.learning.dto.AdminCategoryRequest;
import com.english.learning.entity.Category;

public interface CategoryAdminService {
    Category createCategory(AdminCategoryRequest request);

    Category updateCategory(Long id, AdminCategoryRequest request);

    void deleteCategory(Long id);

    void restoreCategory(Long id);

    void hardDeleteCategory(Long id) throws Exception;
}

