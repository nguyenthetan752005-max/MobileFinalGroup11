package hcmute.edu.vn.nguyenthetan.data.remote.sync;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.RetryUtil;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.system.SyncStateEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapDto;
import hcmute.edu.vn.nguyenthetan.data.remote.mapper.RemoteEntityMapper;
import retrofit2.Response;

public class RemoteCatalogSyncManager {

    private static final String TAG = "RemoteCatalogSync";

    private final MobileApiService mobileApiService;
    private final TungTungDatabase database;

    public RemoteCatalogSyncManager(MobileApiService mobileApiService, TungTungDatabase database) {
        this.mobileApiService = mobileApiService;
        this.database = database;
    }

    public void sync() throws IOException {
        Response<MobileBootstrapDto> response = RetryUtil.retryWithBackoff(
                () -> mobileApiService.getBootstrapLite().execute(),
                3,
                700L,
                2500L,
                2.0
        );
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Bootstrap lite failed with HTTP " + response.code());
        }

        MobileBootstrapDto bootstrap = response.body();
        String remoteVersion = resolveRemoteVersion(bootstrap);
        SyncStateEntity currentState = database.syncStateDao().getByKey("catalog");
        if (currentState != null
                && currentState.version != null
                && currentState.version.equals(remoteVersion)
                && database.categoryDao().count() > 0) {
            Log.d(TAG, "Catalog sync skipped. Remote version unchanged: " + remoteVersion);
            return;
        }

        List<CategoryEntity> categories = RemoteEntityMapper.toCategoryEntities(bootstrap.categories);
        List<SectionEntity> sections = RemoteEntityMapper.toSectionEntities(bootstrap.sections);
        List<LessonEntity> lessons = RemoteEntityMapper.toLessonEntities(bootstrap.lessons, sections, categories);
        if (categories.isEmpty()) {
            throw new IOException("Bootstrap lite returned empty categories.");
        }

        database.runInTransaction(() -> {
            database.sentenceDao().deleteAll();
            database.lessonDao().deleteAll();
            database.sectionDao().deleteAll();
            database.categoryDao().deleteAll();

            database.categoryDao().insertAll(categories);
            database.sectionDao().insertAll(sections);
            database.lessonDao().insertAll(lessons);
            database.syncStateDao().upsert(new SyncStateEntity(
                    "catalog",
                    remoteVersion,
                    System.currentTimeMillis()
            ));
        });
    }

    private String resolveRemoteVersion(MobileBootstrapDto bootstrap) {
        if (bootstrap == null) {
            return "unknown";
        }
        if (bootstrap.version != null && !bootstrap.version.trim().isEmpty()) {
            return bootstrap.version.trim();
        }
        if (bootstrap.generatedAt != null && !bootstrap.generatedAt.trim().isEmpty()) {
            return bootstrap.generatedAt.trim();
        }
        return "unknown";
    }
}
