package hcmute.edu.vn.nguyenthetan.data.remote.dto;

public class VoteCommentRequestDto {
    public long userId;
    public boolean isLike;

    public VoteCommentRequestDto(long userId, boolean isLike) {
        this.userId = userId;
        this.isLike = isLike;
    }
}
