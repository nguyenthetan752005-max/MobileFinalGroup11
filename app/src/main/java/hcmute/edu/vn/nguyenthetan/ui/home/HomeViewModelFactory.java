package hcmute.edu.vn.nguyenthetan.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.home.GetHomeDashboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.GetNotificationSummaryUseCase;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final GetHomeDashboardUseCase getHomeDashboardUseCase;
    private final GetNotificationSummaryUseCase getNotificationSummaryUseCase;

    public HomeViewModelFactory(
            GetHomeDashboardUseCase getHomeDashboardUseCase,
            GetNotificationSummaryUseCase getNotificationSummaryUseCase
    ) {
        this.getHomeDashboardUseCase = getHomeDashboardUseCase;
        this.getNotificationSummaryUseCase = getNotificationSummaryUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(getHomeDashboardUseCase, getNotificationSummaryUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
