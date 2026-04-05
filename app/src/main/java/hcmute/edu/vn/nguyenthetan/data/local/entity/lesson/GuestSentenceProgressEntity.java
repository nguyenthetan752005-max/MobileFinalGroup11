package hcmute.edu.vn.nguyenthetan.data.local.entity.lesson;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;

@Entity(
        tableName = "guest_sentence_progress_local",
        foreignKeys = {
                @ForeignKey(
                        entity = SentenceEntity.class,
                        parentColumns = "id",
                        childColumns = "sentenceId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("lessonId")
        }
)
public class GuestSentenceProgressEntity {

    @PrimaryKey
    public long sentenceId;

    public long lessonId;
    public SentenceStatus status;
    public Integer bestSpeakingScore;
    public String bestSpeakingTranscript;
    public String bestSpeakingFeedback;
    public Integer currentSpeakingScore;
    public String currentSpeakingTranscript;
    public String currentSpeakingFeedback;
    public long lastAccessedAt;

    public GuestSentenceProgressEntity(
            long sentenceId,
            long lessonId,
            SentenceStatus status,
            Integer bestSpeakingScore,
            String bestSpeakingTranscript,
            String bestSpeakingFeedback,
            Integer currentSpeakingScore,
            String currentSpeakingTranscript,
            String currentSpeakingFeedback,
            long lastAccessedAt
    ) {
        this.sentenceId = sentenceId;
        this.lessonId = lessonId;
        this.status = status;
        this.bestSpeakingScore = bestSpeakingScore;
        this.bestSpeakingTranscript = bestSpeakingTranscript;
        this.bestSpeakingFeedback = bestSpeakingFeedback;
        this.currentSpeakingScore = currentSpeakingScore;
        this.currentSpeakingTranscript = currentSpeakingTranscript;
        this.currentSpeakingFeedback = currentSpeakingFeedback;
        this.lastAccessedAt = lastAccessedAt;
    }
}
