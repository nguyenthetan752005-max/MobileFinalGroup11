package hcmute.edu.vn.nguyenthetan.domain.usecase.auth;

import androidx.annotation.NonNull;

import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.support.AuthResponseParser;
import retrofit2.Response;

/**
 * Shared mapping helpers for auth use-cases. Keeps response interpretation in
 * one place so each use-case stays focused on its own endpoint.
 */
final class AuthUseCaseSupport {

    private AuthUseCaseSupport() {
    }

    @NonNull
    static AuthResult toResult(@NonNull Response<AuthResponseDto> response) {
        if (response.isSuccessful() && response.body() != null) {
            AuthResponseDto dto = response.body();
            return AuthResult.success(dto.userId, dto.username, dto.email,
                    dto.token, dto.message);
        }
        boolean accountLocked = AuthResponseParser.isAccountLocked(response);
        String message = AuthResponseParser.resolveErrorMessage(response, "");
        if (accountLocked) {
            return AuthResult.accountLocked(message);
        }
        return AuthResult.httpError(message);
    }
}
