package hcmute.edu.vn.nguyenthetan.core;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import hcmute.edu.vn.nguyenthetan.data.remote.dto.ApiErrorDto;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class AccountLockInterceptor implements Interceptor {

    private static final Gson GSON = new Gson();
    private static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

    private final UserSessionStore userSessionStore;

    public AccountLockInterceptor(UserSessionStore userSessionStore) {
        this.userSessionStore = userSessionStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (response.code() != 401) {
            return response;
        }

        ApiErrorDto errorDto = parseErrorBody(response);
        if (errorDto == null || errorDto.code == null) {
            return response;
        }

        String code = errorDto.code.toUpperCase();
        if ("ACCOUNT_LOCKED".equals(code) || "ACCOUNT_BANNED".equals(code) || "ACCOUNT_INACTIVE".equals(code) ||
            "TOKEN_EXPIRED".equals(code) || "TOKEN_REVOKED".equals(code) || "INVALID_TOKEN".equals(code)) {
            userSessionStore.clear();
            String message = errorDto.message == null || errorDto.message.trim().isEmpty()
                    ? "Phiên đăng nhập không hợp lệ hoặc tài khoản đã bị khóa."
                    : errorDto.message;
            SessionEventBus.postAccountLocked(message);
        }

        return response;
    }

    private ApiErrorDto parseErrorBody(Response response) {
        try {
            ResponseBody errorBody = response.peekBody(Long.MAX_VALUE);
            if (errorBody == null) {
                return null;
            }
            BufferedSource source = errorBody.source();
            source.request(Long.MAX_VALUE);
            String raw = source.getBuffer().clone().readString(StandardCharsets.UTF_8);
            if (raw.trim().isEmpty()) {
                return null;
            }
            return GSON.fromJson(raw, ApiErrorDto.class);
        } catch (RuntimeException | IOException ignored) {
            return null;
        }
    }
}
