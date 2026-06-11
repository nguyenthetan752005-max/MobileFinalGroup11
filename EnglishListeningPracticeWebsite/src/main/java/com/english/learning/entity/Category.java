package com.english.learning.entity;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.enums.PracticeType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "categories")
@SQLRestriction("is_deleted = false")
public class Category {
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.PUBLISHED;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    private String cloudImageId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String imageUrl;
    private String levelRange;

    @Column(unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    private LessonType type = LessonType.AUDIO;

    @Enumerated(EnumType.STRING)
    private PracticeType practiceType = PracticeType.LISTENING;

    private String folderName;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer totalLessons = 0;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
