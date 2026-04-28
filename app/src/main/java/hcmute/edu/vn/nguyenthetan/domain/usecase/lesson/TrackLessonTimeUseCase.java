package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import java.io.IOException;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.TimeTrackingRequestDto;
import retrofit2.Response;

/**
 * Best-effort time tracking. Failures are swallowed because telemetry of this
 * kind should never crash the lesson flow.
 */
public class TrackLessonTimeUseCase {

    private final MobileApiService api;

    public TrackLessonTimeUseCase(MobileApiService api) {
        this.api = api;
    }

    public void execute(long userId, int durationSeconds) {
        if (userId <= 0L || durationSeconds <= 0) {
            return;
        }
        try {
            Response<GenericApiResponseDto> response = api.trackTime(
                    new TimeTrackingRequestDto(userId, durationSeconds)
            ).execute();
            // Result intentionally ignored.
            response.isSuccessful();
        } catch (IOException | RuntimeException ignored) {
            // Best-effort.
        }
    }
}
