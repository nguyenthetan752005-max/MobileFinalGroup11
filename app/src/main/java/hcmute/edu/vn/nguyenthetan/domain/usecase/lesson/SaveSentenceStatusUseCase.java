package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.repository.LessonRepository;

public class SaveSentenceStatusUseCase {

    private final LessonRepository repository;

    public SaveSentenceStatusUseCase(LessonRepository repository) {
        this.repository = repository;
    }

    public void execute(long lessonId, long sentenceId, SentenceStatus status) {
        repository.saveSentenceStatus(lessonId, sentenceId, status);
    }
}
