package com.english.learning.controller.api.admin;

import com.english.learning.dto.AdminCategoryRequest;
import com.english.learning.dto.AdminLessonRequest;
import com.english.learning.dto.AdminSectionRequest;
import com.english.learning.dto.AdminSentenceRequest;
import com.english.learning.service.content.category.CategoryService;
import com.english.learning.service.content.lesson.LessonService;
import com.english.learning.service.content.section.SectionService;
import com.english.learning.service.content.sentence.SentenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class AdminContentApiController {

    private final CategoryService categoryService;
    private final SectionService sectionService;
    private final LessonService lessonService;
    private final SentenceService sentenceService;

    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody AdminCategoryRequest request) {
        return AdminApiResponses.entity(() -> categoryService.createCategory(request), "Da tao category.");
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id,
                                                              @Valid @RequestBody AdminCategoryRequest request) {
        return AdminApiResponses.entity(() -> categoryService.updateCategory(id, request), "Da cap nhat category.");
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        return AdminApiResponses.action(() -> categoryService.deleteCategory(id), "Da xoa mem category.");
    }

    @PostMapping("/categories/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreCategory(@PathVariable Long id) {
        return AdminApiResponses.action(() -> categoryService.restoreCategory(id), "Da khoi phuc category.");
    }

    @DeleteMapping("/trash/categories/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteCategory(@PathVariable Long id) {
        return AdminApiResponses.action(() -> categoryService.hardDeleteCategory(id), "Da xoa vinh vien category.");
    }

    @PostMapping("/sections")
    public ResponseEntity<Map<String, Object>> createSection(@Valid @RequestBody AdminSectionRequest request) {
        return AdminApiResponses.entity(() -> sectionService.createSection(request), "Da tao section.");
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<Map<String, Object>> updateSection(@PathVariable Long id,
                                                             @Valid @RequestBody AdminSectionRequest request) {
        return AdminApiResponses.entity(() -> sectionService.updateSection(id, request), "Da cap nhat section.");
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteSection(@PathVariable Long id) {
        return AdminApiResponses.action(() -> sectionService.deleteSection(id), "Da xoa mem section.");
    }

    @PostMapping("/sections/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreSection(@PathVariable Long id) {
        return AdminApiResponses.action(() -> sectionService.restoreSection(id), "Da khoi phuc section.");
    }

    @DeleteMapping("/trash/sections/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteSection(@PathVariable Long id) {
        return AdminApiResponses.action(() -> sectionService.hardDeleteSection(id), "Da xoa vinh vien section.");
    }

    @PostMapping("/lessons")
    public ResponseEntity<Map<String, Object>> createLesson(@Valid @RequestBody AdminLessonRequest request) {
        return AdminApiResponses.entity(() -> lessonService.createLesson(request), "Da tao lesson.");
    }

    @PutMapping("/lessons/{id}")
    public ResponseEntity<Map<String, Object>> updateLesson(@PathVariable Long id,
                                                            @Valid @RequestBody AdminLessonRequest request) {
        return AdminApiResponses.entity(() -> lessonService.updateLesson(id, request), "Da cap nhat lesson.");
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteLesson(@PathVariable Long id) {
        return AdminApiResponses.action(() -> lessonService.deleteLesson(id), "Da xoa mem lesson.");
    }

    @PostMapping("/lessons/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreLesson(@PathVariable Long id) {
        return AdminApiResponses.action(() -> lessonService.restoreLesson(id), "Da khoi phuc lesson.");
    }

    @DeleteMapping("/trash/lessons/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteLesson(@PathVariable Long id) {
        return AdminApiResponses.action(() -> lessonService.hardDeleteLesson(id), "Da xoa vinh vien lesson.");
    }

    @PostMapping("/sentences")
    public ResponseEntity<Map<String, Object>> createSentence(@Valid @RequestBody AdminSentenceRequest request) {
        return AdminApiResponses.entity(() -> sentenceService.createSentence(request), "Da tao sentence.");
    }

    @PutMapping("/sentences/{id}")
    public ResponseEntity<Map<String, Object>> updateSentence(@PathVariable Long id,
                                                              @Valid @RequestBody AdminSentenceRequest request) {
        return AdminApiResponses.entity(() -> sentenceService.updateSentence(id, request), "Da cap nhat sentence.");
    }

    @DeleteMapping("/sentences/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteSentence(@PathVariable Long id) {
        return AdminApiResponses.action(() -> sentenceService.softDeleteSentence(id), "Da xoa mem sentence.");
    }

    @PostMapping("/sentences/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreSentence(@PathVariable Long id) {
        return AdminApiResponses.action(() -> sentenceService.restoreSentence(id), "Da khoi phuc sentence.");
    }

    @DeleteMapping("/trash/sentences/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteSentence(@PathVariable Long id) {
        return AdminApiResponses.action(() -> sentenceService.hardDeleteSentence(id), "Da xoa vinh vien sentence.");
    }
}
