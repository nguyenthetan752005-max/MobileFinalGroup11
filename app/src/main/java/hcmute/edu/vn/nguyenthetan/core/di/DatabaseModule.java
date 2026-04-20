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
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }
}
