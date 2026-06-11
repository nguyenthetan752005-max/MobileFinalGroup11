package com.english.learning.service.content.slideshow;

import com.english.learning.dto.AdminSlideshowRequest;
import com.english.learning.entity.Slideshow;
import com.english.learning.enums.SlideshowPosition;

import java.util.List;

public interface SlideshowService {
    List<Slideshow> getActiveSlideshowsByPosition(SlideshowPosition position);
    Slideshow createSlideshow(AdminSlideshowRequest request);
    Slideshow updateSlideshow(Long id, AdminSlideshowRequest request);
    void softDeleteSlideshow(Long id);
    void restoreSlideshow(Long id);
    void hardDeleteSlideshow(Long id) throws Exception;
    void toggleActive(Long id);
}

