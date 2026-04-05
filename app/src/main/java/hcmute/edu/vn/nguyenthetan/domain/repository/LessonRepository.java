package hcmute.edu.vn.nguyenthetan.domain.repository;

import hcmute.edu.vn.nguyenthetan.domain.model.LessonProgress;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonSession;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;

public interface LessonRepository {

    LessonSession getLessonSession(long lessonId);

    LessonProgress getLessonProgress(long lessonId);

    void saveSentenceStatus(long lessonId, long sentenceId, SentenceStatus status);

    void saveSpeakingAttempt(long lessonId, long sentenceId, SpeakingAttempt attempt, int passThreshold);
}
