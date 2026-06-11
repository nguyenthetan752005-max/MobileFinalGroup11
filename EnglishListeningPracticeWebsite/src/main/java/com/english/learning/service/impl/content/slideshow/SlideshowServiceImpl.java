package com.english.learning.service.impl.content.slideshow;

import com.english.learning.dto.AdminSlideshowRequest;
import com.english.learning.entity.Slideshow;
import com.english.learning.enums.SlideshowPosition;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.SlideshowRepository;
import com.english.learning.service.content.slideshow.SlideshowService;
import com.english.learning.service.integration.media.MediaStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SlideshowServiceImpl implements SlideshowService {

    private final SlideshowRepository slideshowRepository;
    private final MediaStorageGateway mediaStorageGateway;

    @Override
    public List<Slideshow> getActiveSlideshowsByPosition(SlideshowPosition position) {
        return slideshowRepository.findByIsActiveTrueAndPositionOrderByDisplayOrderAscIdAsc(position);
    }

    @Override
    @Transactional
    public Slideshow createSlideshow(AdminSlideshowRequest request) {
        Slideshow slideshow = new Slideshow();
        applyRequest(slideshow, request);
        return slideshowRepository.save(slideshow);
    }

    @Override
    @Transactional
    public Slideshow updateSlideshow(Long id, AdminSlideshowRequest request) {
        Slideshow slideshow = slideshowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slideshow khong ton tai."));
        assertChanged(slideshow, request);
        String previousCloudImageId = slideshow.getCloudImageId();
        String previousImageUrl = slideshow.getImageUrl();
        applyRequest(slideshow, request);
        Slideshow savedSlideshow = slideshowRepository.save(slideshow);
        deleteObsoleteStoredAsset(previousCloudImageId, previousImageUrl, savedSlideshow.getCloudImageId());
        return savedSlideshow;
    }

    @Override
    @Transactional
    public void softDeleteSlideshow(Long id) {
        Slideshow slideshow = slideshowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slideshow khong ton tai."));
        slideshow.setIsDeleted(true);
        slideshow.setIsActive(false);
        slideshowRepository.save(slideshow);
    }

    @Override
    @Transactional
    public void restoreSlideshow(Long id) {
        Slideshow slideshow = slideshowRepository.findAnyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slideshow khong ton tai."));
        slideshow.setIsDeleted(false);
        slideshow.setIsActive(false);
        slideshowRepository.save(slideshow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteSlideshow(Long id) throws Exception {
        Slideshow slideshow = slideshowRepository.findAnyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slideshow khong ton tai."));
        String storedAssetKey = resolveStoredAssetKey(slideshow.getCloudImageId(), slideshow.getImageUrl());
        if (storedAssetKey != null && !storedAssetKey.isBlank()) {
            mediaStorageGateway.deleteFile(storedAssetKey);
        }
        slideshowRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        Slideshow slideshow = slideshowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slideshow khong ton tai."));
        slideshow.setIsActive(!Boolean.TRUE.equals(slideshow.getIsActive()));
        slideshowRepository.save(slideshow);
    }

    private void applyRequest(Slideshow slideshow, AdminSlideshowRequest request) {
        String nextImageUrl = normalizeBlank(request.getImageUrl());
        String nextCloudImageId = normalizeBlank(request.getCloudImageId());
        slideshow.setTitle(request.getTitle().trim());
        slideshow.setImageUrl(nextImageUrl);
        slideshow.setCloudImageId(nextCloudImageId);
        slideshow.setLinkUrl(normalizeBlank(request.getLinkUrl()));
        slideshow.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        slideshow.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        slideshow.setPosition(request.getPosition() != null ? request.getPosition() : SlideshowPosition.HOME);
    }

    private void assertChanged(Slideshow slideshow, AdminSlideshowRequest request) {
        boolean unchanged = Objects.equals(slideshow.getTitle(), request.getTitle().trim())
                && Objects.equals(slideshow.getImageUrl(), normalizeBlank(request.getImageUrl()))
                && Objects.equals(slideshow.getCloudImageId(), normalizeBlank(request.getCloudImageId()))
                && Objects.equals(slideshow.getLinkUrl(), normalizeBlank(request.getLinkUrl()))
                && Objects.equals(slideshow.getDisplayOrder(), request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                && Objects.equals(slideshow.getIsActive(), Boolean.TRUE.equals(request.getIsActive()))
                && Objects.equals(slideshow.getPosition(), request.getPosition() != null ? request.getPosition() : SlideshowPosition.HOME);
        if (unchanged) {
            throw new IllegalArgumentException("Du lieu chua thay doi.");
        }
    }

    private void deleteObsoleteStoredAsset(String currentPublicId, String currentImageUrl, String nextPublicId) {
        String resolvedCurrentId = resolveStoredAssetKey(currentPublicId, currentImageUrl);
        if (Objects.equals(resolvedCurrentId, nextPublicId) || resolvedCurrentId == null || resolvedCurrentId.isBlank()) {
            return;
        }
        try {
            mediaStorageGateway.deleteFile(resolvedCurrentId);
        } catch (Exception e) {
            throw new IllegalStateException("Khong the thay the anh slideshow cu tren kho media.");
        }
    }

    private String resolveStoredAssetKey(String publicId, String imageUrl) {
        if (publicId != null && !publicId.isBlank()) {
            return publicId;
        }
        return normalizeBlank(imageUrl);
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
