package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.TimeZone;

public final class ReminderSettingsStore {

    private static final String PREFS_NAME = "tt_reminder_settings";
    private static final String KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled";
    private static final String KEY_DAILY_REMINDER_TIME = "daily_reminder_time";
    private static final String KEY_DAILY_REMINDER_TIMEZONE = "daily_reminder_timezone";
    private static final String KEY_LOCAL_TIME_OVERRIDE_ENABLED = "local_time_override_enabled";
    private static final String KEY_LOCAL_TIME_OVERRIDE = "local_time_override";
    private static final String KEY_LOCAL_TIMEZONE_OVERRIDE = "local_timezone_override";
    private static final String DEFAULT_REMINDER_TIME = "19:00";
    private static final String DEFAULT_REMINDER_TIMEZONE = "Asia/Ho_Chi_Minh";

    private ReminderSettingsStore() {
    }

    public static boolean isDailyReminderEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_DAILY_REMINDER_ENABLED, true);
    }

    public static String getDailyReminderTime(Context context) {
        if (hasLocalTimeOverride(context)) {
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return preferences.getString(
                    KEY_LOCAL_TIME_OVERRIDE,
                    getServerDailyReminderTime(context)
            );
        }
        return getServerDailyReminderTime(context);
    }

    public static String getDailyReminderTimezone(Context context) {
        if (hasLocalTimeOverride(context)) {
            return TimeZone.getDefault().getID();
        }
        return getServerDailyReminderTimezone(context);
    }

    public static String getServerDailyReminderTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_DAILY_REMINDER_TIME, DEFAULT_REMINDER_TIME);
    }

    public static String getServerDailyReminderTimezone(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_DAILY_REMINDER_TIMEZONE, DEFAULT_REMINDER_TIMEZONE);
    }

    public static boolean hasLocalTimeOverride(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_LOCAL_TIME_OVERRIDE_ENABLED, false);
    }

    public static void save(Context context, boolean enabled, String time, String timezone) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String sanitizedTime = sanitizeTime(time);
        String sanitizedTimezone = sanitizeTimezone(timezone);
        preferences.edit()
                .putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled)
                .putString(KEY_DAILY_REMINDER_TIME, sanitizedTime)
                .putString(KEY_DAILY_REMINDER_TIMEZONE, sanitizedTimezone)
                .apply();
        if (hasLocalTimeOverride(context)
                && sanitizedTime.equals(getLocalOverrideTime(context))
                && sanitizeTimezone(TimeZone.getDefault().getID()).equals(sanitizedTimezone)) {
            clearLocalTimeOverride(context);
        }
    }

    public static void saveLocalTimeOverride(Context context, String time, String timezone) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String sanitizedTime = sanitizeTime(time);
        String sanitizedTimezone = sanitizeTimezone(timezone);
        if (sanitizedTime.equals(getServerDailyReminderTime(context))
                && sanitizedTimezone.equals(getServerDailyReminderTimezone(context))) {
            clearLocalTimeOverride(context);
            return;
        }
        preferences.edit()
                .putBoolean(KEY_LOCAL_TIME_OVERRIDE_ENABLED, true)
                .putString(KEY_LOCAL_TIME_OVERRIDE, sanitizedTime)
                .putString(KEY_LOCAL_TIMEZONE_OVERRIDE, sanitizedTimezone)
                .apply();
    }

    public static void clearLocalTimeOverride(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(KEY_LOCAL_TIME_OVERRIDE_ENABLED, false)
                .remove(KEY_LOCAL_TIME_OVERRIDE)
                .remove(KEY_LOCAL_TIMEZONE_OVERRIDE)
                .apply();
    }

    private static String sanitizeTime(String time) {
        if (time == null || !time.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            return DEFAULT_REMINDER_TIME;
        }
        return time.trim();
    }

    private static String sanitizeTimezone(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            return DEFAULT_REMINDER_TIMEZONE;
        }
        return timezone.trim();
    }

    private static String getLocalOverrideTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LOCAL_TIME_OVERRIDE, DEFAULT_REMINDER_TIME);
    }
}
