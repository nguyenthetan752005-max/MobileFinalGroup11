package hcmute.edu.vn.nguyenthetan.ui.common;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.nguyenthetan.core.AppearancePreferenceStore;
import hcmute.edu.vn.nguyenthetan.core.ThemePreferenceStore;

public abstract class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemePreferenceStore.applySavedTheme(this);
        AppearancePreferenceStore.applySavedPalette(this);
        super.onCreate(savedInstanceState);
    }
}
