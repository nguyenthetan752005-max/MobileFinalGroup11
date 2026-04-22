package hcmute.edu.vn.nguyenthetan.domain.repository;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonCollection;

public interface CatalogRepository {

    void syncCategoryCollection(String categorySlug);

    void syncSectionLessons(long sectionId);

    boolean hasCategoryCollection(String categorySlug);

    List<ExploreCategory> getExploreCategories();

    LessonCollection getLessonCollection(String categorySlug);
}
