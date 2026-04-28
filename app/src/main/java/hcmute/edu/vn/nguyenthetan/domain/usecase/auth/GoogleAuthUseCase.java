package hcmute.edu.vn.nguyenthetan.domain.usecase.auth;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GoogleAuthRequestDto;
import retrofit2.Response;

public class GoogleAuthUseCase {

    private final MobileApiService api;

    public GoogleAuthUseCase(MobileApiService api) {
        this.api = api;
    }

    public AuthResult execute(String idToken) {
        try {
            Response<AuthResponseDto> response = api.googleAuth(new GoogleAuthRequestDto(idToken)).execute();
            return AuthUseCaseSupport.toResult(response);
        } catch (IOException | RuntimeException e) {
            return AuthResult.networkError();
        }
    }
}
