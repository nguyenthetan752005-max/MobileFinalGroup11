package hcmute.edu.vn.nguyenthetan.data.remote.dto;

public class TimeTrackingRequestDto {
    public long userId;
    public int durationSeconds;

    public TimeTrackingRequestDto(long userId, int durationSeconds) {
        this.userId = userId;
        this.durationSeconds = durationSeconds;
    }
}
