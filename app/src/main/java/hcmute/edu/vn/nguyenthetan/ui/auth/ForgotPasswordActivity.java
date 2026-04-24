package hcmute.edu.vn.nguyenthetan.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityForgotPasswordBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends ThemedActivity {

    private ActivityForgotPasswordBinding binding;
    private MobileApiService mobileApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mobileApiService = ((TungTungApplication) getApplication()).getAppContainer().getMobileApiService();

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonSubmit.setOnClickListener(v -> submitForgotPassword());
    }

    private void submitForgotPassword() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Vui lòng kiểm tra kết nối mạng", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = binding.inputEmail.getText() == null ? "" : binding.inputEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        mobileApiService.forgotPassword(request).enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(Call<AuthResponseDto> call, Response<AuthResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponseDto body = response.body();
                    Toast.makeText(ForgotPasswordActivity.this, body.message != null ? body.message : "Đã gửi email", Toast.LENGTH_LONG).show();
                    if (body.success) {
                        finish();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Lỗi từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponseDto> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonSubmit.setEnabled(!loading);
        binding.inputEmail.setEnabled(!loading);
        binding.buttonBack.setEnabled(!loading);
    }
}

