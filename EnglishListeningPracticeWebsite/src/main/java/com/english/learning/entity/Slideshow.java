package com.english.learning.entity;

import com.english.learning.enums.SlideshowPosition;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "slideshows")
@SQLRestriction("is_deleted = false")
public class Slideshow {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "cloud_image_id")
    private String cloudImageId;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "display_order", columnDefinition = "int default 0")
    private Integer displayOrder = 0;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('HOME') DEFAULT 'HOME'")
    private SlideshowPosition position = SlideshowPosition.HOME;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
