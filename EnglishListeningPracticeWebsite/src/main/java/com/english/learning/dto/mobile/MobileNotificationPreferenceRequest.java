package com.english.learning.dto.mobile;

import lombok.Data;

@Data
public class MobileNotificationPreferenceRequest {
    private Boolean notificationsEnabled;
    private String timezone;
}
