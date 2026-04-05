package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.model.LessonProgress;
import hcmute.edu.vn.nguyenthetan.domain.repository.LessonRepository;

public class GetLessonProgressUseCase {

    private final LessonRepository repository;

    public GetLessonProgressUseCase(LessonRepository repository) {
        this.repository = repository;
    }

    public LessonProgress execute(long lessonId) {
        return repository.getLessonProgress(lessonId);
    }
}
