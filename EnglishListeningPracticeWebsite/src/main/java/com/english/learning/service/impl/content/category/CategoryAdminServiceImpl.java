package com.english.learning.service.impl.content.category;

import com.english.learning.dto.AdminCategoryRequest;
import com.english.learning.entity.Category;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.enums.PracticeType;
import com.english.learning.exception.ResourceInUseException;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.content.category.CategoryAdminService;
import com.english.learning.service.integration.media.MediaStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryAdminServiceImpl implements CategoryAdminService {

    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final SentenceRepository sentenceRepository;
    private final MediaStorageGateway mediaStorageGateway;

    @Override
    @Transactional
    public Category createCategory(AdminCategoryRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Category category = new Category();
        applyCategoryRequest(category, request);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, AdminCategoryRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category khong ton tai"));
        validateCategoryStatusTransition(category.getId(), request.getStatus());
        assertCategoryChanged(category, request);
        applyCategoryRequest(category, request);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category khong ton tai"));

        if (sectionRepository.countByCategory_Id(id) > 0) {
            throw new ResourceInUseException("Khong the xoa Category. Ban phai xoa het Section ben duoi truoc.");
        }

        category.setIsDeleted(true);
        category.setStatus(ContentStatus.ARCHIVED);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void restoreCategory(Long id) {
        Category category = categoryRepository.findAnyCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category khong ton tai"));
        category.setIsDeleted(false);
        category.setStatus(ContentStatus.DRAFT);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteCategory(Long id) throws Exception {
        Category category = categoryRepository.findAnyCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category khong ton tai"));
        if (sectionRepository.countAnyByCategoryId(id) > 0) {
            throw new ResourceInUseException("Khong the xoa vinh vien Category khi van con Section ben duoi.");
        }
        String imageToDelete = resolveImageStorageKey(category);
        if (imageToDelete != null && !imageToDelete.isBlank()) {
            try {
                mediaStorageGateway.deleteFile(imageToDelete);
            } catch (Exception e) {
                // Log nhung khong chan viec xoa category
                System.err.println("Canh bao: Khong the xoa anh tren ImageKit: " + e.getMessage());
            }
        }
        categoryRepository.deleteById(id);
    }

    private String resolveImageStorageKey(Category category) {
        // Uu tien dung cloudImageId neu co
        if (category.getCloudImageId() != null && !category.getCloudImageId().isBlank()) {
            return category.getCloudImageId();
        }
        // Neu khong co, thu extract tu imageUrl neu la URL ImageKit
        String imageUrl = category.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        // Kiem tra co phai URL ImageKit khong (bat dau bang urlEndpoint)
        String endpoint = mediaStorageGateway.getUrlEndpoint(); // Can them method nay
        if (endpoint != null && imageUrl.startsWith(endpoint)) {
            // Extract file path tu URL: https://ik.imagekit.io/xxx/path/file.jpg -> path/file.jpg
            String filePath = imageUrl.substring(endpoint.length());
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            return filePath;
        }
        return null;
    }

    private void applyCategoryRequest(Category category, AdminCategoryRequest request) {
        String imageUrl = normalizeBlank(request.getImageUrl());
        String cloudImageId = normalizeBlank(request.getCloudImageId());
        replaceStoredAsset(category.getCloudImageId(), cloudImageId);

        category.setName(request.getName().trim());
        category.setImageUrl(imageUrl);
        category.setCloudImageId(cloudImageId);
        category.setLevelRange(normalizeBlank(request.getLevelRange()));
        category.setType(request.getType() != null ? request.getType() : LessonType.AUDIO);
        category.setPracticeType(request.getPracticeType() != null ? request.getPracticeType() : PracticeType.LISTENING);
        category.setFolderName(normalizeBlank(request.getFolderName()));
        category.setDescription(normalizeBlank(request.getDescription()));
        category.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        category.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
    }

    private void validateStatusForAdminMutation(ContentStatus status) {
        if (status == ContentStatus.ARCHIVED) {
            throw new IllegalArgumentException("ARCHIVED chi duoc dung noi bo cho thung rac.");
        }
    }

    private void validateCategoryStatusTransition(Long categoryId, ContentStatus nextStatus) {
        if (nextStatus != ContentStatus.DRAFT) {
            return;
        }
        boolean hasPublishedChildren = sectionRepository.countByCategory_IdAndStatus(categoryId, ContentStatus.PUBLISHED) > 0
                || lessonRepository.countBySection_Category_IdAndStatus(categoryId, ContentStatus.PUBLISHED) > 0
                || sentenceRepository.countByLesson_Section_Category_IdAndStatus(categoryId, ContentStatus.PUBLISHED) > 0;
        if (hasPublishedChildren) {
            throw new ResourceInUseException("Khong the chuyen Category ve DRAFT khi ben duoi van con du lieu PUBLISHED.");
        }
    }

    private void assertCategoryChanged(Category category, AdminCategoryRequest request) {
        boolean unchanged = Objects.equals(category.getName(), request.getName().trim())
                && Objects.equals(category.getImageUrl(), normalizeBlank(request.getImageUrl()))
                && Objects.equals(category.getCloudImageId(), normalizeBlank(request.getCloudImageId()))
                && Objects.equals(category.getLevelRange(), normalizeBlank(request.getLevelRange()))
                && Objects.equals(category.getType(), request.getType() != null ? request.getType() : LessonType.AUDIO)
                && Objects.equals(category.getPracticeType(), request.getPracticeType() != null ? request.getPracticeType() : PracticeType.LISTENING)
                && Objects.equals(category.getFolderName(), normalizeBlank(request.getFolderName()))
                && Objects.equals(category.getDescription(), normalizeBlank(request.getDescription()))
                && Objects.equals(category.getStatus(), request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT)
                && Objects.equals(category.getOrderIndex(), request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        if (unchanged) {
            throw new IllegalArgumentException("Du lieu chua thay doi.");
        }
    }

    private void replaceStoredAsset(String currentPublicId, String nextPublicId) {
        if (Objects.equals(currentPublicId, nextPublicId) || currentPublicId == null || currentPublicId.isBlank()) {
            return;
        }
        try {
            mediaStorageGateway.deleteFile(currentPublicId);
        } catch (Exception e) {
            throw new IllegalStateException("Khong the thay the anh cu tren kho media.");
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

