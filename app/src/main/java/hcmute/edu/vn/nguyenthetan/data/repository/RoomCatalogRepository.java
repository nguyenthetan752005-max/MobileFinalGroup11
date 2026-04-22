package hcmute.edu.vn.nguyenthetan.data.repository;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.AppDefaults;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.CategoryDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.GuestProgressDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.LessonDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.SectionDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteCategorySyncManager;
import hcmute.edu.vn.nguyenthetan.data.repository.support.RepositoryFormatters;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.ExploreCategory;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonCollection;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonSection;
import hcmute.edu.vn.nguyenthetan.domain.model.explore.LessonSummary;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.repository.CatalogRepository;

public class RoomCatalogRepository implements CatalogRepository {

    private final CategoryDao categoryDao;
    private final SectionDao sectionDao;
    private final LessonDao lessonDao;
    private final GuestProgressDao guestProgressDao;
    private final RemoteCategorySyncManager remoteCategorySyncManager;

    public RoomCatalogRepository(
            CategoryDao categoryDao,
            SectionDao sectionDao,
            LessonDao lessonDao,
            GuestProgressDao guestProgressDao,
            RemoteCategorySyncManager remoteCategorySyncManager
    ) {
        this.categoryDao = categoryDao;
        this.sectionDao = sectionDao;
        this.lessonDao = lessonDao;
        this.guestProgressDao = guestProgressDao;
        this.remoteCategorySyncManager = remoteCategorySyncManager;
    }

    @Override
    public void syncCategoryCollection(String categorySlug) {
        if (remoteCategorySyncManager != null) {
            remoteCategorySyncManager.syncCategoryCollection(categorySlug);
        }
    }

    @Override
    public void syncSectionLessons(long sectionId) {
        if (remoteCategorySyncManager != null) {
            remoteCategorySyncManager.syncSectionLessons(sectionId);
        }
    }

    @Override
    public boolean hasCategoryCollection(String categorySlug) {
        CategoryEntity category = categoryDao.getBySlug(categorySlug);
        return category != null && sectionDao.countByCategoryId(category.id) > 0;
    }

    @Override
    public List<ExploreCategory> getExploreCategories() {
        List<ExploreCategory> categories = new ArrayList<>();
        for (CategoryEntity category : categoryDao.getAllOrdered()) {
            List<LessonEntity> lessons = getLessonsForCategory(category.id);
            int completedLessons = 0;
            boolean hasProgress = false;

            for (LessonEntity lesson : lessons) {
                int completedSentences = guestProgressDao.getCompletedCountForLesson(lesson.id);
                int touchedSentences = guestProgressDao.getTouchedCountForLesson(lesson.id);
                if (completedSentences >= lesson.totalSentences && lesson.totalSentences > 0) {
                    completedLessons++;
                }
                if (touchedSentences > 0) {
                    hasProgress = true;
                }
            }

            String progressLabel = completedLessons == lessons.size() && !lessons.isEmpty()
                    ? "Completed"
                    : (hasProgress ? "In Progress" : "New");

            categories.add(new ExploreCategory(
                    category.slug,
                    category.imageUrl,
                    category.name,
                    category.levelRange,
                    category.totalLessons,
                    category.practiceType,
                    progressLabel
            ));
        }
        return categories;
    }

    @Override
    public LessonCollection getLessonCollection(String categorySlug) {
        CategoryEntity category = categoryDao.getBySlug(categorySlug);
        if (category == null) {
            category = categoryDao.getBySlug(AppDefaults.DEFAULT_CATEGORY_SLUG);
        }
        if (category == null) {
            category = categoryDao.getFirstCategory();
        }
        if (category == null) {
            return new LessonCollection(categorySlug == null ? "" : categorySlug, "", "", 0, new ArrayList<>());
        }

        List<LessonSection> sections = new ArrayList<>();
        for (SectionEntity section : sectionDao.getByCategoryId(category.id)) {
            List<LessonSummary> lessonSummaries = new ArrayList<>();
            for (LessonEntity lesson : lessonDao.getBySectionId(section.id)) {
                int completedSentences = guestProgressDao.getCompletedCountForLesson(lesson.id);
                int touchedSentences = guestProgressDao.getTouchedCountForLesson(lesson.id);
                lessonSummaries.add(new LessonSummary(
                        lesson.id,
                        lesson.title,
                        lesson.level,
                        RepositoryFormatters.formatLessonMode(category.practiceType, lesson.contentType),
                        completedSentences,
                        lesson.totalSentences,
                        resolveSummaryStatus(completedSentences, lesson.totalSentences, touchedSentences)
                ));
            }
            sections.add(new LessonSection(section.id, section.name, lessonSummaries));
        }

        return new LessonCollection(
                category.slug,
                category.name,
                category.description,
                category.totalLessons,
                sections
        );
    }

    private SentenceStatus resolveSummaryStatus(int completedSentences, int totalSentences, int touchedSentences) {
        if (totalSentences > 0 && completedSentences >= totalSentences) {
            return SentenceStatus.COMPLETED;
        }
        return touchedSentences > 0 ? SentenceStatus.IN_PROGRESS : SentenceStatus.NOT_STARTED;
    }

    private List<LessonEntity> getLessonsForCategory(long categoryId) {
        List<LessonEntity> lessons = new ArrayList<>();
        for (SectionEntity section : sectionDao.getByCategoryId(categoryId)) {
            lessons.addAll(lessonDao.getBySectionId(section.id));
        }
        return lessons;
    }
}
