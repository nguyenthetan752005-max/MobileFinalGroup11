package hcmute.edu.vn.nguyenthetan.data.remote.dto;

public class CreateCommentRequestDto {
    public long sentenceId;
    public long userId;
    public String content;
    public Long parentId;

    public CreateCommentRequestDto(long sentenceId, long userId, String content, Long parentId) {
        this.sentenceId = sentenceId;
        this.userId = userId;
        this.content = content;
        this.parentId = parentId;
    }
}
