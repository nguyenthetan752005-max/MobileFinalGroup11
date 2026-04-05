package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hcmute.edu.vn.nguyenthetan.domain.model.leaderboard.LeaderboardData;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.GetLeaderboardUseCase;

public class LeaderboardViewModel extends ViewModel {

    private final GetLeaderboardUseCase getLeaderboardUseCase;
    private final MutableLiveData<LeaderboardData> leaderboardState = new MutableLiveData<>();
    private boolean loaded;

    public LeaderboardViewModel(GetLeaderboardUseCase getLeaderboardUseCase) {
        this.getLeaderboardUseCase = getLeaderboardUseCase;
    }

    public LiveData<LeaderboardData> getLeaderboardState() {
        return leaderboardState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        leaderboardState.setValue(getLeaderboardUseCase.execute());
        loaded = true;
    }
}
