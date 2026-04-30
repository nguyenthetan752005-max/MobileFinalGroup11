package hcmute.edu.vn.nguyenthetan.domain.repository;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonProgress;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonSession;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;

public interface LessonRepository {

    void syncLessonContent(long lessonId);
    void syncLessonProgress(long lessonId);

    boolean hasLessonContent(long lessonId);

    LessonSession getLessonSession(long lessonId);

    LessonProgress getLessonProgress(long lessonId);

    String getLessonCategorySlug(long lessonId);

    void saveSentenceStatus(long lessonId, long sentenceId, SentenceStatus status);

    void saveSpeakingAttempt(long lessonId, long sentenceId, SpeakingAttempt attempt, int passThreshold);
}
