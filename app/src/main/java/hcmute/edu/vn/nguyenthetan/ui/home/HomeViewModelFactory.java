package hcmute.edu.vn.nguyenthetan.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.home.GetHomeDashboardUseCase;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final GetHomeDashboardUseCase getHomeDashboardUseCase;

    public HomeViewModelFactory(GetHomeDashboardUseCase getHomeDashboardUseCase) {
        this.getHomeDashboardUseCase = getHomeDashboardUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(getHomeDashboardUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
