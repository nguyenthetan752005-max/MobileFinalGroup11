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

        userSessionStore.clear();
        ApiErrorDto errorDto = parseErrorBody(response);
        String message = "Phiên đăng nhập không hợp lệ hoặc đã hết hạn, vui lòng đăng nhập lại.";
        
        if (errorDto != null && errorDto.message != null && !errorDto.message.trim().isEmpty()) {
            message = errorDto.message;
        }
        
        SessionEventBus.postAccountLocked(message);
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
