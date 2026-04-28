package hcmute.edu.vn.nguyenthetan.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;

import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.AuthResult;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.GoogleAuthUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.RegisterUseCase;

public class RegisterViewModel extends ViewModel {

    private final RegisterUseCase registerUseCase;
    private final GoogleAuthUseCase googleAuthUseCase;
    private final UserSessionStore session;
    private final Runnable onAuthSuccess;
    private final MutableLiveData<AuthSubmissionState> state = new MutableLiveData<>(AuthSubmissionState.idle());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RegisterViewModel(
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

    public LiveData<AuthSubmissionState> getState() {
        return state;
    }

    public void submitRegister(String username, String email, String password) {
        if (isLoading()) return;
        state.setValue(AuthSubmissionState.loading());
        executor.execute(() -> handleResult(registerUseCase.execute(username, email, password)));
    }

    public void submitGoogle(String idToken) {
        if (isLoading()) return;
        state.setValue(AuthSubmissionState.loading());
        executor.execute(() -> handleResult(googleAuthUseCase.execute(idToken)));
    }

    public void acknowledge() {
        state.setValue(AuthSubmissionState.idle());
    }

    private boolean isLoading() {
        AuthSubmissionState current = state.getValue();
        return current != null && current.status == AuthSubmissionState.Status.LOADING;
    }

    private void handleResult(AuthResult result) {
        switch (result.status) {
            case SUCCESS:
                if (result.isSuccess()) {
                    session.saveUser(result.userId, result.username, result.email, result.token);
                    if (onAuthSuccess != null) {
                        onAuthSuccess.run();
                    }
                    state.postValue(AuthSubmissionState.success(result.message));
                } else {
                    state.postValue(AuthSubmissionState.error(result.message));
                }
                break;
            case ACCOUNT_LOCKED:
                session.clear();
                state.postValue(AuthSubmissionState.accountLocked(result.message));
                break;
            case HTTP_ERROR:
                state.postValue(AuthSubmissionState.error(result.message));
                break;
            case NETWORK_ERROR:
            default:
                state.postValue(AuthSubmissionState.networkError());
                break;
        }
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
    }
}
