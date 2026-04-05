package hcmute.edu.vn.nguyenthetan.data.local.dao.lesson;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.GuestSentenceProgressEntity;

@Dao
public interface GuestProgressDao {

    @Query("SELECT * FROM guest_sentence_progress_local WHERE lessonId = :lessonId ORDER BY lastAccessedAt DESC, sentenceId")
    List<GuestSentenceProgressEntity> getByLessonId(long lessonId);

    @Query("SELECT * FROM guest_sentence_progress_local WHERE sentenceId = :sentenceId LIMIT 1")
    GuestSentenceProgressEntity getBySentenceId(long sentenceId);

    @Query("SELECT COUNT(*) FROM guest_sentence_progress_local WHERE lessonId = :lessonId AND status = 'COMPLETED'")
    int getCompletedCountForLesson(long lessonId);

    @Query("SELECT COUNT(*) FROM guest_sentence_progress_local WHERE lessonId = :lessonId")
    int getTouchedCountForLesson(long lessonId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GuestSentenceProgressEntity> entities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(GuestSentenceProgressEntity entity);
}
