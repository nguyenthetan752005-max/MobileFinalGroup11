package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppearancePreferenceStore {

    private static final String PREFS_NAME = "tt_appearance_preferences";
    private static final String KEY_PALETTE = "palette";
    private static final String KEY_CURRENT_MASCOT_MOOD_ID = "current_mascot_mood_id";

    private AppearancePreferenceStore() {
    }

    public static boolean isScaryMoodActive(Context context) {
        return getCurrentMood(context) == MascotMoodResolver.Mood.SCARY;
    }

    public static MascotMoodResolver.Mood getCurrentMood(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return MascotMoodResolver.fromId(preferences.getString(
                KEY_CURRENT_MASCOT_MOOD_ID,
                MascotMoodResolver.Mood.ORIGIN.getId()
        ));
    }

    public static void setCurrentMood(Context context, MascotMoodResolver.Mood mood) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(
                        KEY_CURRENT_MASCOT_MOOD_ID,
                        mood == null ? MascotMoodResolver.Mood.ORIGIN.getId() : mood.getId()
                )
                .apply();
    }

    public static void setScaryMoodActive(Context context, boolean active) {
        setCurrentMood(context, active ? MascotMoodResolver.Mood.SCARY : MascotMoodResolver.Mood.ORIGIN);
    }

    public static void clearSessionOverride(Context context) {
        setCurrentMood(context, MascotMoodResolver.Mood.ORIGIN);
    }

    public static AppPalette getPalette(Context context) {
        if (isScaryMoodActive(context)) {
            return AppPalette.SCARY;
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return AppPalette.fromId(preferences.getString(KEY_PALETTE, AppPalette.CLASSIC.getId()));
    }

    public static void setPalette(Context context, AppPalette palette) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PALETTE, palette.getId()).apply();
    }

    public static void applySavedPalette(Context context) {
        AppPalette paletteToApply = getPalette(context);
        context.getTheme().applyStyle(paletteToApply.getThemeOverlayResId(), true);
    }
}
