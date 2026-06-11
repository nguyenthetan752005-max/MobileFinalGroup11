package com.english.learning.service.impl.content.section;

import com.english.learning.dto.AdminSectionRequest;
import com.english.learning.entity.Section;
import com.english.learning.service.content.section.SectionAdminService;
import com.english.learning.service.content.section.SectionQueryService;
import com.english.learning.service.content.section.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionQueryService sectionQueryService;
    private final SectionAdminService sectionAdminService;

    @Override
    public List<Section> getSectionsByCategoryId(Long categoryId) {
        return sectionQueryService.getSectionsByCategoryId(categoryId);
    }

    @Override
    public List<Section> getPublishedSectionsByCategoryId(Long categoryId) {
        return sectionQueryService.getPublishedSectionsByCategoryId(categoryId);
    }

    @Override
    public Optional<Section> getSectionById(Long id) {
        return sectionQueryService.getSectionById(id);
    }

    @Override
    public Optional<Section> getPublishedSectionById(Long categoryId, Long sectionId) {
        return sectionQueryService.getPublishedSectionById(categoryId, sectionId);
    }

    @Override
    public Section createSection(AdminSectionRequest request) {
        return sectionAdminService.createSection(request);
    }

    @Override
    public Section updateSection(Long id, AdminSectionRequest request) {
        return sectionAdminService.updateSection(id, request);
    }

    @Override
    public void deleteSection(Long id) {
        sectionAdminService.deleteSection(id);
    }

    @Override
    public void restoreSection(Long id) {
        sectionAdminService.restoreSection(id);
    }

    @Override
    public void hardDeleteSection(Long id) {
        sectionAdminService.hardDeleteSection(id);
    }
}

