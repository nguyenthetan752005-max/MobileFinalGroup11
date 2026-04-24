package hcmute.edu.vn.nguyenthetan.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityEditProfileBinding;
import hcmute.edu.vn.nguyenthetan.ui.auth.LoginActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends ThemedActivity {

    private ActivityEditProfileBinding binding;
    private MobileApiService mobileApiService;
    private UserSessionStore userSessionStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mobileApiService = ((TungTungApplication) getApplication()).getAppContainer().getMobileApiService();
        userSessionStore = ((TungTungApplication) getApplication()).getAppContainer().getUserSessionStore();

        if (userSessionStore.getUserId() == -1) {
            finish();
            return;
        }

        binding.inputUsername.setText(userSessionStore.getUsername());

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonUpdateUsername.setOnClickListener(v -> submitUpdateUsername());
        binding.buttonUpdatePassword.setOnClickListener(v -> submitUpdatePassword());
    }

    private void submitUpdateUsername() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Vui lòng kiểm tra kết nối mạng", Toast.LENGTH_SHORT).show();
            return;
        }

        String newUsername = binding.inputUsername.getText() == null ? "" : binding.inputUsername.getText().toString().trim();
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        hcmute.edu.vn.nguyenthetan.data.remote.dto.UsernameUpdateRequestDto request = new hcmute.edu.vn.nguyenthetan.data.remote.dto.UsernameUpdateRequestDto(newUsername);

        mobileApiService.updateUsername(userSessionStore.getUserId(), request).enqueue(new Callback<GenericApiResponseDto>() {
            @Override
            public void onResponse(Call<GenericApiResponseDto> call, Response<GenericApiResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().success) {
                        Toast.makeText(EditProfileActivity.this, "Cập nhật tên thành công", Toast.LENGTH_SHORT).show();
                        // Update session
                        userSessionStore.saveUser(
                                userSessionStore.getUserId(),
                                newUsername,
                                userSessionStore.getEmail(),
                                userSessionStore.getToken()
                        );
                    } else {
                        Toast.makeText(EditProfileActivity.this, response.body().message != null ? response.body().message : "Lỗi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Lỗi từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericApiResponseDto> call, Throwable t) {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitUpdatePassword() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Vui lòng kiểm tra kết nối mạng", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentPassword = binding.inputCurrentPassword.getText() == null ? "" : binding.inputCurrentPassword.getText().toString();
        String newPassword = binding.inputNewPassword.getText() == null ? "" : binding.inputNewPassword.getText().toString();

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", currentPassword);
        request.put("newPassword", newPassword);

        mobileApiService.changePassword(userSessionStore.getUserId(), request).enqueue(new Callback<GenericApiResponseDto>() {
            @Override
            public void onResponse(Call<GenericApiResponseDto> call, Response<GenericApiResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().success) {
                        Toast.makeText(EditProfileActivity.this, "Cập nhật mật khẩu thành công. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                        userSessionStore.clear();
                        startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
                        finishAffinity();
                    } else {
                        Toast.makeText(EditProfileActivity.this, response.body().message != null ? response.body().message : "Lỗi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericApiResponseDto> call, Throwable t) {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
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

