package hcmute.edu.vn.nguyenthetan.domain.usecase.notification;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileNotificationFeedDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileNotificationItemDto;
import hcmute.edu.vn.nguyenthetan.domain.model.notification.NotificationFeed;
import hcmute.edu.vn.nguyenthetan.domain.model.notification.NotificationItem;
import retrofit2.Response;

public class GetNotificationsUseCase {

    private final MobileApiService api;

    public GetNotificationsUseCase(MobileApiService api) {
        this.api = api;
    }

    @Nullable
    public NotificationFeed execute(int limit) {
        try {
            Response<MobileNotificationFeedDto> response = api.getNotifications(limit).execute();
            if (response.isSuccessful() && response.body() != null) {
                MobileNotificationFeedDto dto = response.body();
                List<NotificationItem> items = new ArrayList<>();
                if (dto.items != null) {
                    for (MobileNotificationItemDto item : dto.items) {
                        items.add(new NotificationItem(
                                item.id, item.type, item.title, item.body, item.meta,
                                item.timeAgo, item.read, item.targetLessonId,
                                item.targetSentenceId, item.targetCommentId));
                    }
                }
                return new NotificationFeed(dto.unreadCount, items);
            }
            return null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
