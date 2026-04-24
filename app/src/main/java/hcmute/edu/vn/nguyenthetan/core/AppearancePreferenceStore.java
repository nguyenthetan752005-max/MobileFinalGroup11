package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppearancePreferenceStore {

    private static final String PREFS_NAME = "tt_appearance_preferences";
    private static final String KEY_PALETTE = "palette";

    private AppearancePreferenceStore() {
    }

    public static AppPalette getPalette(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return AppPalette.fromId(preferences.getString(KEY_PALETTE, AppPalette.CLASSIC.getId()));
    }

    public static void setPalette(Context context, AppPalette palette) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PALETTE, palette.getId()).apply();
    }

    public static void applySavedPalette(Context context) {
        context.getTheme().applyStyle(getPalette(context).getThemeOverlayResId(), true);
    }
}
