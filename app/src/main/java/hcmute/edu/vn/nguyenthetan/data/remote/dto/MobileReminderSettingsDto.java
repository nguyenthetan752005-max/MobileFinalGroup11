package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MobileReminderSettingsDto {
    @SerializedName(value = "dailyReminderEnabled", alternate = {"daily_reminder_enabled"})
    public boolean dailyReminderEnabled;

    @SerializedName(value = "dailyReminderTime", alternate = {"daily_reminder_time"})
    public String dailyReminderTime;

    @SerializedName(value = "dailyReminderTimezone", alternate = {"daily_reminder_timezone"})
    public String dailyReminderTimezone;
}
