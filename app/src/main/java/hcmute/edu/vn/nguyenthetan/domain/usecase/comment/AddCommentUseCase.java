package hcmute.edu.vn.nguyenthetan.domain.usecase.comment;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.CreateCommentRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapCommentDto;
import retrofit2.Response;

public class AddCommentUseCase {

    private final MobileApiService api;

    public AddCommentUseCase(MobileApiService api) {
        this.api = api;
    }

    public boolean execute(long sentenceId, long authorId, String content, Long parentCommentId) {
        try {
            Response<MobileBootstrapCommentDto> response = api.addComment(
                    new CreateCommentRequestDto(sentenceId, authorId, content, parentCommentId)
            ).execute();
            return response.isSuccessful();
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
