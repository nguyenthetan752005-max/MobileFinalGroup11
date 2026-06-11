package com.english.learning.entity;

import com.english.learning.enums.SpeakingResultType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "speaking_results")
public class SpeakingResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer score; // 0-100

    @Column(columnDefinition = "TEXT")
    private String recognizedText;

    private String userAudioUrl; // Storage URL

    private String userAudioPublicId; // Storage key for deletion

    @Column(columnDefinition = "TEXT")
    private String feedback; // AI feedback text

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type")
    private SpeakingResultType resultType; // BEST or CURRENT

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "sentence_id")
    private Sentence sentence;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
