package hcmute.edu.vn.nguyenthetan.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetProfileUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetReminderSettingsUseCase;

public class ProfileViewModelFactory implements ViewModelProvider.Factory {

    private final GetProfileUseCase getProfileUseCase;
    private final GetReminderSettingsUseCase getReminderSettingsUseCase;

    public ProfileViewModelFactory(
            GetProfileUseCase getProfileUseCase,
            GetReminderSettingsUseCase getReminderSettingsUseCase
    ) {
        this.getProfileUseCase = getProfileUseCase;
        this.getReminderSettingsUseCase = getReminderSettingsUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(getProfileUseCase, getReminderSettingsUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
