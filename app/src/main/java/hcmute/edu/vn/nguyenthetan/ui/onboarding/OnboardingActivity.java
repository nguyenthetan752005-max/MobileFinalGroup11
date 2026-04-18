package hcmute.edu.vn.nguyenthetan.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GoogleAuthRequestDto;
import hcmute.edu.vn.nguyenthetan.ui.main.MainActivity;
import hcmute.edu.vn.nguyenthetan.ui.auth.LoginActivity;
import hcmute.edu.vn.nguyenthetan.ui.auth.RegisterActivity;
import hcmute.edu.vn.nguyenthetan.ui.auth.AuthResponseHelper;
import hcmute.edu.vn.nguyenthetan.ui.auth.GoogleAuthSupport;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityOnboardingBinding;
import hcmute.edu.vn.nguyenthetan.ui.auth.AccountLockUiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingActivity extends AppCompatActivity {

    private static final String TAG = "OnboardingActivity";

    private ActivityOnboardingBinding binding;
    private MobileApiService mobileApiService;
    private UserSessionStore userSessionStore;
    private GoogleAuthSupport googleAuthSupport;
    private TungTungApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (TungTungApplication) getApplication();
        userSessionStore = application.getAppContainer().getUserSessionStore();
        mobileApiService = application.getAppContainer().getMobileApiService();
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AccountLockUiHandler.attach(this);
        showIncomingMessageIfNeeded();
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
        if (userSessionStore.isLoggedIn() && NetworkUtils.isNetworkAvailable(this)) {
            checkServerAndRun(R.string.loading_checking_server, this::openMain);
        }

        binding.buttonGuest.setOnClickListener(v -> {
            if (ensureNetworkAvailable()) {
                checkServerAndRun(R.string.loading_checking_server, this::openMain);
            }
        });
        binding.buttonGoogle.setOnClickListener(v -> {
            if (ensureNetworkAvailable()) {
                checkServerAndRun(R.string.loading_checking_server, googleAuthSupport::launch);
            }
        });
        binding.buttonSignIn.setOnClickListener(v -> {
            if (ensureNetworkAvailable()) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
        binding.buttonCreateAccount.setOnClickListener(v -> {
            if (ensureNetworkAvailable()) {
                startActivity(new Intent(this, RegisterActivity.class));
            }
        });
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void submitGoogleAuth(@NonNull String idToken) {
        if (!ensureNetworkAvailable()) {
            return;
        }
        Log.d(TAG, "Submitting Google auth from onboarding. tokenLength=" + idToken.length());
        setLoading(true, R.string.loading_signing_in);
        mobileApiService.googleAuth(new GoogleAuthRequestDto(idToken)).enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                setLoading(false, R.string.loading_signing_in);
                Log.d(
                        TAG,
                        "Onboarding Google auth HTTP response. code=" + response.code()
                                + ", successful=" + response.isSuccessful()
                                + ", errorBody=" + AuthResponseHelper.peekErrorBody(response)
                );
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            OnboardingActivity.this,
                            AuthResponseHelper.resolveErrorMessage(response, "Google login failed"),
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                AuthResponseDto body = response.body();
                Log.d(
                        TAG,
                        "Onboarding Google auth body received. success=" + body.success
                                + ", userId=" + body.userId
                                + ", message=" + body.message
                );
                if (!body.success || body.userId == null) {
                    Toast.makeText(OnboardingActivity.this, body.message == null ? "Google login failed." : body.message, Toast.LENGTH_SHORT).show();
                    return;
                }

                userSessionStore.saveUser(body.userId, body.username, body.email, body.token);
                Log.d(TAG, "User session saved from onboarding. Opening MainActivity.");
                Toast.makeText(OnboardingActivity.this, body.message == null ? "Login successful." : body.message, Toast.LENGTH_SHORT).show();
                openMain();
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable throwable) {
                setLoading(false, R.string.loading_signing_in);
                Log.e(TAG, "Onboarding Google auth network failure.", throwable);
                Toast.makeText(OnboardingActivity.this, "Google login error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        binding.buttonGoogle.setEnabled(enabled);
        binding.buttonSignIn.setEnabled(enabled);
        binding.buttonCreateAccount.setEnabled(enabled);
        binding.buttonGuest.setEnabled(enabled);
    }

    private void setLoading(boolean loading, int messageRes) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.textLoadingMessage.setText(messageRes);
        setButtonsEnabled(!loading);
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
            setLoading(false, loadingMessageRes);
            if (available) {
                onAvailable.run();
                return;
            }
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_server_required_title)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_server_required_positive, null)
                    .show();
        });
    }

    private void showIncomingMessageIfNeeded() {
        String message = getIntent().getStringExtra(AccountLockUiHandler.EXTRA_ACCOUNT_LOCKED_MESSAGE);
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_account_locked_title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_account_locked_positive, null)
                .show();
    }
}
