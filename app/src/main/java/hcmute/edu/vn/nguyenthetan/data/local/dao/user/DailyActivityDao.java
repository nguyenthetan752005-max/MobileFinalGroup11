package hcmute.edu.vn.nguyenthetan.data.local.dao.user;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.user.DailyActivityEntity;

@Dao
public interface DailyActivityDao {

    @Query("SELECT * FROM daily_activity_local ORDER BY dayIndex")
    List<DailyActivityEntity> getAllOrdered();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DailyActivityEntity> entities);

    @Query("DELETE FROM daily_activity_local")
    void deleteAll();
}
