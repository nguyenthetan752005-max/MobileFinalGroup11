package hcmute.edu.vn.nguyenthetan.core;

import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import hcmute.edu.vn.nguyenthetan.R;

public enum AppPalette {
    CLASSIC("classic", R.string.palette_classic, R.style.ThemeOverlay_TungTung_Palette_Classic),
    OCEAN("ocean", R.string.palette_ocean, R.style.ThemeOverlay_TungTung_Palette_Ocean),
    FOREST("forest", R.string.palette_forest, R.style.ThemeOverlay_TungTung_Palette_Forest),
    SUNSET("sunset", R.string.palette_sunset, R.style.ThemeOverlay_TungTung_Palette_Sunset);

    private final String id;
    private final int labelResId;
    private final int themeOverlayResId;

    AppPalette(String id, @StringRes int labelResId, @StyleRes int themeOverlayResId) {
        this.id = id;
        this.labelResId = labelResId;
        this.themeOverlayResId = themeOverlayResId;
    }

    public String getId() {
        return id;
    }

    @StringRes
    public int getLabelResId() {
        return labelResId;
    }

    @StyleRes
    public int getThemeOverlayResId() {
        return themeOverlayResId;
    }

    public static AppPalette fromId(String rawId) {
        if (rawId != null) {
            for (AppPalette palette : values()) {
                if (palette.id.equalsIgnoreCase(rawId.trim())) {
                    return palette;
                }
            }
        }
        return CLASSIC;
    }
}
