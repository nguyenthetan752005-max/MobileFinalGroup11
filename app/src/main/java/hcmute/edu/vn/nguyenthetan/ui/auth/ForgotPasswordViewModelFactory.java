package hcmute.edu.vn.nguyenthetan.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.ForgotPasswordUseCase;

public class ForgotPasswordViewModelFactory implements ViewModelProvider.Factory {

    private final ForgotPasswordUseCase useCase;

    public ForgotPasswordViewModelFactory(ForgotPasswordUseCase useCase) {
        this.useCase = useCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel.class)) {
            return (T) new ForgotPasswordViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
