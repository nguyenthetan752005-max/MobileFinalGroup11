package hcmute.edu.vn.nguyenthetan.ui.notification;

import androidx.annotation.NonNull;

final class NotificationItem {

    enum Type {
        MOOD,
        REMINDER,
        REPLY
    }

    private final long id;
    private final Type type;
    private final String title;
    private final String body;
    private final String meta;
    private final boolean read;
    private final long targetLessonId;

    NotificationItem(
            long id,
            @NonNull Type type,
            @NonNull String title,
            @NonNull String body,
            @NonNull String meta,
            boolean read,
            long targetLessonId
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.body = body;
        this.meta = meta;
        this.read = read;
        this.targetLessonId = targetLessonId;
    }

    long getId() {
        return id;
    }

    @NonNull
    Type getType() {
        return type;
    }

    @NonNull
    String getTitle() {
        return title;
    }

    @NonNull
    String getBody() {
        return body;
    }

    @NonNull
    String getMeta() {
        return meta;
    }

    boolean isRead() {
        return read;
    }

    long getTargetLessonId() {
        return targetLessonId;
    }

    boolean isRemote() {
        return id > 0L;
    }

    @NonNull
    NotificationItem markRead() {
        if (read) {
            return this;
        }
        return new NotificationItem(id, type, title, body, meta, true, targetLessonId);
    }
}
