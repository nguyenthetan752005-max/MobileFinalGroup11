package com.english.learning.entity;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import jakarta.persistence.Index;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "lessons", indexes = {
    @Index(name = "idx_lesson_status", columnList = "status"),
    @Index(name = "idx_lesson_section", columnList = "section_id")
})
@SQLRestriction("is_deleted = false")
public class Lesson {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.PUBLISHED;

    @Column(name = "order_index")
    private Integer orderIndex = 0;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    private String youtubeVideoId;
    private String title;
    private String level;
    private Integer totalSentences;
    private String folderName;

    @Column(name = "pass_threshold")
    private Integer passThreshold = 70;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    private LessonType contentType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
