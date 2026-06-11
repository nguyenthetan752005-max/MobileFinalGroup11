package com.english.learning.service.mobile;

import com.english.learning.dto.mobile.MobileCategoryCollectionResponse;
import com.english.learning.dto.mobile.MobileCategoryResponse;

import java.util.List;

public interface MobileCategoryService {
    List<MobileCategoryResponse> getAllCategories();
    MobileCategoryCollectionResponse getCategoryWithSections(String slug);
    List<com.english.learning.dto.mobile.MobileLessonResponse> getLessonsBySection(Long sectionId);
}
