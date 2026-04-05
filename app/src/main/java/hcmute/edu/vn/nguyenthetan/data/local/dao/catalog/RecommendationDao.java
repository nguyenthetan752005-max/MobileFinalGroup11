package hcmute.edu.vn.nguyenthetan.data.local.dao.catalog;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.RecommendationEntity;

@Dao
public interface RecommendationDao {

    @Query("SELECT * FROM recommendation_local ORDER BY orderIndex, id")
    List<RecommendationEntity> getAllOrdered();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecommendationEntity> entities);
}
