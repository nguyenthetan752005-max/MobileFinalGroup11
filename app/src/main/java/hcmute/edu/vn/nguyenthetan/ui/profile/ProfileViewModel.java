package hcmute.edu.vn.nguyenthetan.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hcmute.edu.vn.nguyenthetan.domain.model.profile.ProfileData;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetProfileUseCase;

public class ProfileViewModel extends ViewModel {

    private final GetProfileUseCase getProfileUseCase;
    private final MutableLiveData<ProfileData> profileState = new MutableLiveData<>();
    private boolean loaded;

    public ProfileViewModel(GetProfileUseCase getProfileUseCase) {
        this.getProfileUseCase = getProfileUseCase;
    }

    public LiveData<ProfileData> getProfileState() {
        return profileState;
    }

    public void load() {
        if (loaded) {
            return;
        }
        profileState.setValue(getProfileUseCase.execute());
        loaded = true;
    }
}
