package hcmute.edu.vn.nguyenthetan.data.local.dao.system;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.system.OfflineActionEntity;

@Dao
public interface OfflineActionDao {

    @Query("SELECT * FROM offline_actions ORDER BY createdAt ASC")
    List<OfflineActionEntity> getAllOrdered();

    @Insert
    void insert(OfflineActionEntity action);

    @Delete
    void delete(OfflineActionEntity action);

    @Query("SELECT * FROM offline_actions WHERE localFilePath = :syncKey ORDER BY createdAt DESC LIMIT 1")
    OfflineActionEntity getLatestBySyncKey(String syncKey);

    @Query("DELETE FROM offline_actions WHERE localFilePath = :syncKey")
    void deleteBySyncKey(String syncKey);

    @Query("DELETE FROM offline_actions WHERE actionType = :actionType")
    void deleteByActionType(String actionType);

    @Query("DELETE FROM offline_actions")
    void deleteAll();
}
