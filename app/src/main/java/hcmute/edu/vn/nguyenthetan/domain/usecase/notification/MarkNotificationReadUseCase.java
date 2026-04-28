package hcmute.edu.vn.nguyenthetan.domain.usecase.notification;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import retrofit2.Response;

public class MarkNotificationReadUseCase {

    private final MobileApiService api;

    public MarkNotificationReadUseCase(MobileApiService api) {
        this.api = api;
    }

    public boolean execute(long notificationId) {
        try {
            Response<GenericApiResponseDto> response = api.markNotificationRead(notificationId).execute();
            return response.isSuccessful();
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
