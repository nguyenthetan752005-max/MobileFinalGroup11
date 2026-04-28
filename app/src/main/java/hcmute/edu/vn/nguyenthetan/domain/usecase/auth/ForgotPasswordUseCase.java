package hcmute.edu.vn.nguyenthetan.domain.usecase.auth;

import java.io.IOException;
import java.util.Collections;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import retrofit2.Response;

public class ForgotPasswordUseCase {

    private final MobileApiService api;

    public ForgotPasswordUseCase(MobileApiService api) {
        this.api = api;
    }

    public AuthResult execute(String email) {
        try {
            Response<AuthResponseDto> response = api.forgotPassword(Collections.singletonMap("email", email)).execute();
            return AuthUseCaseSupport.toResult(response);
        } catch (IOException | RuntimeException e) {
            return AuthResult.networkError();
        }
    }
}
