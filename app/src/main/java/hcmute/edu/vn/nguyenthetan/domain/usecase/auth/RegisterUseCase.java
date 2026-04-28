package hcmute.edu.vn.nguyenthetan.domain.usecase.auth;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.RegisterRequestDto;
import retrofit2.Response;

public class RegisterUseCase {

    private final MobileApiService api;

    public RegisterUseCase(MobileApiService api) {
        this.api = api;
    }

    public AuthResult execute(String username, String email, String password) {
        try {
            Response<AuthResponseDto> response = api.register(new RegisterRequestDto(username, email, password)).execute();
            return AuthUseCaseSupport.toResult(response);
        } catch (IOException | RuntimeException e) {
            return AuthResult.networkError();
        }
    }
}
