package hcmute.edu.vn.nguyenthetan.data.local.dao.system;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.system.SyncStateEntity;

@Dao
public interface SyncStateDao {

    @Query("SELECT * FROM sync_state_local WHERE `key` = :key LIMIT 1")
    SyncStateEntity getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SyncStateEntity> entities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SyncStateEntity entity);
}
