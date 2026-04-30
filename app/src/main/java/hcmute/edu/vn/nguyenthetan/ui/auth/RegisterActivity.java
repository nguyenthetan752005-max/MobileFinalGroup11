package hcmute.edu.vn.nguyenthetan.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityRegisterBinding;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;
import hcmute.edu.vn.nguyenthetan.ui.main.MainActivity;

public class RegisterActivity extends ThemedActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;
    private GoogleAuthSupport googleAuthSupport;
    private TungTungApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AccountLockUiHandler.attach(this);

        application = (TungTungApplication) getApplication();
        RegisterViewModelFactory factory = new RegisterViewModelFactory(
                application.getAppContainer().getRegisterUseCase(),
                application.getAppContainer().getGoogleAuthUseCase(),
                application.getAppContainer().getUserSessionStore(),
                () -> application.getAppContainer().refreshAfterAuthSuccess()
        );
        viewModel = new ViewModelProvider(this, factory).get(RegisterViewModel.class);
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
        binding.buttonRegister.setOnClickListener(v -> submitRegister());
        binding.buttonGoogleRegister.setOnClickListener(v -> {
            if (ensureNetworkAvailable()) {
                googleAuthSupport.launch();
            }
        });
        binding.textSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void submitRegister() {
        if (!ensureNetworkAvailable()) return;
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
        viewModel.submitRegister(username, email, password);
    }

    private void renderState(AuthSubmissionState state) {
        boolean loading = state.status == AuthSubmissionState.Status.LOADING;
        applyLoadingOverlay(loading, R.string.loading_creating_account);
        switch (state.status) {
            case SUCCESS:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty() ? "Register successful." : state.message,
                        Toast.LENGTH_SHORT).show();
                viewModel.acknowledge();
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
                break;
            case ERROR:
            case ACCOUNT_LOCKED:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty()
                                ? getString(R.string.error_auth_register_failed)
                                : state.message,
                        Toast.LENGTH_SHORT).show();
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
        binding.buttonRegister.setEnabled(!loading);
        binding.buttonGoogleRegister.setEnabled(!loading);
        binding.inputUsername.setEnabled(!loading);
        binding.inputEmail.setEnabled(!loading);
        binding.inputPassword.setEnabled(!loading);
        binding.inputConfirmPassword.setEnabled(!loading);
        binding.textSignIn.setEnabled(!loading);
        binding.buttonBack.setEnabled(!loading);
    }

    private String text(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
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
}
