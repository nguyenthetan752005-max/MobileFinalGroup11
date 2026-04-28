package hcmute.edu.vn.nguyenthetan.domain.model.notification;

import androidx.annotation.Nullable;

/**
 * Domain model for a single notification.
 * Replaces MobileNotificationItemDto in UI layer.
 */
public final class NotificationItem {
    public final long id;
    @Nullable public final String type;
    @Nullable public final String title;
    @Nullable public final String body;
    @Nullable public final String meta;
    @Nullable public final String timeAgo;
    @Nullable public final Boolean read;
    @Nullable public final Long targetLessonId;
    @Nullable public final Long targetSentenceId;
    @Nullable public final Long targetCommentId;

    public NotificationItem(long id, @Nullable String type, @Nullable String title,
                            @Nullable String body, @Nullable String meta,
                            @Nullable String timeAgo, @Nullable Boolean read,
                            @Nullable Long targetLessonId, @Nullable Long targetSentenceId,
                            @Nullable Long targetCommentId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.body = body;
        this.meta = meta;
        this.timeAgo = timeAgo;
        this.read = read;
        this.targetLessonId = targetLessonId;
        this.targetSentenceId = targetSentenceId;
        this.targetCommentId = targetCommentId;
    }
}
