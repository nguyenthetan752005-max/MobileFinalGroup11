package hcmute.edu.vn.nguyenthetan.domain.usecase.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * The change-password flow has four distinct outcomes that the EditProfile
 * screen renders differently. This test pins each branch.
 */
public class ChangePasswordUseCaseTest {

    private MobileApiService api;
    private ChangePasswordUseCase useCase;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        api = mock(MobileApiService.class);
        useCase = new ChangePasswordUseCase(api);
    }

    @Test
    public void execute_returnsSuccess_whenServerAcksTrue() throws Exception {
        GenericApiResponseDto body = new GenericApiResponseDto();
        body.success = true;
        body.message = "Updated";
        stubExecute(Response.success(body));

        ChangePasswordUseCase.Result result = useCase.execute(1L, "old", "new");

        assertTrue(result.success);
        assertFalse(result.networkError);
        assertFalse(result.wrongCurrent);
        assertEquals("Updated", result.message);
    }

    @Test
    public void execute_returnsFailure_whenServerAcksFalse() throws Exception {
        GenericApiResponseDto body = new GenericApiResponseDto();
        body.success = false;
        body.message = "Password too short";
        stubExecute(Response.success(body));

        ChangePasswordUseCase.Result result = useCase.execute(1L, "old", "new");

        assertFalse(result.success);
        assertFalse(result.networkError);
        assertFalse(result.wrongCurrent);
        assertEquals("Password too short", result.message);
    }

    @Test
    public void execute_returnsWrongCurrent_onHttpError() throws Exception {
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), "{}");
        stubExecute(Response.<GenericApiResponseDto>error(401, errorBody));

        ChangePasswordUseCase.Result result = useCase.execute(1L, "old", "new");

        assertFalse(result.success);
        assertTrue(result.wrongCurrent);
        assertFalse(result.networkError);
    }

    @Test
    public void execute_returnsNetworkError_onIOException() throws Exception {
        @SuppressWarnings("unchecked")
        Call<GenericApiResponseDto> call = (Call<GenericApiResponseDto>) mock(Call.class);
        when(call.execute()).thenThrow(new IOException("offline"));
        when(api.changePassword(anyLong(), any())).thenReturn(call);

        ChangePasswordUseCase.Result result = useCase.execute(1L, "old", "new");

        assertFalse(result.success);
        assertFalse(result.wrongCurrent);
        assertTrue(result.networkError);
    }

    @SuppressWarnings("unchecked")
    private void stubExecute(Response<GenericApiResponseDto> response) throws IOException {
        Call<GenericApiResponseDto> call = (Call<GenericApiResponseDto>) mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(api.changePassword(anyLong(), any())).thenReturn(call);
    }
}
