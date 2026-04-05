package hcmute.edu.vn.nguyenthetan.domain.model.comment;

import java.util.Collections;
import java.util.List;

public class Comment {

    private final String author;
    private final String avatarLabel;
    private final String timeAgo;
    private final String content;
    private final int likes;
    private final int dislikes;
    private final List<Comment> replies;

    public Comment(
            String author,
            String avatarLabel,
            String timeAgo,
            String content,
            int likes,
            int dislikes,
            List<Comment> replies
    ) {
        this.author = author;
        this.avatarLabel = avatarLabel;
        this.timeAgo = timeAgo;
        this.content = content;
        this.likes = likes;
        this.dislikes = dislikes;
        this.replies = Collections.unmodifiableList(replies);
    }

    public String getAuthor() {
        return author;
    }

    public String getAvatarLabel() {
        return avatarLabel;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public String getContent() {
        return content;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public List<Comment> getReplies() {
        return replies;
    }
}
