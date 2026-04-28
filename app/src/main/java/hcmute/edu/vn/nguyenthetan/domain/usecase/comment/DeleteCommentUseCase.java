package hcmute.edu.vn.nguyenthetan.domain.usecase.comment;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import retrofit2.Response;

public class DeleteCommentUseCase {

    private final MobileApiService api;

    public DeleteCommentUseCase(MobileApiService api) {
        this.api = api;
    }

    public boolean execute(long commentId, long userId) {
        try {
            Response<GenericApiResponseDto> response = api.deleteComment(commentId, userId).execute();
            return response.isSuccessful();
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
