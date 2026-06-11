package com.english.learning.repository;

import com.english.learning.entity.Slideshow;
import com.english.learning.enums.SlideshowPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SlideshowRepository extends JpaRepository<Slideshow, Long> {
    List<Slideshow> findByIsActiveTrueAndPositionOrderByDisplayOrderAscIdAsc(SlideshowPosition position);
    List<Slideshow> findAllByOrderByDisplayOrderAscIdAsc();

    @Query(value = "SELECT * FROM slideshows WHERE is_deleted = true ORDER BY display_order ASC, id ASC", nativeQuery = true)
    List<Slideshow> findDeletedSlideshows();

    @Query(value = "SELECT * FROM slideshows WHERE id = :id", nativeQuery = true)
    Optional<Slideshow> findAnyById(@Param("id") Long id);
}
