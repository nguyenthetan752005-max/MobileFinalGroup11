package hcmute.edu.vn.nguyenthetan.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityForgotPasswordBinding;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;

public class ForgotPasswordActivity extends ThemedActivity {

    private ActivityForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TungTungApplication application = (TungTungApplication) getApplication();
        ForgotPasswordViewModelFactory factory = new ForgotPasswordViewModelFactory(
                application.getAppContainer().getForgotPasswordUseCase()
        );
        viewModel = new ViewModelProvider(this, factory).get(ForgotPasswordViewModel.class);
        viewModel.getState().observe(this, this::renderState);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Vui lòng kiểm tra kết nối mạng", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = binding.inputEmail.getText() == null ? "" : binding.inputEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.submit(email);
    }

    private void renderState(AuthSubmissionState state) {
        boolean loading = state.status == AuthSubmissionState.Status.LOADING;
        setLoading(loading);
        switch (state.status) {
            case SUCCESS:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty() ? "Đã gửi email" : state.message,
                        Toast.LENGTH_LONG).show();
                viewModel.acknowledge();
                finish();
                break;
            case ERROR:
            case ACCOUNT_LOCKED:
                Toast.makeText(this,
                        state.message == null || state.message.isEmpty() ? "Lỗi từ server" : state.message,
                        Toast.LENGTH_SHORT).show();
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

    private void setLoading(boolean loading) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonSubmit.setEnabled(!loading);
        binding.inputEmail.setEnabled(!loading);
        binding.buttonBack.setEnabled(!loading);
    }
}
