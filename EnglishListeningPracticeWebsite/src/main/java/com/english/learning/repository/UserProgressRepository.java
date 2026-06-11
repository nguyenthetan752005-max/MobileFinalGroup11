package com.english.learning.repository;

import com.english.learning.entity.UserProgress;
import com.english.learning.enums.UserProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUser_IdAndSentence_Id(Long userId, Long sentenceId);
    List<UserProgress> findByUser_Id(Long userId);
    long countBySentence_Id(Long sentenceId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.sentence.lesson.id = :lessonId AND up.status = :status")
    long countByUserIdAndLessonIdAndStatus(@Param("userId") Long userId, @Param("lessonId") Long lessonId, @Param("status") UserProgressStatus status);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.sentence.lesson.section.id = :sectionId AND up.status = :status")
    long countByUserIdAndSectionIdAndStatus(@Param("userId") Long userId, @Param("sectionId") Long sectionId, @Param("status") UserProgressStatus status);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.sentence.lesson.section.category.id = :categoryId AND up.status = :status")
    long countByUserIdAndCategoryIdAndStatus(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("status") UserProgressStatus status);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.sentence.lesson.id = :lessonId")
    long countByUserIdAndLessonId(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    @Query("SELECT up FROM UserProgress up WHERE up.user.id = :userId AND up.sentence.lesson.id = :lessonId")
    List<UserProgress> findByUserIdAndLessonId(@Param("userId") Long userId, @Param("lessonId") Long lessonId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.sentence.lesson.section.id = :sectionId")
    long countByUserIdAndSectionId(@Param("userId") Long userId, @Param("sectionId") Long sectionId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.sentence.lesson.section.category.id = :categoryId")
    long countByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT up.sentence.lesson.id FROM UserProgress up WHERE up.user.id = :userId")
    List<Long> findLessonIdsWithProgressByUserId(@Param("userId") Long userId);

    List<UserProgress> findTop100ByUser_IdOrderByLastAccessedDesc(Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_progress WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Long userId);
}
