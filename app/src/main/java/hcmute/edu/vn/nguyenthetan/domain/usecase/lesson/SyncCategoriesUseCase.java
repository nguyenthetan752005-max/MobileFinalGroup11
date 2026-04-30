package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import hcmute.edu.vn.nguyenthetan.domain.repository.CatalogRepository;

public class SyncCategoriesUseCase {
    private final CatalogRepository catalogRepository;

    public SyncCategoriesUseCase(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public void execute() {
        catalogRepository.syncCategories();
    }
}
