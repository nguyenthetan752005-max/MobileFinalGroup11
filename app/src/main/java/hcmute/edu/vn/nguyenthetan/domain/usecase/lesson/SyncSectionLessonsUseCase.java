package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.repository.CatalogRepository;

public class SyncSectionLessonsUseCase {
    private final CatalogRepository catalogRepository;

    public SyncSectionLessonsUseCase(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public void execute(long sectionId) {
        catalogRepository.syncSectionLessons(sectionId);
    }
}
