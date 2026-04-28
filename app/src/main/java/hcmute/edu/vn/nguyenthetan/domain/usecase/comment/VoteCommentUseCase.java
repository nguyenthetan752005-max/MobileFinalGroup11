package hcmute.edu.vn.nguyenthetan.domain.usecase.comment;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.VoteCommentRequestDto;
import retrofit2.Response;

public class VoteCommentUseCase {

    private final MobileApiService api;

    public VoteCommentUseCase(MobileApiService api) {
        this.api = api;
    }

    public boolean execute(long commentId, long userId, boolean isLike) {
        try {
            Response<GenericApiResponseDto> response = api.voteComment(
                    commentId, new VoteCommentRequestDto(userId, isLike)
            ).execute();
            return response.isSuccessful();
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
