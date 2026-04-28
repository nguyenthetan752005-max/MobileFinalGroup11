package hcmute.edu.vn.nguyenthetan.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.ChangePasswordUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.UpdateUsernameUseCase;

public class EditProfileViewModelFactory implements ViewModelProvider.Factory {

    private final UpdateUsernameUseCase updateUsernameUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UserSessionStore session;

    public EditProfileViewModelFactory(
            UpdateUsernameUseCase updateUsernameUseCase,
            ChangePasswordUseCase changePasswordUseCase,
            UserSessionStore session
    ) {
        this.updateUsernameUseCase = updateUsernameUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.session = session;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EditProfileViewModel.class)) {
            return (T) new EditProfileViewModel(updateUsernameUseCase, changePasswordUseCase, session);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
