package hcmute.edu.vn.nguyenthetan.data.local.dao.lesson;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;

@Dao
public interface SentenceDao {

    @Query("SELECT * FROM sentence_local WHERE lessonId = :lessonId ORDER BY orderIndex, id")
    List<SentenceEntity> getByLessonId(long lessonId);

    @Query("SELECT * FROM sentence_local WHERE id = :sentenceId LIMIT 1")
    SentenceEntity getById(long sentenceId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SentenceEntity> entities);
}
