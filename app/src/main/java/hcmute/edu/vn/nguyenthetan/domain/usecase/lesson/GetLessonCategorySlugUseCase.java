package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.repository.LessonRepository;

public class GetLessonCategorySlugUseCase {

    private final LessonRepository repository;

    public GetLessonCategorySlugUseCase(LessonRepository repository) {
        this.repository = repository;
    }

    public String execute(long lessonId) {
        return repository.getLessonCategorySlug(lessonId);
    }
}
