package hcmute.edu.vn.nguyenthetan.data.local.dao.user;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.user.StreakDayEntity;

@Dao
public interface StreakDayDao {

    @Query("SELECT * FROM streak_day_local ORDER BY dayIndex")
    List<StreakDayEntity> getAllOrdered();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StreakDayEntity> entities);
}
