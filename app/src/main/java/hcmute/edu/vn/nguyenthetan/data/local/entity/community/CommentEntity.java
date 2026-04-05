package hcmute.edu.vn.nguyenthetan.data.local.entity.community;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;


@Entity(
        tableName = "comment_local",
        foreignKeys = {
                @ForeignKey(
                        entity = SentenceEntity.class,
                        parentColumns = "id",
                        childColumns = "sentenceId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("sentenceId"),
                @Index("parentCommentId")
        }
)
public class CommentEntity {

    @PrimaryKey
    public long id;

    public long sentenceId;
    public Long parentCommentId;
    public String author;
    public String avatarLabel;
    public String timeAgo;
    public String content;
    public int likeCount;
    public int dislikeCount;
    public int orderIndex;

    public CommentEntity(
            long id,
            long sentenceId,
            Long parentCommentId,
            String author,
            String avatarLabel,
            String timeAgo,
            String content,
            int likeCount,
            int dislikeCount,
            int orderIndex
    ) {
        this.id = id;
        this.sentenceId = sentenceId;
        this.parentCommentId = parentCommentId;
        this.author = author;
        this.avatarLabel = avatarLabel;
        this.timeAgo = timeAgo;
        this.content = content;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.orderIndex = orderIndex;
    }
}
