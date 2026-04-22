package hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteLeaderboardSyncManager;

public class SyncLeaderboardUseCase {

    private final RemoteLeaderboardSyncManager remoteLeaderboardSyncManager;

    public SyncLeaderboardUseCase(RemoteLeaderboardSyncManager remoteLeaderboardSyncManager) {
        this.remoteLeaderboardSyncManager = remoteLeaderboardSyncManager;
    }

    public void execute() throws IOException {
        remoteLeaderboardSyncManager.sync();
    }
}
