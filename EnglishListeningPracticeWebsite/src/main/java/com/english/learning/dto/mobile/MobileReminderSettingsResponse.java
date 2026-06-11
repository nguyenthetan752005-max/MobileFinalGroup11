package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileReminderSettingsResponse {
    private Boolean dailyReminderEnabled;
    private String dailyReminderTime;
    private String dailyReminderTimezone;
}
