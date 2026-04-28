package hcmute.edu.vn.nguyenthetan.domain.usecase.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Immutable outcome of an auth request. Contains the parsed response
 * fields directly so the ViewModel layer never needs to touch DTOs.
 */
public final class AuthResult {

    public enum Status {
        SUCCESS,
        HTTP_ERROR,
        ACCOUNT_LOCKED,
        NETWORK_ERROR
    }

    @NonNull
    public final Status status;
    @Nullable
    public final String message;

    // Auth payload — only populated on SUCCESS
    @Nullable public final Long userId;
    @Nullable public final String username;
    @Nullable public final String email;
    @Nullable public final String token;

    private AuthResult(@NonNull Status status, @Nullable String message,
                       @Nullable Long userId, @Nullable String username,
                       @Nullable String email, @Nullable String token) {
        this.status = status;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.token = token;
    }

    public static AuthResult success(@NonNull Long userId, @Nullable String username,
                                     @Nullable String email, @Nullable String token,
                                     @Nullable String message) {
        return new AuthResult(Status.SUCCESS, message, userId, username, email, token);
    }

    public static AuthResult httpError(@Nullable String message) {
        return new AuthResult(Status.HTTP_ERROR, message, null, null, null, null);
    }

    public static AuthResult accountLocked(@Nullable String message) {
        return new AuthResult(Status.ACCOUNT_LOCKED, message, null, null, null, null);
    }

    public static AuthResult networkError() {
        return new AuthResult(Status.NETWORK_ERROR, null, null, null, null, null);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS && userId != null;
    }
}
