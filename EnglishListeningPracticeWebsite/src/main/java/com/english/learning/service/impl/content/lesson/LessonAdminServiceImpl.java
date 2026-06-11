package com.english.learning.service.impl.content.lesson;

import com.english.learning.dto.AdminLessonRequest;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.exception.ResourceInUseException;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.content.lesson.LessonAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LessonAdminServiceImpl implements LessonAdminService {

    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(
            "^(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([A-Za-z0-9_-]{11}).*$",
            Pattern.CASE_INSENSITIVE
    );

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final SentenceRepository sentenceRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Lesson createLesson(AdminLessonRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Lesson lesson = new Lesson();
        applyLessonRequest(lesson, request);
        Lesson savedLesson = lessonRepository.save(lesson);
        syncCategoryLessonCount(savedLesson.getSection());
        return savedLesson;
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long id, AdminLessonRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson khong ton tai."));
        Long oldCategoryId = lesson.getSection() != null && lesson.getSection().getCategory() != null
                ? lesson.getSection().getCategory().getId()
                : null;
        validateLessonStatusTransition(lesson.getId(), request.getStatus());
        assertLessonChanged(lesson, request);
        applyLessonRequest(lesson, request);
        Lesson savedLesson = lessonRepository.save(lesson);
        syncCategoryLessonCount(oldCategoryId);
        Long newCategoryId = savedLesson.getSection() != null && savedLesson.getSection().getCategory() != null
                ? savedLesson.getSection().getCategory().getId()
                : null;
        if (newCategoryId != null && !newCategoryId.equals(oldCategoryId)) {
            syncCategoryLessonCount(newCategoryId);
        }
        return savedLesson;
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson khong ton tai."));
        if (sentenceRepository.countByLesson_Id(id) > 0) {
            throw new ResourceInUseException("Khong the xoa Lesson khi van con Sentence ben duoi.");
        }
        lesson.setIsDeleted(true);
        lesson.setStatus(ContentStatus.ARCHIVED);
        lessonRepository.save(lesson);
        syncCategoryLessonCount(lesson.getSection());
    }

    @Override
    @Transactional
    public void restoreLesson(Long id) {
        Lesson lesson = lessonRepository.findAnyLessonById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson khong ton tai."));
        Long sectionId = lesson.getSection() != null ? lesson.getSection().getId() : null;
        Section section = sectionId != null ? sectionRepository.findAnySectionById(sectionId).orElse(null) : null;
        if (section == null || Boolean.TRUE.equals(section.getIsDeleted())) {
            throw new ResourceNotFoundException("Khong the khoi phuc Lesson khi Section cha dang bi xoa.");
        }
        if (section.getStatus() == ContentStatus.ARCHIVED) {
            throw new ResourceInUseException("Khong the khoi phuc Lesson khi Section cha dang o trang thai ARCHIVED. Hay khoi phuc Section truoc.");
        }
        if (section.getCategory() == null || Boolean.TRUE.equals(section.getCategory().getIsDeleted())) {
            throw new ResourceInUseException("Khong the khoi phuc Lesson khi Category cha dang bi xoa.");
        }
        if (section.getCategory().getStatus() == ContentStatus.ARCHIVED) {
            throw new ResourceInUseException("Khong the khoi phuc Lesson khi Category cha dang o trang thai ARCHIVED. Hay khoi phuc Category truoc.");
        }
        lesson.setSection(section);
        lesson.setIsDeleted(false);
        lesson.setStatus(ContentStatus.DRAFT);
        lessonRepository.save(lesson);
        syncCategoryLessonCount(lesson.getSection());
    }

    @Override
    @Transactional
    public void hardDeleteLesson(Long id) {
        Lesson lesson = lessonRepository.findAnyLessonById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson khong ton tai."));
        if (sentenceRepository.countAnyByLessonId(id) > 0) {
            throw new ResourceInUseException("Khong the xoa vinh vien Lesson khi van con Sentence ben duoi.");
        }
        Section section = lesson.getSection();
        lessonRepository.deleteById(id);
        syncCategoryLessonCount(section);
    }

    private void applyLessonRequest(Lesson lesson, AdminLessonRequest request) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section khong ton tai."));
        String normalizedYoutubeValue = normalizeYoutubeReference(section, request.getYoutubeVideoId());
        lesson.setSection(section);
        lesson.setTitle(request.getTitle().trim());
        lesson.setYoutubeVideoId(normalizedYoutubeValue);
        lesson.setFolderName(normalizeBlank(request.getFolderName()));
        lesson.setLevel(normalizeBlank(request.getLevel()));
        lesson.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        lesson.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        if (lesson.getTotalSentences() == null) {
            lesson.setTotalSentences(0);
        }
    }

    private void validateStatusForAdminMutation(ContentStatus status) {
        if (status == ContentStatus.ARCHIVED) {
            throw new IllegalArgumentException("ARCHIVED chi duoc dung noi bo cho thung rac.");
        }
    }

    private void validateLessonStatusTransition(Long lessonId, ContentStatus nextStatus) {
        if (nextStatus != ContentStatus.DRAFT) {
            return;
        }
        if (sentenceRepository.countByLesson_IdAndStatus(lessonId, ContentStatus.PUBLISHED) > 0) {
            throw new ResourceInUseException("Khong the chuyen Lesson ve DRAFT khi van con Sentence PUBLISHED.");
        }
    }

    private void assertLessonChanged(Lesson lesson, AdminLessonRequest request) {
        Section targetSection = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section khong ton tai."));
        String normalizedYoutubeValue = normalizeYoutubeReference(targetSection, request.getYoutubeVideoId());
        boolean unchanged = Objects.equals(lesson.getSection() != null ? lesson.getSection().getId() : null, request.getSectionId())
                && Objects.equals(lesson.getTitle(), request.getTitle().trim())
                && Objects.equals(lesson.getYoutubeVideoId(), normalizedYoutubeValue)
                && Objects.equals(lesson.getFolderName(), normalizeBlank(request.getFolderName()))
                && Objects.equals(lesson.getLevel(), normalizeBlank(request.getLevel()))
                && Objects.equals(lesson.getStatus(), request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT)
                && Objects.equals(lesson.getOrderIndex(), request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        if (unchanged) {
            throw new IllegalArgumentException("Du lieu chua thay doi.");
        }
    }

    private String normalizeYoutubeReference(Section section, String rawValue) {
        String normalized = normalizeBlank(rawValue);
        LessonType lessonType = section.getCategory() != null ? section.getCategory().getType() : LessonType.AUDIO;
        if (lessonType != LessonType.VIDEO) {
            return null;
        }
        if (normalized == null) {
            throw new IllegalArgumentException("Link YouTube khong duoc de trong voi bai hoc video.");
        }
        if (YOUTUBE_ID_PATTERN.matcher(normalized).matches()) {
            return normalized;
        }
        Matcher matcher = YOUTUBE_URL_PATTERN.matcher(normalized);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Link YouTube khong hop le.");
    }

    private void syncCategoryLessonCount(Section section) {
        if (section == null || section.getCategory() == null) {
            return;
        }
        syncCategoryLessonCount(section.getCategory().getId());
    }

    private void syncCategoryLessonCount(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        categoryRepository.findAnyCategoryById(categoryId).ifPresent(category -> {
            category.setTotalLessons((int) lessonRepository.countActiveBySection_Category_Id(categoryId));
            categoryRepository.save(category);
        });
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

