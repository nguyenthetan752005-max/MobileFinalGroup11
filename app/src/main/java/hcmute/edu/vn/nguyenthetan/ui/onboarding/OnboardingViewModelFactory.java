package hcmute.edu.vn.nguyenthetan.ui.onboarding;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.GoogleAuthUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.GetReminderSettingsUseCase;

public class OnboardingViewModelFactory implements ViewModelProvider.Factory {

    private final GoogleAuthUseCase googleAuthUseCase;
    private final GetReminderSettingsUseCase reminderSettingsUseCase;
    private final UserSessionStore session;
    private final Runnable onAuthSuccess;

    public OnboardingViewModelFactory(
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

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(OnboardingViewModel.class)) {
            return (T) new OnboardingViewModel(googleAuthUseCase, reminderSettingsUseCase, session, onAuthSuccess);
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
