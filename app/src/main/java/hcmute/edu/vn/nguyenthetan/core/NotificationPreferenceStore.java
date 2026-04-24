package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.content.SharedPreferences;

public final class NotificationPreferenceStore {

    private static final String PREFS_NAME = "tt_notification_preferences";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_INITIALIZED = "notifications_initialized";
    private static final String KEY_PERMISSION_REQUESTED = "notification_permission_requested";

    private NotificationPreferenceStore() {
    }

    public static boolean isEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
                .putBoolean(KEY_INITIALIZED, true)
                .apply();
    }

    public static boolean isInitialized(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_INITIALIZED, false);
    }

    public static void markInitialized(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_INITIALIZED, true).apply();
    }

    public static boolean hasRequestedPermission(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_PERMISSION_REQUESTED, false);
    }

    public static void markPermissionRequested(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply();
    }

    public static void initializeDefaultEnabledState(Context context, boolean enabled) {
        if (isInitialized(context)) {
            return;
        }
        setEnabled(context, enabled);
    }
}
