package hcmute.edu.vn.nguyenthetan.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityEditProfileBinding;
import hcmute.edu.vn.nguyenthetan.ui.auth.LoginActivity;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;

public class EditProfileActivity extends ThemedActivity {

    private ActivityEditProfileBinding binding;
    private EditProfileViewModel viewModel;
    private UserSessionStore userSessionStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TungTungApplication application = (TungTungApplication) getApplication();
        userSessionStore = application.getAppContainer().getUserSessionStore();

        if (userSessionStore.getUserId() == -1) {
            finish();
            return;
        }

        EditProfileViewModelFactory factory = new EditProfileViewModelFactory(
                application.getAppContainer().getUpdateUsernameUseCase(),
                application.getAppContainer().getChangePasswordUseCase(),
                userSessionStore
        );
        viewModel = new ViewModelProvider(this, factory).get(EditProfileViewModel.class);
        viewModel.getState().observe(this, this::renderState);

        binding.inputUsername.setText(userSessionStore.getUsername());

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonUpdateUsername.setOnClickListener(v -> submitUpdateUsername());
        binding.buttonUpdatePassword.setOnClickListener(v -> submitUpdatePassword());
    }

    private void submitUpdateUsername() {
        if (!ensureNetwork()) return;
        String newUsername = binding.inputUsername.getText() == null ? "" : binding.inputUsername.getText().toString().trim();
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.submitUsername(newUsername);
    }

    private void submitUpdatePassword() {
        if (!ensureNetwork()) return;
        String currentPassword = binding.inputCurrentPassword.getText() == null ? "" : binding.inputCurrentPassword.getText().toString();
        String newPassword = binding.inputNewPassword.getText() == null ? "" : binding.inputNewPassword.getText().toString();
        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.submitPassword(currentPassword, newPassword);
    }

    private void renderState(EditProfileViewModel.UpdateState state) {
        boolean loading = state.outcome == EditProfileViewModel.Outcome.LOADING;
        setLoading(loading);
        switch (state.outcome) {
            case SUCCESS:
                if (state.kind == EditProfileViewModel.UpdateKind.USERNAME) {
                    Toast.makeText(this, "Cập nhật tên thành công", Toast.LENGTH_SHORT).show();
                    viewModel.acknowledge();
                } else {
                    Toast.makeText(this, "Cập nhật mật khẩu thành công. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    viewModel.acknowledge();
                    startActivity(new Intent(this, LoginActivity.class));
                    finishAffinity();
                }
                break;
            case FAILURE_GENERIC:
                Toast.makeText(this,
                        state.message != null && !state.message.isEmpty() ? state.message : "Lỗi",
                        Toast.LENGTH_SHORT).show();
                viewModel.acknowledge();
                break;
            case FAILURE_WRONG_CURRENT:
                Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                viewModel.acknowledge();
                break;
            case NETWORK_ERROR:
                Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                viewModel.acknowledge();
                break;
            case IDLE:
            case LOADING:
            default:
                break;
        }
    }

    private boolean ensureNetwork() {
        if (NetworkUtils.isNetworkAvailable(this)) return true;
        Toast.makeText(this, "Vui lòng kiểm tra kết nối mạng", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void setLoading(boolean loading) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonUpdateUsername.setEnabled(!loading);
        binding.buttonUpdatePassword.setEnabled(!loading);
        binding.inputUsername.setEnabled(!loading);
        binding.inputCurrentPassword.setEnabled(!loading);
        binding.inputNewPassword.setEnabled(!loading);
        binding.buttonBack.setEnabled(!loading);
    }
}
