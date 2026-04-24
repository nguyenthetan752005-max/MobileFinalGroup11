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
import hcmute.edu.vn.nguyenthetan.data.remote.dto.RegisterRequestDto;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityRegisterBinding;
import hcmute.edu.vn.nguyenthetan.ui.main.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends ThemedActivity {

    private static final String TAG = "RegisterActivity";

    private ActivityRegisterBinding binding;
    private MobileApiService mobileApiService;
    private UserSessionStore userSessionStore;
    private GoogleAuthSupport googleAuthSupport;
    private TungTungApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
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
        binding.buttonRegister.setOnClickListener(v -> submitRegister());
        binding.buttonGoogleRegister.setOnClickListener(v -> {
            if (ensureNetworkAvailable()) {
                checkServerAndRun(R.string.loading_checking_server, googleAuthSupport::launch);
            }
        });
        binding.textSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void submitRegister() {
        if (!ensureNetworkAvailable()) {
            return;
        }
        String username = text(binding.inputUsername);
        String email = text(binding.inputEmail);
        String password = text(binding.inputPassword);
        String confirm = text(binding.inputConfirmPassword);
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        checkServerAndRun(R.string.loading_creating_account, () ->
                mobileApiService.register(new RegisterRequestDto(username, email, password)).enqueue(new Callback<AuthResponseDto>() {
                    @Override
                    public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                        setLoading(false, R.string.loading_creating_account);
                        if (!response.isSuccessful() || response.body() == null) {
                            if (AuthResponseHelper.isAccountLocked(response)) {
                                userSessionStore.clear();
                            }
                            Toast.makeText(
                                    RegisterActivity.this,
                                    AuthResponseHelper.resolveErrorMessage(response, "Registration failed"),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        AuthResponseDto body = response.body();
                        handleAuthSuccess(body, "Registration failed.");
                    }

                    @Override
                    public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                        setLoading(false, R.string.loading_creating_account);
                        Toast.makeText(RegisterActivity.this, "Register error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void setLoading(boolean loading, int messageRes) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.textLoadingMessage.setText(messageRes);
        binding.buttonRegister.setEnabled(!loading);
        binding.buttonGoogleRegister.setEnabled(!loading);
        binding.inputUsername.setEnabled(!loading);
        binding.inputEmail.setEnabled(!loading);
        binding.inputPassword.setEnabled(!loading);
        binding.inputConfirmPassword.setEnabled(!loading);
        binding.textSignIn.setEnabled(!loading);
        binding.buttonBack.setEnabled(!loading);
    }

    private String text(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void submitGoogleAuth(@NonNull String idToken) {
        if (!ensureNetworkAvailable()) {
            return;
        }
        Log.d(TAG, "Submitting Google register auth. tokenLength=" + idToken.length());
        setLoading(true, R.string.loading_creating_account);
        mobileApiService.googleAuth(new GoogleAuthRequestDto(idToken)).enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                setLoading(false, R.string.loading_creating_account);
                Log.d(
                        TAG,
                        "Google register HTTP response. code=" + response.code()
                                + ", successful=" + response.isSuccessful()
                                + ", errorBody=" + AuthResponseHelper.peekErrorBody(response)
                );
                if (!response.isSuccessful() || response.body() == null) {
                    if (AuthResponseHelper.isAccountLocked(response)) {
                        userSessionStore.clear();
                    }
                    Toast.makeText(
                            RegisterActivity.this,
                            AuthResponseHelper.resolveErrorMessage(response, "Google registration failed"),
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                Log.d(
                        TAG,
                        "Google register body received. success=" + response.body().success
                                + ", userId=" + response.body().userId
                                + ", message=" + response.body().message
                );
                handleAuthSuccess(response.body(), "Google registration failed.");
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                setLoading(false, R.string.loading_creating_account);
                Log.e(TAG, "Google register network failure.", throwable);
                Toast.makeText(RegisterActivity.this, "Google register error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAuthSuccess(@NonNull AuthResponseDto body, @NonNull String fallbackError) {
        Log.d(
                TAG,
                "Handling register auth payload. success=" + body.success
                        + ", userId=" + body.userId
                        + ", username=" + body.username
                        + ", email=" + body.email
        );
        if (!body.success || body.userId == null) {
            Toast.makeText(this, body.message == null ? fallbackError : body.message, Toast.LENGTH_SHORT).show();
            return;
        }

        userSessionStore.saveUser(body.userId, body.username, body.email, body.token);
        Log.d(TAG, "User session saved after register. Navigating to MainActivity.");
        Toast.makeText(this, body.message == null ? "Register successful." : body.message, Toast.LENGTH_SHORT).show();
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

