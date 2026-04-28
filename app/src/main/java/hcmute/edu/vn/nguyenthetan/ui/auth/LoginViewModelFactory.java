package hcmute.edu.vn.nguyenthetan.ui.auth;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.GoogleAuthUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.LoginUseCase;

public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private final LoginUseCase loginUseCase;
    private final GoogleAuthUseCase googleAuthUseCase;
    private final UserSessionStore session;
    private final Runnable onAuthSuccess;

    public LoginViewModelFactory(
            LoginUseCase loginUseCase,
            GoogleAuthUseCase googleAuthUseCase,
            UserSessionStore session,
            Runnable onAuthSuccess
    ) {
        this.loginUseCase = loginUseCase;
        this.googleAuthUseCase = googleAuthUseCase;
        this.session = session;
        this.onAuthSuccess = onAuthSuccess;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(loginUseCase, googleAuthUseCase, session, onAuthSuccess);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
