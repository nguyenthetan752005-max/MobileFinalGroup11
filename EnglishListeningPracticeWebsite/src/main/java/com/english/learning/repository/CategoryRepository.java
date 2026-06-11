package com.english.learning.repository;

import com.english.learning.entity.Category;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.PracticeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByPracticeType(PracticeType practiceType);
    List<Category> findAllByOrderByOrderIndexAscIdAsc();
    List<Category> findByStatusOrderByOrderIndexAscIdAsc(ContentStatus status);
    List<Category> findByPracticeTypeAndStatusOrderByOrderIndexAscIdAsc(PracticeType practiceType, ContentStatus status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT c FROM Category c
            WHERE c.isDeleted = false
              AND c.status = :status
            ORDER BY c.orderIndex ASC, c.id ASC
            """)
    List<Category> findPublishedCategories(@org.springframework.data.repository.query.Param("status") ContentStatus status);
    Optional<Category> findByIdAndStatus(Long id, ContentStatus status);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM categories WHERE is_deleted = true ORDER BY order_index ASC, id ASC", nativeQuery = true)
    List<Category> findDeletedCategories();

    Optional<Category> findBySlugAndStatus(String slug, ContentStatus status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT c FROM Category c
            WHERE c.isDeleted = false
              AND c.status = :status
              AND c.slug = :slug
            """)
    Optional<Category> findPublishedBySlug(@org.springframework.data.repository.query.Param("slug") String slug,
                                             @org.springframework.data.repository.query.Param("status") ContentStatus status);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM categories WHERE id = :id", nativeQuery = true)
    Optional<Category> findAnyCategoryById(@org.springframework.data.repository.query.Param("id") Long id);
}
