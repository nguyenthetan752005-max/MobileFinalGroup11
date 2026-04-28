package hcmute.edu.vn.nguyenthetan.domain.model.notification;

import java.util.List;

/**
 * Domain model for notification feed.
 * Replaces MobileNotificationFeedDto in UI layer.
 */
public final class NotificationFeed {
    public final long unreadCount;
    public final List<NotificationItem> items;

    public NotificationFeed(long unreadCount, List<NotificationItem> items) {
        this.unreadCount = unreadCount;
        this.items = items;
    }
}
