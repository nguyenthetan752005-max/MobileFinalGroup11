package hcmute.edu.vn.nguyenthetan.domain.usecase.comment;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapCommentDto;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Unit tests for comment use cases:
 * {@link AddCommentUseCase}, {@link VoteCommentUseCase}, {@link DeleteCommentUseCase}.
 */
public class CommentUseCasesTest {

    private MobileApiService api;

    @Before
    public void setUp() {
        api = mock(MobileApiService.class);
    }

    // ────── AddCommentUseCase ──────

    @Test
    public void addComment_success() throws Exception {
        AddCommentUseCase useCase = new AddCommentUseCase(api);
        Call<MobileBootstrapCommentDto> call = mockCall(Response.success(new MobileBootstrapCommentDto()));
        when(api.addComment(any())).thenReturn(call);

        assertTrue(useCase.execute(1L, 10L, "Great lesson!", null));
    }

    @Test
    public void addComment_replyToParent_success() throws Exception {
        AddCommentUseCase useCase = new AddCommentUseCase(api);
        Call<MobileBootstrapCommentDto> call = mockCall(Response.success(new MobileBootstrapCommentDto()));
        when(api.addComment(any())).thenReturn(call);

        assertTrue(useCase.execute(1L, 10L, "Reply text", 5L));
    }

    @Test
    public void addComment_httpError_returnsFalse() throws Exception {
        AddCommentUseCase useCase = new AddCommentUseCase(api);
        Call<MobileBootstrapCommentDto> call = mockCall(
                Response.error(400, ResponseBody.create(null, "")));
        when(api.addComment(any())).thenReturn(call);

        assertFalse(useCase.execute(1L, 10L, "text", null));
    }

    @Test
    public void addComment_networkError_returnsFalse() throws Exception {
        AddCommentUseCase useCase = new AddCommentUseCase(api);
        Call<MobileBootstrapCommentDto> call = mockCallThrows(new IOException("timeout"));
        when(api.addComment(any())).thenReturn(call);

        assertFalse(useCase.execute(1L, 10L, "text", null));
    }

    // ────── VoteCommentUseCase ──────

    @Test
    public void voteComment_like_success() throws Exception {
        VoteCommentUseCase useCase = new VoteCommentUseCase(api);
        Call<GenericApiResponseDto> call = mockCall(Response.success(new GenericApiResponseDto()));
        when(api.voteComment(eq(5L), any())).thenReturn(call);

        assertTrue(useCase.execute(5L, 10L, true));
    }

    @Test
    public void voteComment_dislike_success() throws Exception {
        VoteCommentUseCase useCase = new VoteCommentUseCase(api);
        Call<GenericApiResponseDto> call = mockCall(Response.success(new GenericApiResponseDto()));
        when(api.voteComment(eq(5L), any())).thenReturn(call);

        assertTrue(useCase.execute(5L, 10L, false));
    }

    @Test
    public void voteComment_networkError_returnsFalse() throws Exception {
        VoteCommentUseCase useCase = new VoteCommentUseCase(api);
        Call<GenericApiResponseDto> call = mockCallThrows(new IOException("offline"));
        when(api.voteComment(anyLong(), any())).thenReturn(call);

        assertFalse(useCase.execute(5L, 10L, true));
    }

    // ────── DeleteCommentUseCase ──────

    @Test
    public void deleteComment_success() throws Exception {
        DeleteCommentUseCase useCase = new DeleteCommentUseCase(api);
        Call<GenericApiResponseDto> call = mockCall(Response.success(new GenericApiResponseDto()));
        when(api.deleteComment(eq(5L), eq(10L))).thenReturn(call);

        assertTrue(useCase.execute(5L, 10L));
    }

    @Test
    public void deleteComment_httpError_returnsFalse() throws Exception {
        DeleteCommentUseCase useCase = new DeleteCommentUseCase(api);
        Call<GenericApiResponseDto> call = mockCall(
                Response.error(403, ResponseBody.create(null, "")));
        when(api.deleteComment(anyLong(), anyLong())).thenReturn(call);

        assertFalse(useCase.execute(5L, 10L));
    }

    @Test
    public void deleteComment_networkError_returnsFalse() throws Exception {
        DeleteCommentUseCase useCase = new DeleteCommentUseCase(api);
        Call<GenericApiResponseDto> call = mockCallThrows(new IOException("fail"));
        when(api.deleteComment(anyLong(), anyLong())).thenReturn(call);

        assertFalse(useCase.execute(5L, 10L));
    }

    // ────── Helpers ──────

    @SuppressWarnings("unchecked")
    private <T> Call<T> mockCall(Response<T> response) throws Exception {
        Call<T> call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        return call;
    }

    @SuppressWarnings("unchecked")
    private <T> Call<T> mockCallThrows(Throwable t) throws Exception {
        Call<T> call = mock(Call.class);
        when(call.execute()).thenThrow(t);
        return call;
    }
}
