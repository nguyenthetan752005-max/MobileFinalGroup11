package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.content.SharedPreferences;

public final class ReminderSettingsStore {

    private static final String PREFS_NAME = "tt_reminder_settings";
    private static final String KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled";
    private static final String KEY_DAILY_REMINDER_TIME = "daily_reminder_time";
    private static final String KEY_DAILY_REMINDER_TIMEZONE = "daily_reminder_timezone";
    private static final String DEFAULT_REMINDER_TIME = "19:00";
    private static final String DEFAULT_REMINDER_TIMEZONE = "Asia/Ho_Chi_Minh";

    private ReminderSettingsStore() {
    }

    public static boolean isDailyReminderEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_DAILY_REMINDER_ENABLED, true);
    }

    public static String getDailyReminderTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_DAILY_REMINDER_TIME, DEFAULT_REMINDER_TIME);
    }

    public static String getDailyReminderTimezone(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_DAILY_REMINDER_TIMEZONE, DEFAULT_REMINDER_TIMEZONE);
    }

    public static void save(Context context, boolean enabled, String time, String timezone) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled)
                .putString(KEY_DAILY_REMINDER_TIME, time == null || time.trim().isEmpty() ? DEFAULT_REMINDER_TIME : time.trim())
                .putString(KEY_DAILY_REMINDER_TIMEZONE, timezone == null || timezone.trim().isEmpty() ? DEFAULT_REMINDER_TIMEZONE : timezone.trim())
                .apply();
    }
}
