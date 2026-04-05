package hcmute.edu.vn.nguyenthetan.data.local.dao.catalog;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;

@Dao
public interface SectionDao {

    @Query("SELECT * FROM section_local WHERE categoryId = :categoryId ORDER BY orderIndex, id")
    List<SectionEntity> getByCategoryId(long categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SectionEntity> entities);
}
