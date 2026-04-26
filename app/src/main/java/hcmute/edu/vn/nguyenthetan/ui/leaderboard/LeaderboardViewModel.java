package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardData;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.GetLeaderboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.SyncLeaderboardUseCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeaderboardViewModel extends ViewModel {

    private static final String GENERIC_ERROR = "Could not load leaderboard. Please try again.";

    private final GetLeaderboardUseCase getLeaderboardUseCase;
    private final SyncLeaderboardUseCase syncLeaderboardUseCase;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<LeaderboardUiState> uiState = new MutableLiveData<>(LeaderboardUiState.idle());
    private boolean loaded;

    public LeaderboardViewModel(GetLeaderboardUseCase getLeaderboardUseCase, SyncLeaderboardUseCase syncLeaderboardUseCase) {
        this.getLeaderboardUseCase = getLeaderboardUseCase;
        this.syncLeaderboardUseCase = syncLeaderboardUseCase;
    }

    public LiveData<LeaderboardUiState> getUiState() {
        return uiState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        refreshInternal(false);
    }

    public void forceLoad() {
        loaded = false;
        load();
    }

    public void refresh() {
        refreshInternal(true);
    }

    private void refreshInternal(boolean forceReload) {
        if (forceReload) {
            loaded = false;
        }
        uiState.postValue(LeaderboardUiState.loading());
        ioExecutor.execute(() -> {
            Exception syncFailure = null;
            try {
                if (forceReload) {
                    syncLeaderboardUseCase.execute();
                }
            } catch (Exception exception) {
                syncFailure = exception;
            }

            try {
                LeaderboardData data = getLeaderboardUseCase.execute();
                boolean hasWeekly = data.getWeeklyEntries() != null && !data.getWeeklyEntries().isEmpty();
                boolean hasMonthly = data.getMonthlyEntries() != null && !data.getMonthlyEntries().isEmpty();

                if (!hasWeekly && !hasMonthly && !forceReload && syncFailure == null) {
                    try {
                        syncLeaderboardUseCase.execute();
                        data = getLeaderboardUseCase.execute();
                        hasWeekly = data.getWeeklyEntries() != null && !data.getWeeklyEntries().isEmpty();
                        hasMonthly = data.getMonthlyEntries() != null && !data.getMonthlyEntries().isEmpty();
                    } catch (Exception retryException) {
                        syncFailure = retryException;
                    }
                }

                if (!hasWeekly && !hasMonthly) {
                    if (syncFailure != null) {
                        uiState.postValue(LeaderboardUiState.error(GENERIC_ERROR));
                    } else {
                        uiState.postValue(LeaderboardUiState.empty());
                    }
                } else {
                    uiState.postValue(LeaderboardUiState.success(data));
                    loaded = true;
                }
            } catch (Exception exception) {
                uiState.postValue(LeaderboardUiState.error(GENERIC_ERROR));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdownNow();
    }
}
