package hcmute.edu.vn.nguyenthetan.data.remote.sync;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.RetryUtil;
import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardEntryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardMetaEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLeaderboardDto;
import hcmute.edu.vn.nguyenthetan.data.remote.mapper.RemoteEntityMapper;
import retrofit2.Response;

public class RemoteLeaderboardSyncManager {

    private static final String TAG = "RemoteLeaderboardSync";

    private final MobileApiService mobileApiService;
    private final TungTungDatabase database;

    public RemoteLeaderboardSyncManager(MobileApiService mobileApiService, TungTungDatabase database) {
        this.mobileApiService = mobileApiService;
        this.database = database;
    }

    public void sync() throws IOException {
        Response<MobileLeaderboardDto> response = RetryUtil.retryWithBackoff(
                () -> mobileApiService.getLeaderboard().execute(),
                3,
                700L,
                2500L,
                2.0
        );
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Leaderboard sync failed with HTTP " + response.code());
        }

        MobileLeaderboardDto dto = response.body();
        List<LeaderboardEntryEntity> entries = RemoteEntityMapper.toLeaderboardEntries(dto);
        LeaderboardMetaEntity meta = RemoteEntityMapper.toLeaderboardMeta(dto);

        database.runInTransaction(() -> {
            database.leaderboardDao().deleteEntries();
            database.leaderboardDao().deleteMeta();
            if (!entries.isEmpty()) {
                database.leaderboardDao().insertEntries(entries);
            }
            database.leaderboardDao().upsertMeta(meta);
        });

        Log.d(TAG, "Leaderboard sync completed. entries=" + entries.size());
    }
}
