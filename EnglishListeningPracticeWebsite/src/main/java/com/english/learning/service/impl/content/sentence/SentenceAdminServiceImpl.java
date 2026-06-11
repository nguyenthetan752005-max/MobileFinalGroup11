package com.english.learning.service.impl.content.sentence;

import com.english.learning.dto.AdminSentenceRequest;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Sentence;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.exception.ResourceInUseException;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.CommentRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.service.integration.media.MediaStorageGateway;
import com.english.learning.service.content.sentence.SentenceAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SentenceAdminServiceImpl implements SentenceAdminService {

    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;
    private final SpeakingResultRepository speakingResultRepository;
    private final UserProgressRepository userProgressRepository;
    private final CommentRepository commentRepository;
    private final MediaStorageGateway mediaStorageGateway;

    @Override
    @Transactional
    public Sentence createSentence(AdminSentenceRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Sentence sentence = new Sentence();
        applySentenceRequest(sentence, request);
        Sentence savedSentence = sentenceRepository.save(sentence);
        syncLessonSentenceCount(savedSentence.getLesson().getId());
        return savedSentence;
    }

    @Override
    @Transactional
    public Sentence updateSentence(Long id, AdminSentenceRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Sentence sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence khong ton tai."));
        Long oldLessonId = sentence.getLesson() != null ? sentence.getLesson().getId() : null;
        assertSentenceChanged(sentence, request);
        applySentenceRequest(sentence, request);
        Sentence savedSentence = sentenceRepository.save(sentence);
        syncLessonSentenceCount(oldLessonId);
        if (savedSentence.getLesson() != null && !savedSentence.getLesson().getId().equals(oldLessonId)) {
            syncLessonSentenceCount(savedSentence.getLesson().getId());
        }
        return savedSentence;
    }

    @Override
    @Transactional
    public void softDeleteSentence(Long id) {
        Sentence sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence khong ton tai."));
        sentence.setIsDeleted(true);
        sentence.setStatus(ContentStatus.ARCHIVED);
        sentenceRepository.save(sentence);
        if (sentence.getLesson() != null) {
            syncLessonSentenceCount(sentence.getLesson().getId());
        }
    }

    @Override
    @Transactional
    public void restoreSentence(Long id) {
        Sentence sentence = sentenceRepository.findAnySentenceById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence khong ton tai."));
        Long lessonId = sentence.getLesson() != null ? sentence.getLesson().getId() : null;
        Lesson lesson = lessonId != null ? lessonRepository.findAnyLessonById(lessonId).orElse(null) : null;
        if (lesson == null || Boolean.TRUE.equals(lesson.getIsDeleted())) {
            throw new ResourceNotFoundException("Khong the khoi phuc Sentence khi Lesson cha dang bi xoa.");
        }
        if (lesson.getStatus() == ContentStatus.ARCHIVED) {
            throw new ResourceInUseException("Khong the khoi phuc Sentence khi Lesson cha dang o trang thai ARCHIVED. Hay khoi phuc Lesson truoc.");
        }
        if (lesson.getSection() == null || Boolean.TRUE.equals(lesson.getSection().getIsDeleted())) {
            throw new ResourceInUseException("Khong the khoi phuc Sentence khi Section cha dang bi xoa.");
        }
        if (lesson.getSection().getStatus() == ContentStatus.ARCHIVED) {
            throw new ResourceInUseException("Khong the khoi phuc Sentence khi Section cha dang o trang thai ARCHIVED. Hay khoi phuc Section truoc.");
        }
        if (lesson.getSection().getCategory() == null || Boolean.TRUE.equals(lesson.getSection().getCategory().getIsDeleted())) {
            throw new ResourceInUseException("Khong the khoi phuc Sentence khi Category cha dang bi xoa.");
        }
        if (lesson.getSection().getCategory().getStatus() == ContentStatus.ARCHIVED) {
            throw new ResourceInUseException("Khong the khoi phuc Sentence khi Category cha dang o trang thai ARCHIVED. Hay khoi phuc Category truoc.");
        }
        sentence.setLesson(lesson);
        sentence.setIsDeleted(false);
        sentence.setStatus(ContentStatus.DRAFT);
        sentenceRepository.save(sentence);
        if (sentence.getLesson() != null) {
            syncLessonSentenceCount(sentence.getLesson().getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteSentence(Long id) throws Exception {
        Sentence sentence = sentenceRepository.findAnySentenceById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence khong ton tai."));

        if (speakingResultRepository.countBySentence_Id(id) > 0 || userProgressRepository.countBySentence_Id(id) > 0) {
            throw new ResourceInUseException(
                    "Khong the xoa cung Sentence khi van con ket qua speaking hoac tien do hoc tap lien quan."
            );
        }
        if (commentRepository.countAnyBySentenceId(id) > 0) {
            throw new ResourceInUseException(
                    "Khong the xoa cung Sentence khi van con Comment lien quan. Hay xoa cung comment truoc."
            );
        }

        Long lessonId = sentence.getLesson() != null ? sentence.getLesson().getId() : null;
        if (sentence.getCloudAudioId() != null && !sentence.getCloudAudioId().isEmpty()) {
            mediaStorageGateway.deleteFile(sentence.getCloudAudioId());
        }

        sentenceRepository.deleteById(id);
        syncLessonSentenceCount(lessonId);
    }

    private void applySentenceRequest(Sentence sentence, AdminSentenceRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson khong ton tai."));
        validateLessonMedia(lesson, request);
        String fileName = normalizeFileNameForLesson(lesson, request.getFileName());
        String cloudAudioId = normalizeCloudAudioIdForLesson(lesson, request.getCloudAudioId());
        Double startTime = normalizeStartTimeForLesson(lesson, request.getStartTime());
        Double endTime = normalizeEndTimeForLesson(lesson, request.getEndTime());
        replaceStoredAudio(sentence.getCloudAudioId(), cloudAudioId);
        sentence.setLesson(lesson);
        sentence.setContent(request.getContent().trim());
        sentence.setFileName(fileName);
        sentence.setCloudAudioId(cloudAudioId);
        sentence.setStartTime(startTime);
        sentence.setEndTime(endTime);
        sentence.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        sentence.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
    }

    private void validateStatusForAdminMutation(ContentStatus status) {
        if (status == ContentStatus.ARCHIVED) {
            throw new IllegalArgumentException("ARCHIVED chi duoc dung noi bo cho thung rac.");
        }
    }

    private void assertSentenceChanged(Sentence sentence, AdminSentenceRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson khong ton tai."));
        validateLessonMedia(lesson, request);
        boolean unchanged = Objects.equals(sentence.getLesson() != null ? sentence.getLesson().getId() : null, request.getLessonId())
                && Objects.equals(sentence.getContent(), request.getContent().trim())
                && Objects.equals(sentence.getFileName(), normalizeFileNameForLesson(lesson, request.getFileName()))
                && Objects.equals(sentence.getCloudAudioId(), normalizeCloudAudioIdForLesson(lesson, request.getCloudAudioId()))
                && Objects.equals(sentence.getStartTime(), normalizeStartTimeForLesson(lesson, request.getStartTime()))
                && Objects.equals(sentence.getEndTime(), normalizeEndTimeForLesson(lesson, request.getEndTime()))
                && Objects.equals(sentence.getOrderIndex(), request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                && Objects.equals(sentence.getStatus(), request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        if (unchanged) {
            throw new IllegalArgumentException("Du lieu chua thay doi.");
        }
    }

    private void validateLessonMedia(Lesson lesson, AdminSentenceRequest request) {
        LessonType lessonType = resolveLessonType(lesson);
        if (lessonType == LessonType.VIDEO) {
            String youtubeVideoId = normalizeBlank(lesson.getYoutubeVideoId());
            if (youtubeVideoId == null || youtubeVideoId.length() != 11) {
                throw new IllegalArgumentException("Lesson video dang co link YouTube khong hop le.");
            }
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new IllegalArgumentException("Sentence video phai co start time va end time.");
            }
            if (request.getEndTime() <= request.getStartTime()) {
                throw new IllegalArgumentException("End time phai lon hon start time.");
            }
            return;
        }
        if (normalizeBlank(request.getFileName()) == null) {
            throw new IllegalArgumentException("Sentence audio phai co file name hoac audio upload.");
        }
    }

    private String normalizeFileNameForLesson(Lesson lesson, String fileName) {
        return resolveLessonType(lesson) == LessonType.AUDIO ? normalizeBlank(fileName) : null;
    }

    private String normalizeCloudAudioIdForLesson(Lesson lesson, String cloudAudioId) {
        return resolveLessonType(lesson) == LessonType.AUDIO ? normalizeBlank(cloudAudioId) : null;
    }

    private Double normalizeStartTimeForLesson(Lesson lesson, Double startTime) {
        return resolveLessonType(lesson) == LessonType.VIDEO ? startTime : null;
    }

    private Double normalizeEndTimeForLesson(Lesson lesson, Double endTime) {
        return resolveLessonType(lesson) == LessonType.VIDEO ? endTime : null;
    }

    private LessonType resolveLessonType(Lesson lesson) {
        return lesson.getSection() != null && lesson.getSection().getCategory() != null
                ? lesson.getSection().getCategory().getType()
                : LessonType.AUDIO;
    }

    private void replaceStoredAudio(String currentPublicId, String nextPublicId) {
        if (Objects.equals(currentPublicId, nextPublicId) || currentPublicId == null || currentPublicId.isBlank()) {
            return;
        }
        try {
            mediaStorageGateway.deleteFile(currentPublicId);
        } catch (Exception e) {
            throw new IllegalStateException("Khong the thay the audio cu tren cloud.");
        }
    }

    private void syncLessonSentenceCount(Long lessonId) {
        if (lessonId == null) {
            return;
        }
        lessonRepository.findById(lessonId).ifPresent(lesson -> {
            lesson.setTotalSentences((int) sentenceRepository.countByLesson_Id(lessonId));
            lessonRepository.save(lesson);
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

