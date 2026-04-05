package hcmute.edu.vn.nguyenthetan.domain.usecase.explore;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;
import hcmute.edu.vn.nguyenthetan.domain.repository.CatalogRepository;

public class GetExploreCatalogUseCase {

    private final CatalogRepository repository;

    public GetExploreCatalogUseCase(CatalogRepository repository) {
        this.repository = repository;
    }

    public List<ExploreCategory> execute() {
        return repository.getExploreCategories();
    }
}
