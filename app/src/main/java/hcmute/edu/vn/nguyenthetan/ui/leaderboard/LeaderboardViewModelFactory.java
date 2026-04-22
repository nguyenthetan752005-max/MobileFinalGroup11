package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.GetLeaderboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.SyncLeaderboardUseCase;

public class LeaderboardViewModelFactory implements ViewModelProvider.Factory {

    private final GetLeaderboardUseCase getLeaderboardUseCase;
    private final SyncLeaderboardUseCase syncLeaderboardUseCase;

    public LeaderboardViewModelFactory(GetLeaderboardUseCase getLeaderboardUseCase, SyncLeaderboardUseCase syncLeaderboardUseCase) {
        this.getLeaderboardUseCase = getLeaderboardUseCase;
        this.syncLeaderboardUseCase = syncLeaderboardUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LeaderboardViewModel.class)) {
            return (T) new LeaderboardViewModel(getLeaderboardUseCase, syncLeaderboardUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
