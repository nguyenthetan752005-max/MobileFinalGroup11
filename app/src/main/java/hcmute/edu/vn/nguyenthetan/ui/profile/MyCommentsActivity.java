package hcmute.edu.vn.nguyenthetan.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.NetworkUtils;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapCommentDto;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityMyCommentsBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCommentsActivity extends AppCompatActivity {

    private ActivityMyCommentsBinding binding;
    private MobileApiService mobileApiService;
    private UserSessionStore userSessionStore;
    private MyCommentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyCommentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mobileApiService = ((TungTungApplication) getApplication()).getAppContainer().getMobileApiService();
        userSessionStore = ((TungTungApplication) getApplication()).getAppContainer().getUserSessionStore();

        if (userSessionStore.getUserId() == -1) {
            finish();
            return;
        }

        binding.buttonBack.setOnClickListener(v -> finish());
        
        adapter = new MyCommentsAdapter();
        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComments.setAdapter(adapter);

        loadComments();
    }

    private void loadComments() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Vui lòng kiểm tra kết nối mạng", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.viewEmpty.setVisibility(View.GONE);
        binding.recyclerComments.setVisibility(View.GONE);

        mobileApiService.getUserComments(userSessionStore.getUserId()).enqueue(new Callback<List<MobileBootstrapCommentDto>>() {
            @Override
            public void onResponse(Call<List<MobileBootstrapCommentDto>> call, Response<List<MobileBootstrapCommentDto>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<MobileBootstrapCommentDto> comments = response.body();
                    adapter.submitList(comments);
                    if (comments.isEmpty()) {
                        binding.viewEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerComments.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(MyCommentsActivity.this, "Không thể tải bình luận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MobileBootstrapCommentDto>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyCommentsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
