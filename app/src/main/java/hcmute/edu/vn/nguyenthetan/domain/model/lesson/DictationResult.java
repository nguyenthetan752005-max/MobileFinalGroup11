package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * Domain model for a server-side dictation check result.
 * Replaces DictationResultDto in the UI layer.
 */
public final class DictationResult {
    public final boolean correct;
    public final int matchedCount;
    public final int totalWords;
    @Nullable public final List<String> hintWords;
    public final int newHintIndex;
    @Nullable public final String hint;
    @Nullable public final String correctSentence;
    @Nullable public final String message;

    public DictationResult(boolean correct, int matchedCount, int totalWords,
                           @Nullable List<String> hintWords, int newHintIndex,
                           @Nullable String hint, @Nullable String correctSentence,
                           @Nullable String message) {
        this.correct = correct;
        this.matchedCount = matchedCount;
        this.totalWords = totalWords;
        this.hintWords = hintWords;
        this.newHintIndex = newHintIndex;
        this.hint = hint;
        this.correctSentence = correctSentence;
        this.message = message;
    }
}
