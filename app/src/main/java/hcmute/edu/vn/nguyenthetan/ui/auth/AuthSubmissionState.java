package hcmute.edu.vn.nguyenthetan.ui.auth;

import androidx.annotation.Nullable;

/**
 * Sealed-style ViewModel state for any auth submission flow (login, register,
 * Google auth, forgot password). The Activity observes this and renders loading
 * UI / toasts / navigation, then calls {@code acknowledge()} on the VM after
 * handling a terminal state so config changes don't replay the side-effects.
 */
public final class AuthSubmissionState {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR,
        ACCOUNT_LOCKED,
        NETWORK_ERROR
    }

    public final Status status;
    @Nullable
    public final String message;

    private AuthSubmissionState(Status status, @Nullable String message) {
        this.status = status;
        this.message = message;
    }

    public static AuthSubmissionState idle() {
        return new AuthSubmissionState(Status.IDLE, null);
    }

    public static AuthSubmissionState loading() {
        return new AuthSubmissionState(Status.LOADING, null);
    }

    public static AuthSubmissionState success(@Nullable String message) {
        return new AuthSubmissionState(Status.SUCCESS, message);
    }

    public static AuthSubmissionState error(@Nullable String message) {
        return new AuthSubmissionState(Status.ERROR, message);
    }

    public static AuthSubmissionState accountLocked(@Nullable String message) {
        return new AuthSubmissionState(Status.ACCOUNT_LOCKED, message);
    }

    public static AuthSubmissionState networkError() {
        return new AuthSubmissionState(Status.NETWORK_ERROR, null);
    }
}
