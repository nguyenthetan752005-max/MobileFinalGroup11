package com.english.learning.service.settings;

import com.english.learning.dto.AppSettingForm;
import com.english.learning.entity.AppSetting;

import java.time.LocalTime;

public interface AppSettingService {
    AppSetting getSettings();
    AppSettingForm getSettingForm();
    void updateSettings(AppSettingForm form);
    String getSiteName();
    String getSeoMetaDescription();
    int getMaxRecentUsersOnDashboard();
    int getSpeakingPassThreshold();
    boolean isUserRegistrationAllowed();
    int getOnlineTimeoutMinutes();
    boolean isDailyReminderEnabled();
    LocalTime getDailyReminderTime();
    String getDailyReminderTimezone();
}
