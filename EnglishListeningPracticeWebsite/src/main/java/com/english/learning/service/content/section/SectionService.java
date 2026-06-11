package com.english.learning.service.content.section;

import com.english.learning.entity.Section;

import java.util.List;
import java.util.Optional;

public interface SectionService {
    List<Section> getSectionsByCategoryId(Long categoryId);
    List<Section> getPublishedSectionsByCategoryId(Long categoryId);
    Optional<Section> getSectionById(Long id);
    Optional<Section> getPublishedSectionById(Long categoryId, Long sectionId);
    Section createSection(com.english.learning.dto.AdminSectionRequest request);
    Section updateSection(Long id, com.english.learning.dto.AdminSectionRequest request);
    void deleteSection(Long id);
    void restoreSection(Long id);
    void hardDeleteSection(Long id);
}

