package hcmute.edu.vn.nguyenthetan.data.local.dao.catalog;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM category_local ORDER BY orderIndex, id")
    List<CategoryEntity> getAllOrdered();

    @Query("SELECT * FROM category_local WHERE slug = :slug LIMIT 1")
    CategoryEntity getBySlug(String slug);

    @Query("SELECT * FROM category_local ORDER BY orderIndex, id LIMIT 1")
    CategoryEntity getFirstCategory();

    @Query("SELECT COUNT(*) FROM category_local")
    int count();

    @Query("DELETE FROM category_local")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CategoryEntity> entities);
}
