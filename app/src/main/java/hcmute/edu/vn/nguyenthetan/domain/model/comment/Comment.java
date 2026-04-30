package hcmute.edu.vn.nguyenthetan.domain.model.comment;

import java.util.Collections;
import java.util.List;

public class Comment {

    private final long id;
    private final long authorId;
    private final long sentenceId;
    private final String author;
    private final String avatarLabel;
    private final String avatarUrl;
    private final String timeAgo;
    private final String content;
    private final int likes;
    private final int dislikes;
    private final List<Comment> replies;

    public Comment(
            long id,
            long authorId,
            long sentenceId,
            String author,
            String avatarLabel,
            String avatarUrl,
            String timeAgo,
            String content,
            int likes,
            int dislikes,
            List<Comment> replies
    ) {
        this.id = id;
        this.authorId = authorId;
        this.sentenceId = sentenceId;
        this.author = author;
        this.avatarLabel = avatarLabel;
        this.avatarUrl = avatarUrl;
        this.timeAgo = timeAgo;
        this.content = content;
        this.likes = likes;
        this.dislikes = dislikes;
        this.replies = Collections.unmodifiableList(replies);
    }

    public long getId() {
        return id;
    }

    public long getAuthorId() {
        return authorId;
    }

    public long getSentenceId() {
        return sentenceId;
    }

    public String getAuthor() {
        return author;
    }

    public String getAvatarLabel() {
        return avatarLabel;
    }

    public String getAvatarUrl() {
        return avatarUrl;
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
