package hcmute.edu.vn.nguyenthetan.data.local.dao.user;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import hcmute.edu.vn.nguyenthetan.data.local.entity.user.AppSettingsEntity;

@Dao
public interface AppSettingsDao {

    @Query("SELECT * FROM app_settings_local WHERE id = 1 LIMIT 1")
    AppSettingsEntity getSettings();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AppSettingsEntity entity);
}
