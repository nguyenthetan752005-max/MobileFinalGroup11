package com.english.learning.entity;

import com.english.learning.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain Entity: User (OOSE - Rich Domain Model).
 *
 * Owns lifecycle of all child entities via JPA Cascade.
 * When a User is deleted, all related records (comments, votes, progress, etc.)
 * are automatically removed by the ORM — no manual repository calls needed.
 */
@Entity
@Getter
@Setter
@ToString(exclude = { "passwordResetTokens", "comments", "commentVotes", "userProgresses", "speakingResults",
        "dailyStudyStatistics" })
@EqualsAndHashCode(of = "id")
@Table(name = "users", indexes = {
        @Index(name = "idx_role_active_time_7d", columnList = "role,active_time_7d DESC"),
        @Index(name = "idx_role_active_time_30d", columnList = "role,active_time_30d DESC")
})
@SQLRestriction("is_deleted = false")
public class User {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    // Trạng thái tài khoản: is_active=false -> tạm khóa, is_deleted=true ->
    // xóa/vĩnh viễn
    private String suspensionReason; // Lý do khóa (optional)

    private String avatarUrl;
    private String avatarPublicId;
    private String provider;
    private String providerId;
    private Integer totalActiveTime = 0;
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = false;

    @Column(name = "notification_timezone", length = 80)
    private String notificationTimezone;

    // Cached values updated by Leaderboard Cron Job to ensure ultra-fast
    // Leaderboard Retrieval
    @Column(name = "active_time_7d")
    private Integer activeTime7d = 0;

    @Column(name = "active_time_30d")
    private Integer activeTime30d = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Transient
    public boolean isOnline(int timeoutMinutes) {
        return lastActiveAt != null && lastActiveAt.isAfter(LocalDateTime.now().minusMinutes(timeoutMinutes));
    }

    // ===== Cascading Relationships (OOSE: Entity owns lifecycle of children) =====

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentVote> commentVotes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<UserProgress> userProgresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SpeakingResult> speakingResults = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<DailyStudyStatistic> dailyStudyStatistics = new ArrayList<>();
}
