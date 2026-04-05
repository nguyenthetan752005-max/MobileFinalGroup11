package hcmute.edu.vn.nguyenthetan.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hcmute.edu.vn.nguyenthetan.domain.model.home.HomeDashboard;
import hcmute.edu.vn.nguyenthetan.domain.usecase.home.GetHomeDashboardUseCase;

public class HomeViewModel extends ViewModel {

    private final GetHomeDashboardUseCase getHomeDashboardUseCase;
    private final MutableLiveData<HomeDashboard> dashboardState = new MutableLiveData<>();
    private boolean loaded;

    public HomeViewModel(GetHomeDashboardUseCase getHomeDashboardUseCase) {
        this.getHomeDashboardUseCase = getHomeDashboardUseCase;
    }

    public LiveData<HomeDashboard> getDashboardState() {
        return dashboardState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        dashboardState.setValue(getHomeDashboardUseCase.execute());
        loaded = true;
    }

    public void forceLoad() {
        loaded = false;
        load();
    }
}
