package hcmute.edu.vn.nguyenthetan.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.GoogleAuthUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.RegisterUseCase;

public class RegisterViewModelFactory implements ViewModelProvider.Factory {

    private final RegisterUseCase registerUseCase;
    private final GoogleAuthUseCase googleAuthUseCase;
    private final UserSessionStore session;
    private final Runnable onAuthSuccess;

    public RegisterViewModelFactory(
            RegisterUseCase registerUseCase,
            GoogleAuthUseCase googleAuthUseCase,
            UserSessionStore session,
            Runnable onAuthSuccess
    ) {
        this.registerUseCase = registerUseCase;
        this.googleAuthUseCase = googleAuthUseCase;
        this.session = session;
        this.onAuthSuccess = onAuthSuccess;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
            return (T) new RegisterViewModel(registerUseCase, googleAuthUseCase, session, onAuthSuccess);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
