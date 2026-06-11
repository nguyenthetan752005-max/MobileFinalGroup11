package com.english.learning.repository;

import com.english.learning.entity.Section;
import com.english.learning.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCategory_Id(Long categoryId);
    List<Section> findByCategoryId(Long categoryId);
    List<Section> findByCategory_IdOrderByOrderIndexAscIdAsc(Long categoryId);
    List<Section> findByCategory_IdAndStatusOrderByOrderIndexAscIdAsc(Long categoryId, ContentStatus status);
    List<Section> findByStatusOrderByOrderIndexAscIdAsc(ContentStatus status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT s FROM Section s
            WHERE s.isDeleted = false
              AND s.status = :status
              AND s.category.isDeleted = false
            ORDER BY s.orderIndex ASC, s.id ASC
            """)
    List<Section> findPublishedSections(@org.springframework.data.repository.query.Param("status") ContentStatus status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT s FROM Section s
            WHERE s.isDeleted = false
              AND s.status = :status
              AND s.category.id = :categoryId
              AND s.category.isDeleted = false
            ORDER BY s.orderIndex ASC, s.id ASC
            """)
    List<Section> findPublishedSectionsByCategoryId(@org.springframework.data.repository.query.Param("categoryId") Long categoryId,
                                                      @org.springframework.data.repository.query.Param("status") ContentStatus status);

    long countByCategory_Id(Long categoryId);
    long countByCategory_IdAndStatus(Long categoryId, ContentStatus status);

    @org.springframework.data.jpa.repository.Query("""
            select s from Section s
            where s.id = :sectionId
              and s.category.id = :categoryId
              and s.isDeleted = false
              and s.status = :status
              and s.category.isDeleted = false
              and s.category.status = :status
            """)
    Optional<Section> findPublishedByIdAndCategoryId(@org.springframework.data.repository.query.Param("sectionId") Long sectionId,
                                                     @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
                                                     @org.springframework.data.repository.query.Param("status") ContentStatus status);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM sections WHERE is_deleted = true ORDER BY order_index ASC, id ASC", nativeQuery = true)
    List<Section> findDeletedSections();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM sections WHERE id = :id", nativeQuery = true)
    Optional<Section> findAnySectionById(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM sections WHERE category_id = :categoryId", nativeQuery = true)
    long countAnyByCategoryId(@org.springframework.data.repository.query.Param("categoryId") Long categoryId);
}
