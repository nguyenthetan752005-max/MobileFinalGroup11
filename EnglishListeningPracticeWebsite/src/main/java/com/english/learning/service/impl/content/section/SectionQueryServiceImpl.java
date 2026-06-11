package com.english.learning.service.impl.content.section;

import com.english.learning.entity.Section;
import com.english.learning.enums.ContentStatus;
import com.english.learning.repository.SectionRepository;
import com.english.learning.service.content.section.SectionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SectionQueryServiceImpl implements SectionQueryService {

    private final SectionRepository sectionRepository;

    @Override
    public List<Section> getSectionsByCategoryId(Long categoryId) {
        return sectionRepository.findByCategory_IdOrderByOrderIndexAscIdAsc(categoryId);
    }

    @Override
    public List<Section> getPublishedSectionsByCategoryId(Long categoryId) {
        return sectionRepository.findByCategory_IdAndStatusOrderByOrderIndexAscIdAsc(categoryId, ContentStatus.PUBLISHED);
    }

    @Override
    public Optional<Section> getSectionById(Long id) {
        return sectionRepository.findById(id);
    }

    @Override
    public Optional<Section> getPublishedSectionById(Long categoryId, Long sectionId) {
        return sectionRepository.findPublishedByIdAndCategoryId(sectionId, categoryId, ContentStatus.PUBLISHED);
    }
}

