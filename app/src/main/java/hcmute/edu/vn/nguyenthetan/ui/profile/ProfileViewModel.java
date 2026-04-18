package hcmute.edu.vn.nguyenthetan.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.profile.ProfileData;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetProfileUseCase;

public class ProfileViewModel extends ViewModel {

    private final GetProfileUseCase getProfileUseCase;
    private final MutableLiveData<ProfileData> profileState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean loaded;

    public ProfileViewModel(GetProfileUseCase getProfileUseCase) {
        this.getProfileUseCase = getProfileUseCase;
    }

    public LiveData<ProfileData> getProfileState() {
        return profileState;
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
            profileState.postValue(getProfileUseCase.execute());
            loadingState.postValue(false);
        });
        loaded = true;
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
        super.onCleared();
    }
}
