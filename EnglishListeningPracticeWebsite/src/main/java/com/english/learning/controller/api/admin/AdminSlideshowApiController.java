package com.english.learning.controller.api.admin;

import com.english.learning.dto.AdminSlideshowRequest;
import com.english.learning.service.content.slideshow.SlideshowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSlideshowApiController {

    private final SlideshowService slideshowService;

    @PostMapping("/slideshows")
    public ResponseEntity<Map<String, Object>> createSlideshow(@Valid @RequestBody AdminSlideshowRequest request) {
        return AdminApiResponses.entity(() -> slideshowService.createSlideshow(request), "Da tao slideshow.");
    }

    @PutMapping("/slideshows/{id}")
    public ResponseEntity<Map<String, Object>> updateSlideshow(@PathVariable Long id,
                                                               @Valid @RequestBody AdminSlideshowRequest request) {
        return AdminApiResponses.entity(() -> slideshowService.updateSlideshow(id, request), "Da cap nhat slideshow.");
    }

    @PatchMapping("/slideshows/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleSlideshowActive(@PathVariable Long id) {
        return AdminApiResponses.action(() -> slideshowService.toggleActive(id), "Da cap nhat trang thai slideshow.");
    }

    @DeleteMapping("/slideshows/{id}")
    public ResponseEntity<Map<String, Object>> deleteSlideshow(@PathVariable Long id) {
        return AdminApiResponses.action(() -> slideshowService.softDeleteSlideshow(id), "Da xoa mem slideshow.");
    }

    @PostMapping("/slideshows/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreSlideshow(@PathVariable Long id) {
        return AdminApiResponses.action(() -> slideshowService.restoreSlideshow(id), "Da khoi phuc slideshow.");
    }

    @DeleteMapping("/trash/slideshows/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteSlideshow(@PathVariable Long id) {
        return AdminApiResponses.action(() -> slideshowService.hardDeleteSlideshow(id), "Da xoa vinh vien slideshow.");
    }
}
