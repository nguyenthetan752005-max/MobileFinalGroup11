package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AttrRes;

import com.google.android.material.color.MaterialColors;

public final class ThemeColorResolver {

    private ThemeColorResolver() {
    }

    public static int resolveColor(Context context, @AttrRes int attrRes) {
        TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(attrRes, typedValue, true)) {
            throw new IllegalArgumentException("Missing theme attribute: " + attrRes);
        }
        return MaterialColors.getColor(context, attrRes, typedValue.data);
    }
}
