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

public class UpdateUsernameUseCaseTest {

    private MobileApiService api;
    private UpdateUsernameUseCase useCase;

    @Before
    public void setUp() {
        api = mock(MobileApiService.class);
        useCase = new UpdateUsernameUseCase(api);
    }

    @Test
    public void execute_returnsSuccess_whenServerAcksTrue() throws Exception {
        GenericApiResponseDto body = new GenericApiResponseDto();
        body.success = true;
        body.message = "Updated";
        stubExecute(Response.success(body));

        UpdateUsernameUseCase.Result result = useCase.execute(1L, "newName");

        assertTrue(result.success);
        assertFalse(result.networkError);
        assertEquals("Updated", result.message);
    }

    @Test
    public void execute_returnsFailure_onHttpError() throws Exception {
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), "{}");
        stubExecute(Response.<GenericApiResponseDto>error(409, errorBody));

        UpdateUsernameUseCase.Result result = useCase.execute(1L, "taken");

        assertFalse(result.success);
        assertFalse(result.networkError);
    }

    @Test
    public void execute_returnsNetworkError_onIOException() throws Exception {
        @SuppressWarnings("unchecked")
        Call<GenericApiResponseDto> call = (Call<GenericApiResponseDto>) mock(Call.class);
        when(call.execute()).thenThrow(new IOException("offline"));
        when(api.updateUsername(anyLong(), any())).thenReturn(call);

        UpdateUsernameUseCase.Result result = useCase.execute(1L, "x");

        assertTrue(result.networkError);
    }

    @SuppressWarnings("unchecked")
    private void stubExecute(Response<GenericApiResponseDto> response) throws IOException {
        Call<GenericApiResponseDto> call = (Call<GenericApiResponseDto>) mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(api.updateUsername(anyLong(), any())).thenReturn(call);
    }
}
