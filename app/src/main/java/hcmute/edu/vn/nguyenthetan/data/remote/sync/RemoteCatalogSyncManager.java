package hcmute.edu.vn.nguyenthetan.data.remote.sync;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
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

    public void sync() {
        try {
            Response<MobileBootstrapDto> response = mobileApiService.getBootstrap().execute();
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Bootstrap sync skipped. HTTP " + response.code());
                return;
            }

            MobileBootstrapDto bootstrap = response.body();
            List<CategoryEntity> categories = RemoteEntityMapper.toCategoryEntities(bootstrap.categories);
            if (categories.isEmpty()) {
                Log.w(TAG, "Bootstrap sync skipped. Empty category payload.");
                return;
            }

            SyncStateEntity currentState = database.syncStateDao().getByKey("catalog");
            if (currentState != null && currentState.version != null && currentState.version.equals(bootstrap.version)) {
                Log.d(TAG, "Bootstrap sync skipped. Catalog version is unchanged.");
                return;
            }

            database.runInTransaction(() -> {
                database.categoryDao().deleteAll();
                database.categoryDao().insertAll(categories);
                database.sectionDao().insertAll(RemoteEntityMapper.toSectionEntities(bootstrap.sections));
                database.lessonDao().insertAll(RemoteEntityMapper.toLessonEntities(bootstrap.lessons));
                database.sentenceDao().insertAll(RemoteEntityMapper.toSentenceEntities(bootstrap.sentences));
                database.commentDao().insertAll(RemoteEntityMapper.toCommentEntities(bootstrap.comments));
                database.leaderboardDao().deleteEntries();
                database.leaderboardDao().deleteMeta();
                database.leaderboardDao().insertEntries(RemoteEntityMapper.toLeaderboardEntries(bootstrap));
                database.leaderboardDao().upsertMeta(RemoteEntityMapper.toLeaderboardMeta(bootstrap));
                database.syncStateDao().upsert(new hcmute.edu.vn.nguyenthetan.data.local.entity.system.SyncStateEntity(
                        "catalog",
                        bootstrap.version,
                        System.currentTimeMillis()
                ));
            });
        } catch (IOException exception) {
            Log.w(TAG, "Bootstrap sync failed: " + exception.getMessage());
        }
    }
}
