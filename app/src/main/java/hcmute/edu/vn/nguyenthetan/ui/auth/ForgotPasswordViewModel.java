package hcmute.edu.vn.nguyenthetan.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.AuthResult;
import hcmute.edu.vn.nguyenthetan.domain.usecase.auth.ForgotPasswordUseCase;

public class ForgotPasswordViewModel extends ViewModel {

    private final ForgotPasswordUseCase useCase;
    private final MutableLiveData<AuthSubmissionState> state = new MutableLiveData<>(AuthSubmissionState.idle());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ForgotPasswordViewModel(ForgotPasswordUseCase useCase) {
        this.useCase = useCase;
    }

    public LiveData<AuthSubmissionState> getState() {
        return state;
    }

    public void submit(String email) {
        AuthSubmissionState current = state.getValue();
        if (current != null && current.status == AuthSubmissionState.Status.LOADING) return;
        state.setValue(AuthSubmissionState.loading());
        executor.execute(() -> {
            AuthResult result = useCase.execute(email);
            switch (result.status) {
                case SUCCESS:
                    if (result.isSuccess()) {
                        state.postValue(AuthSubmissionState.success(result.message));
                    } else {
                        state.postValue(AuthSubmissionState.error(result.message));
                    }
                    break;
                case HTTP_ERROR:
                case ACCOUNT_LOCKED:
                    state.postValue(AuthSubmissionState.error(result.message));
                    break;
                case NETWORK_ERROR:
                default:
                    state.postValue(AuthSubmissionState.networkError());
                    break;
            }
        });
    }

    public void acknowledge() {
        state.setValue(AuthSubmissionState.idle());
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
    }
}
