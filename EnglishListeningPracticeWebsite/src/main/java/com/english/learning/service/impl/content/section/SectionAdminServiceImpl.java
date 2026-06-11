package com.english.learning.service.impl.content.section;

import com.english.learning.dto.AdminSectionRequest;
import com.english.learning.entity.Category;
import com.english.learning.entity.Section;
import com.english.learning.enums.ContentStatus;
import com.english.learning.exception.ResourceInUseException;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.content.section.SectionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SectionAdminServiceImpl implements SectionAdminService {

    private final SectionRepository sectionRepository;
    private final CategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;
    private final SentenceRepository sentenceRepository;

    @Override
    @Transactional
    public Section createSection(AdminSectionRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Section section = new Section();
        applySectionRequest(section, request);
        return sectionRepository.save(section);
    }

    @Override
    @Transactional
    public Section updateSection(Long id, AdminSectionRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section khong ton tai."));
        validateSectionStatusTransition(section.getId(), request.getStatus());
        assertSectionChanged(section, request);
        applySectionRequest(section, request);
        return sectionRepository.save(section);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section khong ton tai."));

        if (lessonRepository.countBySection_Id(id) > 0) {
            throw new ResourceInUseException("Khong the xoa Section khi van con Lesson ben duoi.");
        }

        section.setIsDeleted(true);
        section.setStatus(ContentStatus.ARCHIVED);
        sectionRepository.save(section);
    }

    @Override
    @Transactional
    public void restoreSection(Long id) {
        Section section = sectionRepository.findAnySectionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section khong ton tai."));
        Long categoryId = section.getCategory() != null ? section.getCategory().getId() : null;
        Category category = categoryId != null ? categoryRepository.findAnyCategoryById(categoryId).orElse(null) : null;
        if (category == null || Boolean.TRUE.equals(category.getIsDeleted())) {
            throw new ResourceNotFoundException("Khong the khoi phuc Section khi Category cha dang bi xoa.");
        }
        if (category.getStatus() == ContentStatus.ARCHIVED) {
            throw new ResourceInUseException("Khong the khoi phuc Section khi Category cha dang o trang thai ARCHIVED. Hay khoi phuc Category truoc.");
        }
        section.setCategory(category);
        section.setIsDeleted(false);
        section.setStatus(ContentStatus.DRAFT);
        sectionRepository.save(section);
    }

    @Override
    @Transactional
    public void hardDeleteSection(Long id) {
        Section section = sectionRepository.findAnySectionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section khong ton tai."));
        if (lessonRepository.countAnyBySectionId(id) > 0) {
            throw new ResourceInUseException("Khong the xoa vinh vien Section khi van con Lesson ben duoi.");
        }
        sectionRepository.deleteById(section.getId());
    }

    private void applySectionRequest(Section section, AdminSectionRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category khong ton tai."));
        section.setCategory(category);
        section.setName(request.getName().trim());
        section.setFolderName(normalizeBlank(request.getFolderName()));
        section.setDescription(normalizeBlank(request.getDescription()));
        section.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        section.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
    }

    private void validateStatusForAdminMutation(ContentStatus status) {
        if (status == ContentStatus.ARCHIVED) {
            throw new IllegalArgumentException("ARCHIVED chi duoc dung noi bo cho thung rac.");
        }
    }

    private void validateSectionStatusTransition(Long sectionId, ContentStatus nextStatus) {
        if (nextStatus != ContentStatus.DRAFT) {
            return;
        }
        boolean hasPublishedChildren = lessonRepository.countBySection_IdAndStatus(sectionId, ContentStatus.PUBLISHED) > 0
                || sentenceRepository.countByLesson_Section_IdAndStatus(sectionId, ContentStatus.PUBLISHED) > 0;
        if (hasPublishedChildren) {
            throw new ResourceInUseException("Khong the chuyen Section ve DRAFT khi ben duoi van con du lieu PUBLISHED.");
        }
    }

    private void assertSectionChanged(Section section, AdminSectionRequest request) {
        boolean unchanged = Objects.equals(section.getCategory() != null ? section.getCategory().getId() : null, request.getCategoryId())
                && Objects.equals(section.getName(), request.getName().trim())
                && Objects.equals(section.getFolderName(), normalizeBlank(request.getFolderName()))
                && Objects.equals(section.getDescription(), normalizeBlank(request.getDescription()))
                && Objects.equals(section.getStatus(), request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT)
                && Objects.equals(section.getOrderIndex(), request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        if (unchanged) {
            throw new IllegalArgumentException("Du lieu chua thay doi.");
        }
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

