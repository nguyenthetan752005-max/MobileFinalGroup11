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

    @Query("SELECT COUNT(*) FROM sentence_local WHERE lessonId = :lessonId")
    int countByLessonId(long lessonId);

    @Query("DELETE FROM sentence_local WHERE lessonId = :lessonId")
    void deleteByLessonId(long lessonId);

    @Query("DELETE FROM sentence_local")
    void deleteAll();

    @Query("UPDATE sentence_local SET localAudioPath = :localAudioPath WHERE id = :sentenceId")
    void updateLocalAudioPath(long sentenceId, String localAudioPath);

    @Query("UPDATE sentence_local SET localAudioPath = NULL WHERE id = :sentenceId")
    void clearLocalAudioPath(long sentenceId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SentenceEntity> entities);
}
