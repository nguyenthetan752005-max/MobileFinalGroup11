package com.english.learning.entity;

import com.english.learning.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "user_notifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_notifications_user_dedup", columnNames = {"user_id", "dedup_key"})
        },
        indexes = {
                @Index(name = "idx_user_notifications_user_event", columnList = "user_id,event_at"),
                @Index(name = "idx_user_notifications_user_read", columnList = "user_id,read_at")
        }
)
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(length = 160)
    private String meta;

    @Column(name = "dedup_key", nullable = false, length = 120)
    private String dedupKey;

    @Column(name = "target_lesson_id")
    private Long targetLessonId;

    @Column(name = "target_sentence_id")
    private Long targetSentenceId;

    @Column(name = "target_comment_id")
    private Long targetCommentId;

    @Column(name = "event_at", nullable = false)
    private LocalDateTime eventAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
