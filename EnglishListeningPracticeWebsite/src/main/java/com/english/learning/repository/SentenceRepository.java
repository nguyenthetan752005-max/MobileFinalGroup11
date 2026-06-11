package com.english.learning.repository;

import com.english.learning.entity.Sentence;
import com.english.learning.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findByLesson_IdOrderByOrderIndex(Long lessonId);
    List<Sentence> findByLesson_IdOrderByOrderIndexAsc(Long lessonId);
    List<Sentence> findByLesson_IdAndStatusOrderByOrderIndexAsc(Long lessonId, ContentStatus status);
    List<Sentence> findByStatusOrderByOrderIndexAscIdAsc(ContentStatus status);
    long countByLesson_Id(Long lessonId);
    long countByLesson_IdAndStatus(Long lessonId, ContentStatus status);
    long countByLesson_Section_Id(Long sectionId);
    long countByLesson_Section_IdAndStatus(Long sectionId, ContentStatus status);
    long countByLesson_Section_Category_Id(Long categoryId);
    long countByLesson_Section_Category_IdAndStatus(Long categoryId, ContentStatus status);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM sentences WHERE is_deleted = true", nativeQuery = true)
    List<Sentence> findDeletedSentences();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM sentences WHERE id = :id", nativeQuery = true)
    Optional<Sentence> findAnySentenceById(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query("""
            select s from Sentence s
            where s.id = :sentenceId
              and s.isDeleted = false
              and s.status = :status
              and s.lesson.isDeleted = false
              and s.lesson.status = :status
              and s.lesson.section.isDeleted = false
              and s.lesson.section.status = :status
              and s.lesson.section.category.isDeleted = false
              and s.lesson.section.category.status = :status
            """)
    Optional<Sentence> findPublishedById(@org.springframework.data.repository.query.Param("sentenceId") Long sentenceId,
                                         @org.springframework.data.repository.query.Param("status") ContentStatus status);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM sentences WHERE lesson_id = :lessonId", nativeQuery = true)
    long countAnyByLessonId(@org.springframework.data.repository.query.Param("lessonId") Long lessonId);
}
