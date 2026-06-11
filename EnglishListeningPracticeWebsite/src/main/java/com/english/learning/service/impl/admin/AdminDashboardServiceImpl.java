package com.english.learning.service.impl.admin;

import com.english.learning.dto.AdminDashboardDTO;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.CommentRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.SlideshowRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.admin.AdminDashboardService;
import com.english.learning.service.content.category.CategoryService;
import com.english.learning.service.settings.AppSettingService;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Implementation of AdminDashboardService.
 * Aggregates all data needed for the admin dashboard, including
 * separate lists for regular users vs admin accounts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final SentenceRepository sentenceRepository;
    private final CommentRepository commentRepository;
    private final SlideshowRepository slideshowRepository;
    private final SpeakingResultRepository speakingResultRepository;
    private final CategoryService categoryService;
    private final AppSettingService appSettingService;

    @Override
    public AdminDashboardDTO getDashboardData() {
        AdminDashboardDTO.AdminDashboardDTOBuilder builder = AdminDashboardDTO.builder();

        // Overview Stats
        try {
            builder.totalUsers(userRepository.count());
            builder.totalAdmins(userRepository.countAdmins());
            builder.totalLessons(lessonRepository.count());
            Long totalTime = userRepository.sumTotalActiveTime();
            if (totalTime == null) totalTime = 0L;
            builder.formattedTotalTime(TimeFormatUtil.formatActiveTime(totalTime.intValue()));
        } catch (Exception e) {
            log.error("Error fetching overview stats: {}", e.getMessage());
            builder.totalUsers(0).totalAdmins(0).totalLessons(0).formattedTotalTime("0 min");
        }

        // Recent Users (overview)
        try {
            builder.recentUsers(
                    userRepository.findByIsDeletedFalseOrderByCreatedAtDesc(
                            PageRequest.of(0, appSettingService.getMaxRecentUsersOnDashboard())
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching recent users: {}", e.getMessage());
            builder.recentUsers(Collections.emptyList());
        }

        // Regular Users (role=USER)
        try {
            builder.regularUsers(userRepository.findAllRegularUsers());
        } catch (Exception e) {
            log.error("Error fetching regular users: {}", e.getMessage());
            builder.regularUsers(Collections.emptyList());
        }

        // User top score map (for sorting/filtering on admin UI)
        try {
            Map<Long, Integer> topScoreMap = speakingResultRepository.findAll().stream()
                    .filter(sr -> sr.getUser() != null && sr.getUser().getId() != null)
                    .filter(sr -> sr.getScore() != null)
                    .collect(Collectors.toMap(
                            sr -> sr.getUser().getId(),
                            SpeakingResult::getScore,
                            Math::max
                    ));
            builder.userTopScoreMap(topScoreMap);
        } catch (Exception e) {
            log.error("Error fetching user top score map: {}", e.getMessage());
            builder.userTopScoreMap(Collections.emptyMap());
        }

        // Content hierarchy: Categories only (Sections/Lessons/Sentences loaded via API on demand)
        try {
            builder.categories(categoryRepository.findAllByOrderByOrderIndexAscIdAsc());
        } catch (Exception e) {
            log.error("Error fetching categories: {}", e.getMessage());
            builder.categories(Collections.emptyList());
        }

        try {
            builder.categoryLevelsMap(builder.build().getCategories().stream()
                    .collect(Collectors.toMap(
                            com.english.learning.entity.Category::getId,
                            category -> String.join(",", categoryService.getExpandedLevels(category.getLevelRange()))
                    )));
            builder.categoryLevelOptions(builder.build().getCategories().stream()
                    .flatMap(category -> categoryService.getExpandedLevels(category.getLevelRange()).stream())
                    .map(String::trim)
                    .filter(level -> !level.isEmpty())
                    .collect(Collectors.toCollection(TreeSet::new)));
        } catch (Exception e) {
            log.error("Error building category level metadata: {}", e.getMessage());
            builder.categoryLevelsMap(Collections.emptyMap());
            builder.categoryLevelOptions(Set.of());
        }

        try {
            builder.allLessons(lessonRepository.findAll(Sort.by(Sort.Direction.ASC, "orderIndex").and(Sort.by(Sort.Direction.ASC, "id"))));
        } catch (Exception e) {
            log.error("Error fetching lessons: {}", e.getMessage());
            builder.allLessons(Collections.emptyList());
        }

        try {
            builder.lessonLevelOptions(builder.build().getAllLessons().stream()
                    .map(com.english.learning.entity.Lesson::getLevel)
                    .filter(java.util.Objects::nonNull)
                    .map(String::trim)
                    .filter(level -> !level.isEmpty())
                    .collect(Collectors.toCollection(TreeSet::new)));
        } catch (Exception e) {
            log.error("Error building lesson level options: {}", e.getMessage());
            builder.lessonLevelOptions(Set.of());
        }

        // Recycle Bin
        try {
            builder.deletedUsers(userRepository.findDeletedUsers());
        } catch (Exception e) {
            log.error("Error fetching deleted users: {}", e.getMessage());
            builder.deletedUsers(Collections.emptyList());
        }

        try {
            builder.deletedCategories(categoryRepository.findDeletedCategories());
        } catch (Exception e) {
            log.error("Error fetching deleted categories: {}", e.getMessage());
            builder.deletedCategories(Collections.emptyList());
        }

        try {
            builder.deletedSections(sectionRepository.findDeletedSections());
        } catch (Exception e) {
            log.error("Error fetching deleted sections: {}", e.getMessage());
            builder.deletedSections(Collections.emptyList());
        }

        try {
            builder.deletedLessons(lessonRepository.findDeletedLessons());
        } catch (Exception e) {
            log.error("Error fetching deleted lessons: {}", e.getMessage());
            builder.deletedLessons(Collections.emptyList());
        }

        try {
            builder.deletedSentences(sentenceRepository.findDeletedSentences());
        } catch (Exception e) {
            log.error("Error fetching deleted sentences: {}", e.getMessage());
            builder.deletedSentences(Collections.emptyList());
        }

        try {
            builder.deletedComments(commentRepository.findDeletedComments());
        } catch (Exception e) {
            log.error("Error fetching deleted comments: {}", e.getMessage());
            builder.deletedComments(Collections.emptyList());
        }

        try {
            builder.deletedSlideshows(slideshowRepository.findDeletedSlideshows());
        } catch (Exception e) {
            log.error("Error fetching deleted slideshows: {}", e.getMessage());
            builder.deletedSlideshows(Collections.emptyList());
        }

        // Recent Comments (for moderation)
        try {
            builder.recentComments(
                    commentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                            .stream()
                            .limit(100)
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            log.error("Error fetching comments: {}", e.getMessage());
            builder.recentComments(Collections.emptyList());
        }

        // Slideshows (all positions, ordered by displayOrder)
        try {
            builder.slideshows(slideshowRepository.findAllByOrderByDisplayOrderAscIdAsc());
        } catch (Exception e) {
            log.error("Error fetching slideshows: {}", e.getMessage());
            builder.slideshows(Collections.emptyList());
        }

        return builder.build();
    }
}

