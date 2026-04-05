package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonSession;
import hcmute.edu.vn.nguyenthetan.domain.repository.LessonRepository;

public class GetLessonSessionUseCase {

    private final LessonRepository repository;

    public GetLessonSessionUseCase(LessonRepository repository) {
        this.repository = repository;
    }

    public LessonSession execute(long lessonId) {
        return repository.getLessonSession(lessonId);
    }
}
