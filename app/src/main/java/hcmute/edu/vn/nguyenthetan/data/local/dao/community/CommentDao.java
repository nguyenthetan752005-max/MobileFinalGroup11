package hcmute.edu.vn.nguyenthetan.data.local.dao.community;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.community.CommentEntity;

@Dao
public interface CommentDao {

    @Query("SELECT * FROM comment_local WHERE sentenceId = :sentenceId ORDER BY orderIndex, id")
    List<CommentEntity> getBySentenceId(long sentenceId);

    @Query("DELETE FROM comment_local WHERE sentenceId = :sentenceId")
    void deleteBySentenceId(long sentenceId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CommentEntity> entities);
}
