package hcmute.edu.vn.nguyenthetan.data.local.dao.user;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;

@Dao
public interface ProfileDao {

    @Query("SELECT * FROM profile_local WHERE id = 1 LIMIT 1")
    ProfileEntity getProfile();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ProfileEntity entity);
}
