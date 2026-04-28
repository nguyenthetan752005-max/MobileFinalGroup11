package hcmute.edu.vn.nguyenthetan.ui.profile;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.ChangePasswordUseCase;
import hcmute.edu.vn.nguyenthetan.domain.usecase.profile.UpdateUsernameUseCase;

public class EditProfileViewModel extends ViewModel {

    public enum UpdateKind { USERNAME, PASSWORD }

    public enum Outcome { LOADING, SUCCESS, FAILURE_GENERIC, FAILURE_WRONG_CURRENT, NETWORK_ERROR, IDLE }

    public static final class UpdateState {
        public final UpdateKind kind;
        public final Outcome outcome;
        @Nullable public final String message;

        UpdateState(UpdateKind kind, Outcome outcome, @Nullable String message) {
            this.kind = kind;
            this.outcome = outcome;
            this.message = message;
        }

        static UpdateState idle() {
            return new UpdateState(UpdateKind.USERNAME, Outcome.IDLE, null);
        }
    }

    private final UpdateUsernameUseCase updateUsernameUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UserSessionStore session;
    private final MutableLiveData<UpdateState> state = new MutableLiveData<>(UpdateState.idle());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EditProfileViewModel(
            UpdateUsernameUseCase updateUsernameUseCase,
            ChangePasswordUseCase changePasswordUseCase,
            UserSessionStore session
    ) {
        this.updateUsernameUseCase = updateUsernameUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.session = session;
    }

    public LiveData<UpdateState> getState() {
        return state;
    }

    public void submitUsername(String newUsername) {
        if (isLoading()) return;
        state.setValue(new UpdateState(UpdateKind.USERNAME, Outcome.LOADING, null));
        executor.execute(() -> {
            UpdateUsernameUseCase.Result result = updateUsernameUseCase.execute(session.getUserId(), newUsername);
            if (result.networkError) {
                state.postValue(new UpdateState(UpdateKind.USERNAME, Outcome.NETWORK_ERROR, null));
                return;
            }
            if (result.success) {
                session.saveUser(session.getUserId(), newUsername, session.getEmail(), session.getToken());
                state.postValue(new UpdateState(UpdateKind.USERNAME, Outcome.SUCCESS, result.message));
            } else {
                state.postValue(new UpdateState(UpdateKind.USERNAME, Outcome.FAILURE_GENERIC, result.message));
            }
        });
    }

    public void submitPassword(String currentPassword, String newPassword) {
        if (isLoading()) return;
        state.setValue(new UpdateState(UpdateKind.PASSWORD, Outcome.LOADING, null));
        executor.execute(() -> {
            ChangePasswordUseCase.Result result = changePasswordUseCase.execute(session.getUserId(), currentPassword, newPassword);
            if (result.networkError) {
                state.postValue(new UpdateState(UpdateKind.PASSWORD, Outcome.NETWORK_ERROR, null));
                return;
            }
            if (result.wrongCurrent) {
                state.postValue(new UpdateState(UpdateKind.PASSWORD, Outcome.FAILURE_WRONG_CURRENT, null));
                return;
            }
            if (result.success) {
                // Server forces re-login; clear local session.
                session.clear();
                state.postValue(new UpdateState(UpdateKind.PASSWORD, Outcome.SUCCESS, result.message));
            } else {
                state.postValue(new UpdateState(UpdateKind.PASSWORD, Outcome.FAILURE_GENERIC, result.message));
            }
        });
    }

    public void acknowledge() {
        state.setValue(UpdateState.idle());
    }

    private boolean isLoading() {
        UpdateState current = state.getValue();
        return current != null && current.outcome == Outcome.LOADING;
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
    }
}
