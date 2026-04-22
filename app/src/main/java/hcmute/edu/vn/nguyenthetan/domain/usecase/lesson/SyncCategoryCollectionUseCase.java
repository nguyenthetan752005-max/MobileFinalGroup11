package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.repository.CatalogRepository;

public class SyncCategoryCollectionUseCase {
    private final CatalogRepository catalogRepository;

    public SyncCategoryCollectionUseCase(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public void execute(String categorySlug) {
        catalogRepository.syncCategoryCollection(categorySlug);
    }
}
