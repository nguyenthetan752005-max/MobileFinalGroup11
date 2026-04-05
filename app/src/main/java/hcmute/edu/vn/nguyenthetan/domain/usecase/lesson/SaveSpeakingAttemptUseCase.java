package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;
import hcmute.edu.vn.nguyenthetan.domain.repository.LessonRepository;

public class SaveSpeakingAttemptUseCase {

    private final LessonRepository repository;

    public SaveSpeakingAttemptUseCase(LessonRepository repository) {
        this.repository = repository;
    }

    public void execute(long lessonId, long sentenceId, SpeakingAttempt attempt, int passThreshold) {
        repository.saveSpeakingAttempt(lessonId, sentenceId, attempt, passThreshold);
    }
}
