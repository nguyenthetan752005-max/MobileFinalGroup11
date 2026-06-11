package com.english.learning.service.content.section;

import com.english.learning.entity.Section;

import java.util.List;
import java.util.Optional;

public interface SectionQueryService {
    List<Section> getSectionsByCategoryId(Long categoryId);

    List<Section> getPublishedSectionsByCategoryId(Long categoryId);

    Optional<Section> getSectionById(Long id);

    Optional<Section> getPublishedSectionById(Long categoryId, Long sectionId);
}

