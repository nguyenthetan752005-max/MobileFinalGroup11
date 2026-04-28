package hcmute.edu.vn.nguyenthetan.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.home.HomeDashboard;
import hcmute.edu.vn.nguyenthetan.domain.usecase.home.GetHomeDashboardUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.notification.GetNotificationSummaryUseCase;

public class HomeViewModel extends ViewModel {

    private final GetHomeDashboardUseCase getHomeDashboardUseCase;
    private final GetNotificationSummaryUseCase getNotificationSummaryUseCase;
    private final MutableLiveData<HomeDashboard> dashboardState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final MutableLiveData<Long> unreadNotificationCount = new MutableLiveData<>(0L);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean loaded;

    public HomeViewModel(
            GetHomeDashboardUseCase getHomeDashboardUseCase,
            GetNotificationSummaryUseCase getNotificationSummaryUseCase
    ) {
        this.getHomeDashboardUseCase = getHomeDashboardUseCase;
        this.getNotificationSummaryUseCase = getNotificationSummaryUseCase;
    }

    public LiveData<HomeDashboard> getDashboardState() {
        return dashboardState;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public LiveData<Long> getUnreadNotificationCount() {
        return unreadNotificationCount;
    }

    public void load() {
        if (loaded) {
            return;
        }
        loadingState.setValue(true);
        executorService.execute(() -> {
            dashboardState.postValue(getHomeDashboardUseCase.execute());
            loadingState.postValue(false);
        });
        loaded = true;
    }

    public void forceLoad() {
        loaded = false;
        load();
    }

    public void refreshNotificationBadge(boolean loggedIn) {
        if (!loggedIn) {
            unreadNotificationCount.postValue(0L);
            return;
        }
        executorService.execute(() ->
                unreadNotificationCount.postValue(getNotificationSummaryUseCase.execute())
        );
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
        super.onCleared();
    }
}
