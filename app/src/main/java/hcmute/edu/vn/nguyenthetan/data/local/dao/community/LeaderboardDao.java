package hcmute.edu.vn.nguyenthetan.data.local.dao.community;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardEntryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardMetaEntity;

@Dao
public interface LeaderboardDao {

    @Query("SELECT * FROM leaderboard_entry_local WHERE period = :period ORDER BY rank")
    List<LeaderboardEntryEntity> getByPeriod(String period);

    @Query("SELECT * FROM leaderboard_meta_local WHERE id = 1 LIMIT 1")
    LeaderboardMetaEntity getMeta();

    @Query("DELETE FROM leaderboard_entry_local")
    void deleteEntries();

    @Query("DELETE FROM leaderboard_meta_local")
    void deleteMeta();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEntries(List<LeaderboardEntryEntity> entities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertMeta(LeaderboardMetaEntity entity);
}
