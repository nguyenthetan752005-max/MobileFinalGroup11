package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MobileNotificationFeedResponse {
    private Long unreadCount;
    private List<MobileNotificationItemResponse> items;
}
