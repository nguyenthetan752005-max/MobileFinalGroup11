package hcmute.edu.vn.nguyenthetan.domain.usecase.profile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import retrofit2.Response;

public class ChangePasswordUseCase {

    public static final class Result {
        public final boolean success;
        public final String message;
        public final boolean networkError;
        public final boolean wrongCurrent;

        private Result(boolean success, String message, boolean networkError, boolean wrongCurrent) {
            this.success = success;
            this.message = message;
            this.networkError = networkError;
            this.wrongCurrent = wrongCurrent;
        }

        public static Result success(String message) {
            return new Result(true, message, false, false);
        }

        public static Result failure(String message) {
            return new Result(false, message, false, false);
        }

        public static Result wrongCurrent() {
            return new Result(false, null, false, true);
        }

        public static Result networkError() {
            return new Result(false, null, true, false);
        }
    }

    private final MobileApiService api;

    public ChangePasswordUseCase(MobileApiService api) {
        this.api = api;
    }

    public Result execute(long userId, String currentPassword, String newPassword) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("currentPassword", currentPassword);
            body.put("newPassword", newPassword);
            Response<GenericApiResponseDto> response = api.changePassword(userId, body).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().success
                        ? Result.success(response.body().message)
                        : Result.failure(response.body().message);
            }
            return Result.wrongCurrent();
        } catch (IOException | RuntimeException e) {
            return Result.networkError();
        }
    }
}
