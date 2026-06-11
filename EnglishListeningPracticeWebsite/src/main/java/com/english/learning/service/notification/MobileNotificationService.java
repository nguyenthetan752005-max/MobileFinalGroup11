package com.english.learning.service.notification;

import com.english.learning.dto.mobile.MobileNotificationFeedResponse;
import com.english.learning.dto.mobile.MobileNotificationSummaryResponse;
import com.english.learning.dto.mobile.MobileReminderDeliveryRequest;
import com.english.learning.entity.Comment;

public interface MobileNotificationService {
    MobileNotificationFeedResponse getFeed(Long userId, int limit);
    MobileNotificationSummaryResponse getSummary(Long userId);
    boolean markAsRead(Long userId, Long notificationId);
    int markAllAsRead(Long userId);
    void createReplyNotification(Comment parentComment, Comment replyComment);
    void recordDailyReminderDelivery(Long userId, MobileReminderDeliveryRequest request);
}
