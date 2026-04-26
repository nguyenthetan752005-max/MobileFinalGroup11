package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.repository.LessonRepository;

public class SyncLessonProgressUseCase {

    private final LessonRepository repository;

    public SyncLessonProgressUseCase(LessonRepository repository) {
        this.repository = repository;
    }

    public void execute(long lessonId) {
        repository.syncLessonProgress(lessonId);
    }
}
