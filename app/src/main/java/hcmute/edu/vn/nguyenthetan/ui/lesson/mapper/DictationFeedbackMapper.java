package hcmute.edu.vn.nguyenthetan.ui.lesson.mapper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationResult;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;

public class DictationFeedbackMapper {

    public static DictationFeedback map(@NonNull DictationResult dto, String currentSentenceContent, String currentHintText) {
        String fullAnswer = dto.correctSentence == null || dto.correctSentence.trim().isEmpty()
                ? currentSentenceContent : dto.correctSentence;
        String[] answerWords = splitWords(fullAnswer);
        String correctWords = dto.correct ? fullAnswer : joinWords(answerWords, 0, Math.max(dto.matchedCount, 0));
        String visibleHint = dto.correct ? "" : buildVisibleHint(dto, currentHintText);
        String maskedWords = dto.correct ? "" : buildMaskedWords(dto.hintWords);

        return new DictationFeedback(
                dto.correct,
                dto.message == null || dto.message.trim().isEmpty()
                        ? (dto.correct ? "Correct! Well done." : "Incorrect. Try again.")
                        : dto.message,
                fullAnswer,
                correctWords,
                visibleHint,
                maskedWords
        );
    }

    private static String buildVisibleHint(DictationResult dto, String currentHintText) {
        if (dto.hintWords == null || dto.hintWords.isEmpty()) {
            return currentHintText == null ? "" : currentHintText;
        }

        List<String> revealedWords = new ArrayList<>();
        for (String word : dto.hintWords) {
            if (word == null || "***".equals(word.trim())) {
                break;
            }
            revealedWords.add(word);
        }
        return joinWords(revealedWords);
    }

    private static String buildMaskedWords(List<String> hintWords) {
        if (hintWords == null || hintWords.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String word : hintWords) {
            if (word == null || !"***".equals(word.trim())) {
                continue;
            }
            if (builder.length() > 0) builder.append(' ');
            builder.append(word);
        }
        return builder.toString();
    }

    private static String[] splitWords(String sentence) {
        return sentence == null || sentence.trim().isEmpty()
                ? new String[0]
                : sentence.trim().split("\\s+");
    }

    private static String joinWords(String[] words, int startInclusive, int endExclusive) {
        StringBuilder builder = new StringBuilder();
        for (int i = startInclusive; i < endExclusive && i < words.length; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(words[i]);
        }
        return builder.toString();
    }

    private static String joinWords(List<String> words) {
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(word.trim());
        }
        return builder.toString();
    }
}
