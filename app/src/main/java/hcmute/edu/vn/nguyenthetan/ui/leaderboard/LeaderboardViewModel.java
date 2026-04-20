package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardData;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.GetLeaderboardUseCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeaderboardViewModel extends ViewModel {

    private final GetLeaderboardUseCase getLeaderboardUseCase;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<LeaderboardUiState> uiState = new MutableLiveData<>(LeaderboardUiState.idle());
    private boolean loaded;

    public LeaderboardViewModel(GetLeaderboardUseCase getLeaderboardUseCase) {
        this.getLeaderboardUseCase = getLeaderboardUseCase;
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

    public void refresh() {
        refreshInternal(true);
    }

    private void refreshInternal(boolean forceReload) {
        if (forceReload) {
            loaded = false;
        }
        uiState.setValue(LeaderboardUiState.loading());
        ioExecutor.execute(() -> {
            try {
                LeaderboardData data = getLeaderboardUseCase.execute();
                boolean hasWeekly = data.getWeeklyEntries() != null && !data.getWeeklyEntries().isEmpty();
                boolean hasMonthly = data.getMonthlyEntries() != null && !data.getMonthlyEntries().isEmpty();
                if (!hasWeekly && !hasMonthly) {
                    uiState.postValue(LeaderboardUiState.empty());
                } else {
                    uiState.postValue(LeaderboardUiState.success(data));
                    loaded = true;
                }
            } catch (Exception exception) {
                uiState.postValue(LeaderboardUiState.error(exception.getMessage()));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ioExecutor.shutdownNow();
    }
}
