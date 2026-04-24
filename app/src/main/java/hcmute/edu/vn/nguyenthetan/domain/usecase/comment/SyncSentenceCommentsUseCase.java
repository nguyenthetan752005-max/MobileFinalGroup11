package hcmute.edu.vn.nguyenthetan.domain.usecase.comment;

import androidx.annotation.NonNull;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.dao.community.CommentDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.CommentEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapCommentDto;
import hcmute.edu.vn.nguyenthetan.data.remote.mapper.RemoteEntityMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncSentenceCommentsUseCase {

    private final MobileApiService apiService;
    private final CommentDao commentDao;

    public SyncSentenceCommentsUseCase(MobileApiService apiService, CommentDao commentDao) {
        this.apiService = apiService;
        this.commentDao = commentDao;
    }

    public void execute(long sentenceId, Runnable onSuccess, Runnable onFailure) {
        apiService.getSentenceComments(sentenceId).enqueue(new Callback<List<MobileBootstrapCommentDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<MobileBootstrapCommentDto>> call, @NonNull Response<List<MobileBootstrapCommentDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        List<CommentEntity> entities = RemoteEntityMapper.toCommentEntities(response.body());
                        commentDao.insertAll(entities);
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    }).start();
                } else if (onFailure != null) {
                    onFailure.run();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MobileBootstrapCommentDto>> call, @NonNull Throwable t) {
                if (onFailure != null) {
                    onFailure.run();
                }
            }
        });
    }
}
