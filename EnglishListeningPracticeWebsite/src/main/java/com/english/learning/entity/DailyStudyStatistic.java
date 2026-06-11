package com.english.learning.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Design Logic: Action-based time tracking for Leaderboard (OOS / SOLID compliance).
 * 
 * - Purpose: Stores the total active learning time (in seconds) for a user on a specific calendar date.
 * - Why not just store a single counter in User.java?: Storing daily records enables querying rolling time windows 
 *   (e.g., last 7 days, last 30 days) accurately. When a day expires, its duration naturally falls out of the SUM window.
 * - Unique Constraint: Only ONE record per user per day. If a user studies multiple times, 
 *   we accumulate the duration into this single record (activeTimeSeconds += newDuration).
 * - Anti-AFK capability: The service layer limits how much time can be added per action to prevent idling cheats.
 */
@Entity
@Data
@Table(name = "daily_study_statistics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "study_date"})
})
public class DailyStudyStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "active_time_seconds", nullable = false)
    private Integer activeTimeSeconds = 0;
}
