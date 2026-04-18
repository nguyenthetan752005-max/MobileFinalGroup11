package hcmute.edu.vn.nguyenthetan.ui.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Response;

public final class AuthResponseHelper {

    private static final Gson GSON = new Gson();
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

    private AuthResponseHelper() {
    }

    @Nullable
    public static AuthResponseDto parseErrorBody(@NonNull Response<?> response) {
        String raw = peekErrorBody(response);
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        try {
            return GSON.fromJson(raw, AuthResponseDto.class);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @Nullable
    public static String peekErrorBody(@NonNull Response<?> response) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) {
            return null;
        }

        try {
            BufferedSource source = errorBody.source();
            source.request(Long.MAX_VALUE);
            return source.getBuffer().clone().readString(StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    @NonNull
    public static String resolveErrorMessage(
            @NonNull Response<?> response,
            @NonNull String fallbackMessage
    ) {
        AuthResponseDto errorDto = parseErrorBody(response);
        if (errorDto != null && errorDto.message != null && !errorDto.message.trim().isEmpty()) {
            return errorDto.message;
        }
        return fallbackMessage + ": HTTP " + response.code();
    }

    public static boolean isAccountLocked(@NonNull Response<?> response) {
        AuthResponseDto errorDto = parseErrorBody(response);
        return errorDto != null
                && errorDto.code != null
                && ACCOUNT_LOCKED.equalsIgnoreCase(errorDto.code);
    }
}
