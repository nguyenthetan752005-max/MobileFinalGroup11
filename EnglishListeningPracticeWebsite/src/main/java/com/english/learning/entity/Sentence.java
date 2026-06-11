package com.english.learning.entity;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.english.learning.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "sentences", indexes = {
    @Index(name = "idx_lesson_order", columnList = "lesson_id, orderIndex")
})
@SQLRestriction("is_deleted = false")
public class Sentence {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.PUBLISHED;
    
    private String cloudAudioId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;


    private String fileName;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private Double startTime;
    private Double endTime;
    
    private Integer orderIndex;

    @Column(name = "hint_text", length = 500)
    private String hintText = "";

    @Column(name = "duration_millis")
    private Integer durationMillis;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Danh từ riêng được trích xuất từ content, không lưu vào DB
    @Transient
    private List<String> properNouns;
}