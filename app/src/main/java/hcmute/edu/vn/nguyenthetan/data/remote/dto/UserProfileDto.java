package hcmute.edu.vn.nguyenthetan.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UserProfileDto {
    @SerializedName("id")
    public long id;

    @SerializedName("username")
    public String username;

    @SerializedName("email")
    public String email;

    @SerializedName("role")
    public String role;

    @SerializedName(value = "avatarUrl", alternate = {"avatar_url"})
    public String avatarUrl;

    @SerializedName(value = "totalActiveTime", alternate = {"total_active_time"})
    public int totalActiveTime;

    @SerializedName(value = "formattedActiveTime", alternate = {"formatted_active_time"})
    public String formattedActiveTime;

    @SerializedName(value = "activeTime7d", alternate = {"active_time_7d"})
    public int activeTime7d;

    @SerializedName(value = "activeTime30d", alternate = {"active_time_30d"})
    public int activeTime30d;

    @SerializedName(value = "currentStreak", alternate = {"current_streak"})
    public int currentStreak;

    @SerializedName(value = "longestStreak", alternate = {"longest_streak"})
    public int longestStreak;

    @SerializedName(value = "missedDays", alternate = {"missed_days"})
    public Integer missedDays;

    @SerializedName(value = "weeklyActivity", alternate = {"weekly_activity"})
    public java.util.List<Integer> weeklyActivity;

    @SerializedName(value = "notificationsEnabled", alternate = {"notifications_enabled"})
    public Boolean notificationsEnabled;

    @SerializedName(value = "notificationTimezone", alternate = {"notification_timezone"})
    public String notificationTimezone;

    @SerializedName(value = "dailyReminderEnabled", alternate = {"daily_reminder_enabled"})
    public Boolean dailyReminderEnabled;

    @SerializedName(value = "dailyReminderTime", alternate = {"daily_reminder_time"})
    public String dailyReminderTime;

    @SerializedName(value = "dailyReminderTimezone", alternate = {"daily_reminder_timezone"})
    public String dailyReminderTimezone;
}
