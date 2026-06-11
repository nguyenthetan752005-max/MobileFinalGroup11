package com.english.learning.repository;

import com.english.learning.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByComment_IdAndUser_Id(Long commentId, Long userId);

    @Query("SELECT COUNT(v) FROM CommentVote v WHERE v.comment.id = :commentId AND v.isLike = :isLike")
    long countVotes(@Param("commentId") Long commentId, @Param("isLike") Boolean isLike);

    @Modifying
    @Query(value = "DELETE FROM comment_votes WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM comment_votes WHERE comment_id IN (SELECT id FROM comments WHERE user_id = :userId)", nativeQuery = true)
    void deleteByOwnedCommentUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM comment_votes WHERE comment_id = :commentId", nativeQuery = true)
    void deleteByCommentId(@Param("commentId") Long commentId);
}
