package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

import androidx.annotation.Nullable;

/**
 * Domain model for a full speaking evaluation response from server.
 * Includes both the current attempt and the best-ever result.
 * Replaces SpeakingResultDto in the UI layer.
 */
public final class SpeakingEvaluation {
    public final int score;
    public final double accuracy;
    @Nullable public final String recognizedText;
    @Nullable public final String feedback;
    @Nullable public final String audioUrl;
    @Nullable public final String resultType;
    @Nullable public final SpeakingAttempt bestResult;

    public SpeakingEvaluation(int score, double accuracy,
                              @Nullable String recognizedText, @Nullable String feedback,
                              @Nullable String audioUrl, @Nullable String resultType,
                              @Nullable SpeakingAttempt bestResult) {
        this.score = score;
        this.accuracy = accuracy;
        this.recognizedText = recognizedText;
        this.feedback = feedback;
        this.audioUrl = audioUrl;
        this.resultType = resultType;
        this.bestResult = bestResult;
    }
}
