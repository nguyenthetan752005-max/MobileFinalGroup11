package com.english.learning.entity;

import com.english.learning.enums.UserProgressStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "sentence_id"})
})
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "sentence_id")
    private Sentence sentence;

    @Enumerated(EnumType.STRING)
    private UserProgressStatus status;

    @UpdateTimestamp
    private LocalDateTime lastAccessed;
}