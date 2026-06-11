package com.english.learning.repository;

import com.english.learning.entity.Lesson;
import com.english.learning.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findBySectionId(Long sectionId);
    List<Lesson> findBySection_Id(Long sectionId);
    List<Lesson> findBySection_IdOrderByOrderIndexAscIdAsc(Long sectionId);
    List<Lesson> findBySection_IdAndStatusOrderByOrderIndexAscIdAsc(Long sectionId, ContentStatus status);
    List<Lesson> findByStatusOrderByOrderIndexAscIdAsc(ContentStatus status);
    long countBySection_Id(Long sectionId);
    long countBySection_IdAndStatus(Long sectionId, ContentStatus status);
    long countBySection_Category_Id(Long categoryId);
    long countBySection_Category_IdAndStatus(Long categoryId, ContentStatus status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT COUNT(l) FROM Lesson l
            WHERE l.section.category.id = :categoryId
              AND l.isDeleted = false
            """)
    long countActiveBySection_Category_Id(@org.springframework.data.repository.query.Param("categoryId") Long categoryId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT l FROM Lesson l
            WHERE l.isDeleted = false
              AND l.status = :status
              AND l.section.isDeleted = false
              AND l.section.category.isDeleted = false
            ORDER BY l.orderIndex ASC, l.id ASC
            """)
    List<Lesson> findPublishedLessons(@org.springframework.data.repository.query.Param("status") ContentStatus status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT l FROM Lesson l
            WHERE l.isDeleted = false
              AND l.status = :status
              AND l.section.id = :sectionId
              AND l.section.isDeleted = false
              AND l.section.category.isDeleted = false
            ORDER BY l.orderIndex ASC, l.id ASC
            """)
    List<Lesson> findPublishedLessonsBySectionId(@org.springframework.data.repository.query.Param("sectionId") Long sectionId,
                                                 @org.springframework.data.repository.query.Param("status") ContentStatus status);

    @org.springframework.data.jpa.repository.Query("""
            select l from Lesson l
            where l.id = :lessonId
              and l.isDeleted = false
              and l.status = :status
              and l.section.isDeleted = false
              and l.section.status = :status
              and l.section.category.isDeleted = false
              and l.section.category.status = :status
            """)
    Optional<Lesson> findPublishedById(@org.springframework.data.repository.query.Param("lessonId") Long lessonId,
                                       @org.springframework.data.repository.query.Param("status") ContentStatus status);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM lessons WHERE is_deleted = true ORDER BY order_index ASC, id ASC", nativeQuery = true)
    List<Lesson> findDeletedLessons();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM lessons WHERE id = :id", nativeQuery = true)
    Optional<Lesson> findAnyLessonById(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM lessons WHERE section_id = :sectionId", nativeQuery = true)
    long countAnyBySectionId(@org.springframework.data.repository.query.Param("sectionId") Long sectionId);
}
