package com.english.learning.service.impl.mobile;

import com.english.learning.dto.mobile.MobileLessonDetailResponse;
import com.english.learning.dto.mobile.MobileSentenceResponse;
import com.english.learning.entity.Lesson;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.mapper.mobile.MobileResponseMapper;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.mobile.MobileLessonDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation: Lesson Detail Service.
 *
 * Returns lesson metadata + all published sentences for on-demand loading.
 * Only returns lessons that are published and not soft-deleted,
 * with section and category also published.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MobileLessonDetailServiceImpl implements MobileLessonDetailService {

    private final LessonRepository lessonRepository;
    private final SentenceRepository sentenceRepository;
    private final MobileResponseMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public MobileLessonDetailResponse getLessonDetail(Long lessonId) {
        log.info("Loading lesson detail for id={}", lessonId);

        // Find published lesson (also validates section + category are published)
        Lesson lesson = lessonRepository
                .findPublishedById(lessonId, ContentStatus.PUBLISHED)
                .orElse(null);

        if (lesson == null) {
            log.warn("Lesson id={} not found or not published", lessonId);
            return null;
        }

        // Load published sentences for this lesson
        List<MobileSentenceResponse> sentences = sentenceRepository
                .findByLesson_IdAndStatusOrderByOrderIndexAsc(lessonId, ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileSentence)
                .collect(Collectors.toList());

        // Derive contentType
        String contentType = deriveContentType(lesson);
        String displayType = "VIDEO".equals(contentType) ? "Video" : "Audio";

        log.info("Lesson id={}: {} sentences loaded", lessonId, sentences.size());

        return MobileLessonDetailResponse.builder()
                .id(lesson.getId())
                .sectionId(lesson.getSection() != null ? lesson.getSection().getId() : null)
                .title(lesson.getTitle())
                .level(lesson.getLevel())
                .displayType(displayType)
                .contentType(contentType)
                .totalSentences(lesson.getTotalSentences())
                .passThreshold(lesson.getPassThreshold() != null ? lesson.getPassThreshold() : 70)
                .youtubeVideoId(lesson.getYoutubeVideoId())
                .orderIndex(lesson.getOrderIndex())
                .sentences(sentences)
                .build();
    }

    private String deriveContentType(Lesson lesson) {
        if (lesson.getContentType() != null) {
            return lesson.getContentType().name();
        }
        if (lesson.getSection() != null && lesson.getSection().getCategory() != null) {
            LessonType catType = lesson.getSection().getCategory().getType();
            if (catType != null) {
                return catType.name();
            }
        }
        return "AUDIO";
    }
}
