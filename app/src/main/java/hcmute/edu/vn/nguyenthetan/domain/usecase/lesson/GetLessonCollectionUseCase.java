package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonCollection;
import hcmute.edu.vn.nguyenthetan.domain.repository.CatalogRepository;

public class GetLessonCollectionUseCase {

    private final CatalogRepository repository;

    public GetLessonCollectionUseCase(CatalogRepository repository) {
        this.repository = repository;
    }

    public LessonCollection execute(String categoryId) {
        repository.syncCategoryCollection(categoryId);
        return repository.getLessonCollection(categoryId);
    }
}
