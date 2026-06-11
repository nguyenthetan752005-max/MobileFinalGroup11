package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileBootstrapLiteResponse;
import com.english.learning.dto.mobile.MobileBootstrapResponse;
import com.english.learning.dto.mobile.MobileLessonDetailResponse;
import com.english.learning.dto.mobile.MobileReminderSettingsResponse;
import com.english.learning.service.settings.AppSettingService;
import com.english.learning.service.mobile.MobileBootstrapLiteService;
import com.english.learning.service.mobile.MobileBootstrapService;
import com.english.learning.service.mobile.MobileLessonDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

/**
 * REST Controller: Mobile Content API.
 * Provides JSON endpoints for Android app consumption.
 * All responses use camelCase field naming (Jackson default).
 */
@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileContentApiController {

    private static final DateTimeFormatter REMINDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final MobileBootstrapService mobileBootstrapService;
    private final MobileBootstrapLiteService mobileBootstrapLiteService;
    private final MobileLessonDetailService mobileLessonDetailService;
    private final com.english.learning.service.mobile.MobileCategoryService mobileCategoryService;
    private final AppSettingService appSettingService;

    /**
     * GET /api/mobile/bootstrap
     *
     * Returns all published content for Android app initial sync (v1 - heavy).
     * Kept for backward compatibility.
     */
    @GetMapping("/bootstrap")
    public ResponseEntity<MobileBootstrapResponse> getBootstrap() {
        MobileBootstrapResponse response = mobileBootstrapService.getBootstrapData();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/mobile/catalog/bootstrap-lite
     *
     * Returns catalog data WITHOUT sentences for lighter initial sync (v2).
     * Android should use this endpoint instead of /bootstrap.
     */
    @GetMapping("/catalog/bootstrap-lite")
    public ResponseEntity<MobileBootstrapLiteResponse> getBootstrapLite() {
        MobileBootstrapLiteResponse response = mobileBootstrapLiteService.getBootstrapLiteData();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/mobile/lessons/{id}
     *
     * Returns lesson metadata + all published sentences for a specific lesson.
     * Android calls this when user opens a lesson.
     */
    @GetMapping("/lessons/{id}")
    public ResponseEntity<MobileLessonDetailResponse> getLessonDetail(@PathVariable Long id) {
        MobileLessonDetailResponse response = mobileLessonDetailService.getLessonDetail(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/mobile/categories
     *
     * Returns a lightweight list of all published categories.
     * Android uses this for the Explore screen.
     */
    @GetMapping("/categories")
    public ResponseEntity<java.util.List<com.english.learning.dto.mobile.MobileCategoryResponse>> getCategories() {
        return ResponseEntity.ok(mobileCategoryService.getAllCategories());
    }

    /**
     * GET /api/mobile/categories/{categorySlug}/sections
     *
     * Returns category metadata, along with its sections and lessons.
     * Android uses this when opening a specific category.
     */
    @GetMapping("/categories/{categorySlug}/sections")
    public ResponseEntity<com.english.learning.dto.mobile.MobileCategoryCollectionResponse> getCategorySections(@PathVariable String categorySlug) {
        com.english.learning.dto.mobile.MobileCategoryCollectionResponse response = mobileCategoryService.getCategoryWithSections(categorySlug);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/mobile/sections/{sectionId}/lessons
     *
     * Returns a list of lessons for a specific section.
     * Android calls this when dynamically expanding a section.
     */
    @GetMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<java.util.List<com.english.learning.dto.mobile.MobileLessonResponse>> getSectionLessons(@PathVariable Long sectionId) {
        java.util.List<com.english.learning.dto.mobile.MobileLessonResponse> lessons = mobileCategoryService.getLessonsBySection(sectionId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * GET /api/mobile/app-settings/reminder
     *
     * Returns the public daily reminder schedule used by the Android app.
     * This endpoint must stay public so guest users can also receive app-level reminders.
     */
    @GetMapping("/app-settings/reminder")
    public ResponseEntity<MobileReminderSettingsResponse> getReminderSettings() {
        MobileReminderSettingsResponse response = MobileReminderSettingsResponse.builder()
                .dailyReminderEnabled(appSettingService.isDailyReminderEnabled())
                .dailyReminderTime(appSettingService.getDailyReminderTime().format(REMINDER_TIME_FORMATTER))
                .dailyReminderTimezone(appSettingService.getDailyReminderTimezone())
                .build();
        return ResponseEntity.ok(response);
    }
}
