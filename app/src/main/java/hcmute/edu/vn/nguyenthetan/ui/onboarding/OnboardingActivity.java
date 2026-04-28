package hcmute.edu.vn.nguyenthetan.ui.onboarding;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.DailyReminderScheduler;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.NotificationPreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ReminderSettingsStore;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.model.profile.ReminderSettings;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityOnboardingBinding;
import hcmute.edu.vn.nguyenthetan.ui.auth.AccountLockUiHandler;
import hcmute.edu.vn.nguyenthetan.ui.auth.AuthSubmissionState;
import hcmute.edu.vn.nguyenthetan.ui.auth.GoogleAuthSupport;
import hcmute.edu.vn.nguyenthetan.ui.auth.LoginActivity;
import hcmute.edu.vn.nguyenthetan.ui.auth.RegisterActivity;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;
import hcmute.edu.vn.nguyenthetan.ui.main.MainActivity;

public class OnboardingActivity extends ThemedActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingViewModel viewModel;
    private UserSessionStore userSessionStore;
    private GoogleAuthSupport googleAuthSupport;
    private TungTungApplication application;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                NotificationPreferenceStore.markPermissionRequested(this);
                NotificationPreferenceStore.setEnabled(this, granted);
                if (granted) {
                    DailyReminderScheduler.apply(this);
                } else {
                    DailyReminderScheduler.cancel(this);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (TungTungApplication) getApplication();
        userSessionStore = application.getAppContainer().getUserSessionStore();

        OnboardingViewModelFactory factory = new OnboardingViewModelFactory(
                application.getAppContainer().getGoogleAuthUseCase(),
                application.getAppContainer().getReminderSettingsUseCase(),
                userSessionStore,
                () -> application.getAppContainer().refreshCurrentUserProfile()
        );
        viewModel = new ViewModelProvider(this, factory).get(OnboardingViewModel.class);
        viewModel.getAuthState().observe(this, this::renderAuthState);
        viewModel.getReminderState().observe(this, this::applyReminderSettings);

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ensureReminderPreferenceInitialized();
        viewModel.loadReminderSettings();
        AccountLockUiHandler.attach(this);
        showIncomingMessageIfNeeded();
        googleAuthSupport = new GoogleAuthSupport(this, new GoogleAuthSupport.Callback() {
            @Override
            public void onGoogleIdTokenReceived(@NonNull String idToken) {
                if (ensureNetworkAvailable()) {
                    viewModel.submitGoogle(idToken);
                }
            }

            @Override
            public void onGoogleAuthLoadingChanged(boolean loading) {
                applyLoadingOverlay(loading, R.string.loading_google_sign_in);
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
                googleAuthSupport.launch();
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

    private void renderAuthState(AuthSubmissionState state) {
        boolean loading = state.status == AuthSubmissionState.Status.LOADING;
        applyLoadingOverlay(loading, R.string.loading_signing_in);
        switch (state.status) {
            case SUCCESS:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty() ? "Login successful." : state.message,
                        Toast.LENGTH_SHORT).show();
                viewModel.acknowledgeAuth();
                openMain();
                break;
            case ERROR:
            case ACCOUNT_LOCKED:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty() ? "Google login failed" : state.message,
                        Toast.LENGTH_SHORT).show();
                viewModel.acknowledgeAuth();
                break;
            case NETWORK_ERROR:
                Toast.makeText(this, R.string.error_connection_generic, Toast.LENGTH_SHORT).show();
                viewModel.acknowledgeAuth();
                break;
            case IDLE:
            case LOADING:
            default:
                break;
        }
    }

    private void applyReminderSettings(ReminderSettings settings) {
        if (settings == null) {
            DailyReminderScheduler.apply(this);
            return;
        }
        ReminderSettingsStore.save(this, settings.dailyReminderEnabled, settings.dailyReminderTime, settings.dailyReminderTimezone);
        DailyReminderScheduler.apply(this);
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void ensureReminderPreferenceInitialized() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationPreferenceStore.initializeDefaultEnabledState(this, true);
            DailyReminderScheduler.apply(this);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationPreferenceStore.initializeDefaultEnabledState(this, true);
            NotificationPreferenceStore.markPermissionRequested(this);
            DailyReminderScheduler.apply(this);
            return;
        }

        if (!NotificationPreferenceStore.hasRequestedPermission(this)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }

        if (!NotificationPreferenceStore.isInitialized(this)) {
            NotificationPreferenceStore.initializeDefaultEnabledState(this, false);
        }
        DailyReminderScheduler.cancel(this);
    }

    private void setButtonsEnabled(boolean enabled) {
        binding.buttonGoogle.setEnabled(enabled);
        binding.buttonSignIn.setEnabled(enabled);
        binding.buttonCreateAccount.setEnabled(enabled);
        binding.buttonGuest.setEnabled(enabled);
    }

    private void applyLoadingOverlay(boolean loading, int messageRes) {
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
        applyLoadingOverlay(true, loadingMessageRes);
        application.getAppContainer().checkServerAvailability((available, message) -> {
            applyLoadingOverlay(false, loadingMessageRes);
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
