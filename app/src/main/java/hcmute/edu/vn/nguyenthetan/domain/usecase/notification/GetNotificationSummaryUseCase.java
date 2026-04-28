package hcmute.edu.vn.nguyenthetan.domain.usecase.notification;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileNotificationSummaryDto;
import retrofit2.Response;

public class GetNotificationSummaryUseCase {

    private final MobileApiService api;

    public GetNotificationSummaryUseCase(MobileApiService api) {
        this.api = api;
    }

    /** Returns the unread count, or 0 if the call failed. */
    public long execute() {
        try {
            Response<MobileNotificationSummaryDto> response = api.getNotificationSummary().execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().unreadCount;
            }
            return 0L;
        } catch (IOException | RuntimeException e) {
            return 0L;
        }
    }
}
