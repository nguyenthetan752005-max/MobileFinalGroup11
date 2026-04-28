package hcmute.edu.vn.nguyenthetan.ui.profile;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.model.profile.ReminderSettings;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ProfileData;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetProfileUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetReminderSettingsUseCase;

public class ProfileViewModel extends ViewModel {

    public static final class ReminderResult {
        public final boolean fetched;
        @Nullable public final ReminderSettings settings;

        ReminderResult(boolean fetched, @Nullable ReminderSettings settings) {
            this.fetched = fetched;
            this.settings = settings;
        }
    }

    private final GetProfileUseCase getProfileUseCase;
    private final GetReminderSettingsUseCase getReminderSettingsUseCase;
    private final MutableLiveData<ProfileData> profileState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    private final MutableLiveData<ReminderResult> reminderState = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean loaded;

    public ProfileViewModel(
            GetProfileUseCase getProfileUseCase,
            GetReminderSettingsUseCase getReminderSettingsUseCase
    ) {
        this.getProfileUseCase = getProfileUseCase;
        this.getReminderSettingsUseCase = getReminderSettingsUseCase;
    }

    public LiveData<ProfileData> getProfileState() {
        return profileState;
    }

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public LiveData<ReminderResult> getReminderState() {
        return reminderState;
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

    public void forceLoad() {
        loaded = false;
        load();
    }

    public void refreshReminderSettings() {
        executorService.execute(() -> {
            ReminderSettings settings = getReminderSettingsUseCase.execute();
            reminderState.postValue(new ReminderResult(settings != null, settings));
        });
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
        super.onCleared();
    }
}
