package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileNotificationSummaryResponse {
    private Long unreadCount;
}
