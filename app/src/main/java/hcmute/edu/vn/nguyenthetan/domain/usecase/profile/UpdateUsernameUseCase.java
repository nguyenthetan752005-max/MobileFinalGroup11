package hcmute.edu.vn.nguyenthetan.domain.usecase.profile;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.UsernameUpdateRequestDto;
import retrofit2.Response;

public class UpdateUsernameUseCase {

    public static final class Result {
        public final boolean success;
        public final String message;
        public final boolean networkError;

        private Result(boolean success, String message, boolean networkError) {
            this.success = success;
            this.message = message;
            this.networkError = networkError;
        }

        public static Result of(boolean success, String message) {
            return new Result(success, message, false);
        }

        public static Result networkError() {
            return new Result(false, null, true);
        }
    }

    private final MobileApiService api;

    public UpdateUsernameUseCase(MobileApiService api) {
        this.api = api;
    }

    public Result execute(long userId, String newUsername) {
        try {
            Response<GenericApiResponseDto> response = api.updateUsername(userId, new UsernameUpdateRequestDto(newUsername)).execute();
            if (response.isSuccessful() && response.body() != null) {
                return Result.of(response.body().success, response.body().message);
            }
            return Result.of(false, null);
        } catch (IOException | RuntimeException e) {
            return Result.networkError();
        }
    }
}
