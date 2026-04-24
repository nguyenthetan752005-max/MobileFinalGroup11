package hcmute.edu.vn.nguyenthetan.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GoogleAuthRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.LoginRequestDto;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityLoginBinding;
import hcmute.edu.vn.nguyenthetan.ui.main.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends ThemedActivity {

    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private MobileApiService mobileApiService;
    private UserSessionStore userSessionStore;
    private GoogleAuthSupport googleAuthSupport;
    private TungTungApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AccountLockUiHandler.attach(this);

        application = (TungTungApplication) getApplication();
        mobileApiService = application.getAppContainer().getMobileApiService();
        userSessionStore = application.getAppContainer().getUserSessionStore();
        googleAuthSupport = new GoogleAuthSupport(this, new GoogleAuthSupport.Callback() {
            @Override
            public void onGoogleIdTokenReceived(@NonNull String idToken) {
                submitGoogleAuth(idToken);
            }

            @Override
            public void onGoogleAuthLoadingChanged(boolean loading) {
                setLoading(loading, R.string.loading_google_sign_in);
            }
        });

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonLogin.setOnClickListener(v -> submitLogin());
        binding.buttonGoogleLogin.setOnClickListener(v -> {
            if (ensureNetworkAvailable()) {
                checkServerAndRun(R.string.loading_checking_server, googleAuthSupport::launch);
            }
        });
        binding.textCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
        binding.textForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void submitLogin() {
        if (!ensureNetworkAvailable()) {
            return;
        }
        String username = binding.inputUsername.getText() == null ? "" : binding.inputUsername.getText().toString().trim();
        String password = binding.inputPassword.getText() == null ? "" : binding.inputPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        checkServerAndRun(R.string.loading_signing_in, () ->
                mobileApiService.login(new LoginRequestDto(username, password)).enqueue(new Callback<AuthResponseDto>() {
                    @Override
                    public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                        setLoading(false, R.string.loading_signing_in);
                        if (!response.isSuccessful() || response.body() == null) {
                            if (AuthResponseHelper.isAccountLocked(response)) {
                                userSessionStore.clear();
                            }
                            Toast.makeText(
                                    LoginActivity.this,
                                    AuthResponseHelper.resolveErrorMessage(response, "Login failed"),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        AuthResponseDto body = response.body();
                        handleAuthSuccess(body, "Login failed.");
                    }

                    @Override
                    public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                        setLoading(false, R.string.loading_signing_in);
                        Toast.makeText(LoginActivity.this, "Login error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void setLoading(boolean loading, int messageRes) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.textLoadingMessage.setText(messageRes);
        binding.buttonLogin.setEnabled(!loading);
        binding.buttonGoogleLogin.setEnabled(!loading);
        binding.inputUsername.setEnabled(!loading);
        binding.inputPassword.setEnabled(!loading);
        binding.textCreateAccount.setEnabled(!loading);
        binding.buttonBack.setEnabled(!loading);
    }

    private void submitGoogleAuth(@NonNull String idToken) {
        if (!ensureNetworkAvailable()) {
            return;
        }
        Log.d(TAG, "Submitting Google auth. tokenLength=" + idToken.length());
        setLoading(true, R.string.loading_signing_in);
        mobileApiService.googleAuth(new GoogleAuthRequestDto(idToken)).enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                setLoading(false, R.string.loading_signing_in);
                Log.d(
                        TAG,
                        "Google auth HTTP response. code=" + response.code()
                                + ", successful=" + response.isSuccessful()
                                + ", errorBody=" + AuthResponseHelper.peekErrorBody(response)
                );
                if (!response.isSuccessful() || response.body() == null) {
                    if (AuthResponseHelper.isAccountLocked(response)) {
                        userSessionStore.clear();
                    }
                    Toast.makeText(
                            LoginActivity.this,
                            AuthResponseHelper.resolveErrorMessage(response, "Google login failed"),
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                Log.d(
                        TAG,
                        "Google auth body received. success=" + response.body().success
                                + ", userId=" + response.body().userId
                                + ", message=" + response.body().message
                );
                handleAuthSuccess(response.body(), "Google login failed.");
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                setLoading(false, R.string.loading_signing_in);
                Log.e(TAG, "Google auth network failure.", throwable);
                Toast.makeText(LoginActivity.this, "Google login error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAuthSuccess(@NonNull AuthResponseDto body, @NonNull String fallbackError) {
        Log.d(
                TAG,
                "Handling auth success payload. success=" + body.success
                        + ", userId=" + body.userId
                        + ", username=" + body.username
                        + ", email=" + body.email
        );
        if (!body.success || body.userId == null) {
            Toast.makeText(this, body.message == null ? fallbackError : body.message, Toast.LENGTH_SHORT).show();
            return;
        }

        userSessionStore.saveUser(body.userId, body.username, body.email, body.token);
        Log.d(TAG, "User session saved. Navigating to MainActivity.");
        Toast.makeText(this, body.message == null ? "Login successful." : body.message, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    private boolean ensureNetworkAvailable() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            return true;
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_network_required_title)
                .setMessage(R.string.dialog_network_required_message)
                .setPositiveButton(R.string.dialog_network_required_positive, null)
                .show();
        return false;
    }

    private void checkServerAndRun(int loadingMessageRes, Runnable onAvailable) {
        setLoading(true, loadingMessageRes);
        application.getAppContainer().checkServerAvailability((available, message) -> {
            if (!available) {
                setLoading(false, loadingMessageRes);
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_server_required_title)
                        .setMessage(message)
                        .setPositiveButton(R.string.dialog_server_required_positive, null)
                        .show();
                return;
            }
            onAvailable.run();
        });
    }
}

