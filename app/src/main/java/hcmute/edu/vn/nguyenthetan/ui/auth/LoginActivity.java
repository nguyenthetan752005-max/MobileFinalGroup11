package hcmute.edu.vn.nguyenthetan.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityLoginBinding;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;
import hcmute.edu.vn.nguyenthetan.ui.main.MainActivity;

public class LoginActivity extends ThemedActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private GoogleAuthSupport googleAuthSupport;
    private TungTungApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AccountLockUiHandler.attach(this);

        application = (TungTungApplication) getApplication();
        LoginViewModelFactory factory = new LoginViewModelFactory(
                application.getAppContainer().getLoginUseCase(),
                application.getAppContainer().getGoogleAuthUseCase(),
                application.getAppContainer().getUserSessionStore(),
                () -> application.getAppContainer().refreshCurrentUserProfile()
        );
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);
        viewModel.getState().observe(this, this::renderState);

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

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonLogin.setOnClickListener(v -> submitLogin());
        binding.buttonGoogleLogin.setOnClickListener(v -> {
            if (ensureNetworkAvailable()) {
                googleAuthSupport.launch();
            }
        });
        binding.textCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
        binding.textForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );
    }

    private void submitLogin() {
        if (!ensureNetworkAvailable()) return;
        String username = textOf(binding.inputUsername.getText());
        String password = binding.inputPassword.getText() == null ? "" : binding.inputPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.submitLogin(username, password);
    }

    private void renderState(AuthSubmissionState state) {
        boolean loading = state.status == AuthSubmissionState.Status.LOADING;
        applyLoadingOverlay(loading, R.string.loading_signing_in);
        switch (state.status) {
            case SUCCESS:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty() ? "Login successful." : state.message,
                        Toast.LENGTH_SHORT).show();
                viewModel.acknowledge();
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
                break;
            case ERROR:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty()
                                ? getString(R.string.error_auth_login_failed)
                                : state.message,
                        Toast.LENGTH_SHORT).show();
                viewModel.acknowledge();
                break;
            case ACCOUNT_LOCKED:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty()
                                ? getString(R.string.error_auth_login_failed)
                                : state.message,
                        Toast.LENGTH_LONG).show();
                viewModel.acknowledge();
                break;
            case NETWORK_ERROR:
                Toast.makeText(this, R.string.error_connection_generic, Toast.LENGTH_SHORT).show();
                viewModel.acknowledge();
                break;
            case IDLE:
            case LOADING:
            default:
                break;
        }
    }

    private void applyLoadingOverlay(boolean loading, int messageRes) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.textLoadingMessage.setText(messageRes);
        binding.buttonLogin.setEnabled(!loading);
        binding.buttonGoogleLogin.setEnabled(!loading);
        binding.inputUsername.setEnabled(!loading);
        binding.inputPassword.setEnabled(!loading);
        binding.textCreateAccount.setEnabled(!loading);
        binding.buttonBack.setEnabled(!loading);
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

    private String textOf(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }
}
