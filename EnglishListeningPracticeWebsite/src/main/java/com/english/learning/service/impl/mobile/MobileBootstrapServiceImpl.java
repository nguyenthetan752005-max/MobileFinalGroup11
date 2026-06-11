package com.english.learning.service.impl.mobile;

import com.english.learning.dto.mobile.*;
import com.english.learning.enums.ContentStatus;
import com.english.learning.mapper.mobile.MobileLeaderboardMapper;
import com.english.learning.mapper.mobile.MobileResponseMapper;
import com.english.learning.repository.*;
import com.english.learning.service.mobile.MobileBootstrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation: Mobile Bootstrap Service.
 *
 * Aggregates all published content (categories, sections, lessons,
 * sentences, comments) + leaderboard data into a single bootstrap
 * response for Android app initial sync.
 *
 * Re-uses existing repositories from the web project.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MobileBootstrapServiceImpl implements MobileBootstrapService {

    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final SentenceRepository sentenceRepository;
    private final CommentRepository commentRepository;
    private final MobileResponseMapper mapper;
    private final MobileLeaderboardMapper leaderboardMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable("bootstrap")
    public MobileBootstrapResponse getBootstrapData() {
        log.info("Building mobile bootstrap response...");

        // 1. Published categories ordered by orderIndex
        List<MobileCategoryResponse> categories = categoryRepository
                .findByStatusOrderByOrderIndexAscIdAsc(ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileCategory)
                .collect(Collectors.toList());

        // 2. Published sections ordered by orderIndex
        List<MobileSectionResponse> sections = sectionRepository
                .findByStatusOrderByOrderIndexAscIdAsc(ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileSection)
                .collect(Collectors.toList());

        // 3. Published lessons ordered by orderIndex
        List<MobileLessonResponse> allLessons = lessonRepository
                .findByStatusOrderByOrderIndexAscIdAsc(ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileLesson)
                .collect(Collectors.toList());

        // --- REMOVED: FILTER LESSONS (Now returns all lessons without limits) ---
        List<MobileLessonResponse> lessons = allLessons;

        Set<Long> retainedLessonIds = lessons.stream().map(MobileLessonResponse::getId).collect(Collectors.toSet());

        // 4. Published sentences ordered by orderIndex
        List<MobileSentenceResponse> sentences = sentenceRepository
                .findByStatusOrderByOrderIndexAscIdAsc(ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileSentence)
                .filter(s -> retainedLessonIds.contains(s.getLessonId()))
                .collect(Collectors.toList());

        // 5. Visible comments (not deleted, not hidden)
        List<MobileCommentResponse> comments = commentRepository
                .findVisibleComments()
                .stream()
                .map(mapper::toMobileComment)
                .collect(Collectors.toList());

        // 6. Leaderboard
        MobileLeaderboardResponse leaderboard = leaderboardMapper.buildLeaderboard(null);

        log.info("Bootstrap: {} categories, {} sections, {} lessons, {} sentences, {} comments",
                categories.size(), sections.size(), lessons.size(), sentences.size(), comments.size());

        return MobileBootstrapResponse.builder()
                .version("v1.1")
                .generatedAt(Instant.now().toString())
                .categories(categories)
                .sections(sections)
                .lessons(lessons)
                .sentences(sentences)
                .comments(comments)
                .leaderboard(leaderboard)
                .build();
    }
}
