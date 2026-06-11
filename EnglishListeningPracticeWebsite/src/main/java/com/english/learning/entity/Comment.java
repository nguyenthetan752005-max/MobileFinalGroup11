package com.english.learning.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "comments")
@SQLRestriction("is_deleted = false")
public class Comment {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @ManyToOne
    @JoinColumn(name = "sentence_id")
    @JsonIgnoreProperties({"lesson", "properNouns", "hibernateLazyInitializer"})
    private Sentence sentence;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "provider", "providerId", "hibernateLazyInitializer"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"parent", "sentence", "hibernateLazyInitializer"})
    private Comment parent;

    @Transient
    private long likeCount;

    @Transient
    private long dislikeCount;
}
