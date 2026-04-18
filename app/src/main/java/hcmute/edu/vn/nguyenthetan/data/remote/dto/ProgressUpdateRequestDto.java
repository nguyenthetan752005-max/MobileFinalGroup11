package hcmute.edu.vn.nguyenthetan.data.remote.dto;

public class ProgressUpdateRequestDto {
    public long userId;
    public long sentenceId;

    public ProgressUpdateRequestDto(long userId, long sentenceId) {
        this.userId = userId;
        this.sentenceId = sentenceId;
    }
}
