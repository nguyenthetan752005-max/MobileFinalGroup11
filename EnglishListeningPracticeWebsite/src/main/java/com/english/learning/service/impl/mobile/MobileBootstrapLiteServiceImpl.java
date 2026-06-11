package com.english.learning.service.impl.mobile;

import com.english.learning.dto.mobile.*;
import com.english.learning.enums.ContentStatus;
import com.english.learning.mapper.mobile.MobileLeaderboardMapper;
import com.english.learning.mapper.mobile.MobileResponseMapper;
import com.english.learning.repository.*;
import com.english.learning.service.mobile.MobileBootstrapLiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation: Bootstrap Lite Service.
 *
 * Aggregates published categories, sections, lessons, comments + leaderboard.
 * Does NOT include sentences — Android will load them per-lesson via /api/mobile/lessons/{id}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MobileBootstrapLiteServiceImpl implements MobileBootstrapLiteService {

    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final CommentRepository commentRepository;
    private final MobileResponseMapper mapper;
    private final MobileLeaderboardMapper leaderboardMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable("bootstrap-lite")
    public MobileBootstrapLiteResponse getBootstrapLiteData() {
        log.info("Building mobile bootstrap-lite response (no sentences)...");

        // 1. Published categories (not deleted, status=PUBLISHED) ordered by orderIndex
        List<MobileCategoryResponse> categories = categoryRepository
                .findPublishedCategories(ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileCategory)
                .collect(Collectors.toList());

        // 2. Published sections (not deleted, category not deleted) ordered by orderIndex
        List<MobileSectionResponse> sections = sectionRepository
                .findPublishedSections(ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileSection)
                .collect(Collectors.toList());

        // 3. Published lessons (not deleted, section/category not deleted) ordered by orderIndex
        List<MobileLessonResponse> allLessons = lessonRepository
                .findPublishedLessons(ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileLesson)
                .collect(Collectors.toList());

        // --- REMOVED: FILTER LESSONS (Now returns all lessons without limits) ---
        List<MobileLessonResponse> lessons = allLessons;

        // 4. Visible comments (not deleted, not hidden)
        List<MobileCommentResponse> comments = commentRepository
                .findVisibleComments()
                .stream()
                .map(mapper::toMobileComment)
                .collect(Collectors.toList());

        // 5. Leaderboard
        MobileLeaderboardResponse leaderboard = leaderboardMapper.buildLeaderboard(null);

        log.info("Bootstrap-lite: {} categories, {} sections, {} lessons, {} comments (no sentences)",
                categories.size(), sections.size(), lessons.size(), comments.size());

        return MobileBootstrapLiteResponse.builder()
                .version("v2.1")
                .generatedAt(Instant.now().toString())
                .categories(categories)
                .sections(sections)
                .lessons(lessons)
                .comments(comments)
                .leaderboard(leaderboard)
                .build();
    }
}
