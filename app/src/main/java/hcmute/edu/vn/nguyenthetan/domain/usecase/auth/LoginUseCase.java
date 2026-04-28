package hcmute.edu.vn.nguyenthetan.domain.usecase.auth;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.LoginRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.support.AuthResponseParser;
import retrofit2.Response;

public class LoginUseCase {

    private final MobileApiService api;

    public LoginUseCase(MobileApiService api) {
        this.api = api;
    }

    public AuthResult execute(String username, String password) {
        try {
            Response<AuthResponseDto> response = api.login(new LoginRequestDto(username, password)).execute();
            return AuthUseCaseSupport.toResult(response);
        } catch (IOException | RuntimeException e) {
            return AuthResult.networkError();
        }
    }
}
