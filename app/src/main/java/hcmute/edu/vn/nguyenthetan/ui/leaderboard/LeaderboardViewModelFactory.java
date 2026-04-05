package hcmute.edu.vn.nguyenthetan.ui.leaderboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.leaderboard.GetLeaderboardUseCase;

public class LeaderboardViewModelFactory implements ViewModelProvider.Factory {

    private final GetLeaderboardUseCase getLeaderboardUseCase;

    public LeaderboardViewModelFactory(GetLeaderboardUseCase getLeaderboardUseCase) {
        this.getLeaderboardUseCase = getLeaderboardUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LeaderboardViewModel.class)) {
            return (T) new LeaderboardViewModel(getLeaderboardUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
