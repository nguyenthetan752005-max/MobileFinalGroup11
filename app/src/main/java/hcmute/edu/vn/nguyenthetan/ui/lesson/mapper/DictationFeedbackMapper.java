package hcmute.edu.vn.nguyenthetan.ui.lesson.mapper;

import androidx.annotation.NonNull;

import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.data.remote.dto.DictationResultDto;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;

public class DictationFeedbackMapper {

    public static DictationFeedback map(@NonNull DictationResultDto dto, String currentSentenceContent, String currentHintText) {
        String fullAnswer = dto.correctSentence == null || dto.correctSentence.trim().isEmpty()
                ? currentSentenceContent : dto.correctSentence;
        String hint = dto.hint == null ? currentHintText : dto.hint;
        String correctWords = dto.correct ? fullAnswer : "";
        String maskedWords = dto.correct ? "" : buildMaskedWords(fullAnswer, hint);
        
        return new DictationFeedback(
                dto.correct,
                dto.message == null || dto.message.trim().isEmpty()
                        ? (dto.correct ? "Correct! Well done." : "Almost there. Use the hint and try again.")
                        : dto.message,
                fullAnswer, rightWords(dto.correct, fullAnswer),
                hint == null ? "" : hint, maskedWords
        );
    }

    private static String rightWords(boolean correct, String answer) {
        return correct ? answer : "";
    }

    private static String buildMaskedWords(String sentence, String hint) {
        if (sentence == null || sentence.trim().isEmpty()) return "";
        String[] words = sentence.trim().split("\\s+");
        String normalizedHint = hint == null ? "" : hint.trim().toLowerCase(Locale.US);
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!normalizedHint.isEmpty() && word.toLowerCase(Locale.US).contains(normalizedHint)) continue;
            if (builder.length() > 0) builder.append(' ');
            int stars = Math.max(3, Math.min(word.length(), 8));
            for (int i = 0; i < stars; i++) builder.append('*');
        }
        return builder.toString();
    }
}
