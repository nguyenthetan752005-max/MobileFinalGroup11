package hcmute.edu.vn.nguyenthetan.ui.common;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ThemePreferenceStore;

public abstract class ThemedActivity extends AppCompatActivity {

    private hcmute.edu.vn.nguyenthetan.core.AppPalette appliedPalette;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemePreferenceStore.applySavedTheme(this);
        AppearancePreferenceStore.applySavedPalette(this);
        appliedPalette = AppearancePreferenceStore.getPalette(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppearancePreferenceStore.getPalette(this) != appliedPalette) {
            recreate();
        }
    }
}
