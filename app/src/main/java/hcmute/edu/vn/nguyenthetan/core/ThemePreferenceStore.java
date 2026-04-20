package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemePreferenceStore {

    private static final String PREFS_NAME = "tt_theme_preferences";
    private static final String KEY_THEME_MODE = "theme_mode";

    private ThemePreferenceStore() {
    }

    public static void applySavedTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(getThemeMode(context));
    }

    public static int getThemeMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void setThemeMode(Context context, int themeMode) {
        int resolvedMode = themeMode == AppCompatDelegate.MODE_NIGHT_YES
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(KEY_THEME_MODE, resolvedMode).apply();
        AppCompatDelegate.setDefaultNightMode(resolvedMode);
    }

    public static boolean isDarkMode(Context context) {
        return getThemeMode(context) == AppCompatDelegate.MODE_NIGHT_YES;
    }
}
