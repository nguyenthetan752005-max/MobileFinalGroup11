package hcmute.edu.vn.nguyenthetan.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.home.HomeDashboard;
import hcmute.edu.vn.nguyenthetan.domain.usecase.home.GetHomeDashboardUseCase;

public class HomeViewModel extends ViewModel {

    private final GetHomeDashboardUseCase getHomeDashboardUseCase;
    private final MutableLiveData<HomeDashboard> dashboardState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean loaded;

    public HomeViewModel(GetHomeDashboardUseCase getHomeDashboardUseCase) {
        this.getHomeDashboardUseCase = getHomeDashboardUseCase;
    }

    public LiveData<HomeDashboard> getDashboardState() {
        return dashboardState;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
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

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
        super.onCleared();
    }
}
