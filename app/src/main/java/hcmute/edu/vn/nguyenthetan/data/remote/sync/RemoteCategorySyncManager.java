package hcmute.edu.vn.nguyenthetan.data.remote.sync;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.RetryUtil;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileCategoryCollectionDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileCategoryCollectionSectionDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileCategoryDto;
import hcmute.edu.vn.nguyenthetan.data.remote.mapper.RemoteEntityMapper;
import retrofit2.Response;

public class RemoteCategorySyncManager {

    private static final String TAG = "RemoteCategorySync";

    private final MobileApiService mobileApiService;
    private final TungTungDatabase database;

    public RemoteCategorySyncManager(MobileApiService mobileApiService, TungTungDatabase database) {
        this.mobileApiService = mobileApiService;
        this.database = database;
    }

    public void syncCategories() {
        try {
            Response<List<MobileCategoryDto>> response = RetryUtil.retryWithBackoff(
                    () -> mobileApiService.getCategories().execute(),
                    3,
                    700L,
                    2500L,
                    2.0
            );
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Category sync skipped. HTTP " + response.code());
                return;
            }

            List<CategoryEntity> categories = RemoteEntityMapper.toCategoryEntities(response.body());
            if (categories.isEmpty()) {
                Log.w(TAG, "Category sync skipped. Empty category payload.");
                return;
            }

            database.runInTransaction(() -> database.categoryDao().insertAll(categories));
        } catch (IOException exception) {
            Log.w(TAG, "Category sync failed: " + exception.getMessage());
        }
    }

    public void syncCategoryCollection(String categorySlug) {
        try {
            Response<MobileCategoryCollectionDto> response = RetryUtil.retryWithBackoff(
                    () -> mobileApiService.getCategoryCollection(categorySlug).execute(),
                    3,
                    700L,
                    2500L,
                    2.0
            );
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Category collection sync skipped. HTTP " + response.code() + " for " + categorySlug);
                return;
            }

            MobileCategoryCollectionDto collection = response.body();
            CategoryEntity category = database.categoryDao().getBySlug(categorySlug);
            if (category == null) {
                Log.w(TAG, "Category collection sync skipped. Category not found locally for slug " + categorySlug);
                return;
            }

            List<SectionEntity> sections = RemoteEntityMapper.toSectionEntities(String.valueOf(category.id), collection.sections);
            List<LessonEntity> lessons = new ArrayList<>();
            if (collection.sections != null) {
                for (MobileCategoryCollectionSectionDto sectionDto : collection.sections) {
                    lessons.addAll(RemoteEntityMapper.toLessonEntities(sectionDto.lessons));
                }
            }

            database.runInTransaction(() -> {
                database.sectionDao().deleteByCategoryId(category.id);
                database.sectionDao().insertAll(sections);
                database.lessonDao().insertAll(lessons);
            });
        } catch (IOException exception) {
            Log.w(TAG, "Category collection sync failed for " + categorySlug + ": " + exception.getMessage());
        }
    }
}
