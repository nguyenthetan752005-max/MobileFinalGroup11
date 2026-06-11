package com.english.learning.dto.mobile;

import lombok.Data;

@Data
public class MobileReminderDeliveryRequest {
    private String title;
    private String body;
    private String meta;
    private String reminderDate;
    private String reminderTime;
    private String reminderTimezone;
    private Long deliveredAtEpochMillis;
    private Long lessonId;
    private String lessonTitle;
}
