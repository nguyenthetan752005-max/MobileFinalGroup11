package hcmute.edu.vn.nguyenthetan.ui.onboarding;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ReminderSettings;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.AuthResult;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.GoogleAuthUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetReminderSettingsUseCase;
import hcmute.edu.vn.nguyenthetan.ui.auth.AuthSubmissionState;

public class OnboardingViewModel extends ViewModel {

    private final GoogleAuthUseCase googleAuthUseCase;
    private final GetReminderSettingsUseCase reminderSettingsUseCase;
    private final UserSessionStore session;
    private final Runnable onAuthSuccess;
    private final MutableLiveData<AuthSubmissionState> authState = new MutableLiveData<>(AuthSubmissionState.idle());
    private final MutableLiveData<ReminderSettings> reminderState = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public OnboardingViewModel(
            GoogleAuthUseCase googleAuthUseCase,
            GetReminderSettingsUseCase reminderSettingsUseCase,
            UserSessionStore session,
            Runnable onAuthSuccess
    ) {
        this.googleAuthUseCase = googleAuthUseCase;
        this.reminderSettingsUseCase = reminderSettingsUseCase;
        this.session = session;
        this.onAuthSuccess = onAuthSuccess;
    }

    public LiveData<AuthSubmissionState> getAuthState() {
        return authState;
    }

    /** Emits the latest reminder settings, or null on failure. */
    public LiveData<ReminderSettings> getReminderState() {
        return reminderState;
    }

    public void submitGoogle(String idToken) {
        AuthSubmissionState current = authState.getValue();
        if (current != null && current.status == AuthSubmissionState.Status.LOADING) return;
        authState.setValue(AuthSubmissionState.loading());
        executor.execute(() -> handleAuth(googleAuthUseCase.execute(idToken)));
    }

    public void acknowledgeAuth() {
        authState.setValue(AuthSubmissionState.idle());
    }

    public void loadReminderSettings() {
        executor.execute(() -> reminderState.postValue(reminderSettingsUseCase.execute()));
    }

    private void handleAuth(AuthResult result) {
        switch (result.status) {
            case SUCCESS:
                if (result.isSuccess()) {
                    session.saveUser(result.userId, result.username, result.email, result.token);
                    if (onAuthSuccess != null) {
                        onAuthSuccess.run();
                    }
                    authState.postValue(AuthSubmissionState.success(result.message));
                } else {
                    authState.postValue(AuthSubmissionState.error(result.message));
                }
                break;
            case ACCOUNT_LOCKED:
                session.clear();
                authState.postValue(AuthSubmissionState.accountLocked(result.message));
                break;
            case HTTP_ERROR:
                authState.postValue(AuthSubmissionState.error(result.message));
                break;
            case NETWORK_ERROR:
            default:
                authState.postValue(AuthSubmissionState.networkError());
                break;
        }
    }

    @Nullable
    public ReminderSettings getCurrentReminder() {
        return reminderState.getValue();
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
    }
}
