package com.english.learning.repository;

import com.english.learning.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findBySentence_Id(Long sentenceId);
    List<Comment> findBySentence_IdAndParentIsNullOrderByCreatedAtDesc(Long sentenceId);
    List<Comment> findByParent_IdOrderByCreatedAtAsc(Long parentId);
    List<Comment> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<Comment> findBySentence_IdAndIsHiddenFalseAndParentIsNullOrderByCreatedAtDesc(Long sentenceId);
    List<Comment> findByParent_IdAndIsHiddenFalseOrderByCreatedAtAsc(Long parentId);
    List<Comment> findByUser_IdAndIsHiddenFalseOrderByCreatedAtDesc(Long userId);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT * FROM comments WHERE is_deleted = false AND is_hidden = false ORDER BY created_at DESC",
        nativeQuery = true)
    List<Comment> findVisibleComments();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM comments WHERE is_deleted = true ORDER BY created_at DESC", nativeQuery = true)
    List<Comment> findDeletedComments();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM comments WHERE id = :id", nativeQuery = true)
    java.util.Optional<Comment> findAnyCommentById(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM comments WHERE parent_id = :parentId", nativeQuery = true)
    java.util.List<Comment> findAnyByParentId(@org.springframework.data.repository.query.Param("parentId") Long parentId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM comments WHERE sentence_id = :sentenceId", nativeQuery = true)
    long countAnyBySentenceId(@org.springframework.data.repository.query.Param("sentenceId") Long sentenceId);

    @Modifying
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM comments WHERE parent_id IN (SELECT id FROM (SELECT id FROM comments WHERE user_id = :userId) owned_comments)", nativeQuery = true)
    void deleteRepliesToOwnedComments(@org.springframework.data.repository.query.Param("userId") Long userId);

    @Modifying
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM comments WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
