package hcmute.edu.vn.nguyenthetan.core.di;

import android.content.Context;

import androidx.room.Room;

import hcmute.edu.vn.nguyenthetan.data.local.db.TungTungDatabase;

public final class DatabaseModule {

    private DatabaseModule() {
    }

    public static TungTungDatabase createDatabase(Context context) {
        return Room.databaseBuilder(
                        context.getApplicationContext(),
                        TungTungDatabase.class,
                        "tungtung.db"
                )
                // Keep destructive fallback ONLY on downgrade (e.g. user installs older build).
                // For upgrades, missing migrations should fail loudly so we don't silently
                // wipe user progress, attempts, comments, etc.
                .fallbackToDestructiveMigrationOnDowngrade()
                .build();
    }
}
