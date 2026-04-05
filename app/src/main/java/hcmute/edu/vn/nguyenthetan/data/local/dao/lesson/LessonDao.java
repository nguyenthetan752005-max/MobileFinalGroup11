package hcmute.edu.vn.nguyenthetan.data.local.dao.lesson;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;

@Dao
public interface LessonDao {

    @Query("SELECT * FROM lesson_local WHERE sectionId = :sectionId ORDER BY orderIndex, id")
    List<LessonEntity> getBySectionId(long sectionId);

    @Query("SELECT * FROM lesson_local WHERE id = :lessonId LIMIT 1")
    LessonEntity getById(long lessonId);

    @Query("SELECT * FROM lesson_local ORDER BY orderIndex, id")
    List<LessonEntity> getAllOrdered();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LessonEntity> entities);
}
